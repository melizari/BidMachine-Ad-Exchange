package services.auction.rtb.adapters

import java.util.concurrent.ThreadLocalRandom

import akka.util.ByteString
import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.models.auction.{Bidder, BidderId}
import com.appodealx.exchange.common.models.jsoniter.JsoniterRtbInstances
import com.appodealx.openrtb.{BidRequest, BidResponse}
import com.github.plokhotnyuk.jsoniter_scala.core.{readFromArray, writeToArray}
import io.circe.Printer
import kamon.BidderMetrics
import models.auction.NoBidReason
import play.api.http.ContentTypes
import play.api.http.Status._
import play.api.libs.ws.{BodyWritable, InMemoryBody, WSClient, WSResponse}
import play.api.{Configuration, Logger}
import play.mvc.Http.HeaderNames
import redis.RedisClient
import services.auction.rtb.Adapter

import cats.MonadError
import cats.syntax.either._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Try}

class OpenRtbAdapter[F[_]](ws: WSClient,
                           redisClient: RedisClient,
                           configuration: Configuration)(implicit M: MonadError[F, Throwable], Execute: Execute[F])
    extends Adapter[F]
    with JsoniterRtbInstances {

  import NoBidReason._
  import models.auction.PimpedBidResponse

  private val logger = Logger(getClass)

  private val random = new Random(ThreadLocalRandom.current)

  implicit val customPrinter = Printer.noSpaces.copy(dropNullValues = true)

  private val tMaxDefaultDuration = configuration.get[Int]("settings.default.tMax").milliseconds

  def announce(bidder: Bidder, request: BidRequest, cachedRequest: ByteString) = {

    val bidderName = s"bidder: `${bidder.title}`(${bidder.id.getOrElse(BidderId(-1)).value})"
    val agencyName = s"agency: `${bidder.agencyId.value}`"

    def nbr(response: BidResponse) = {
      logger.debug(s"BidResponse from $bidderName :" + new String(writeToArray(response)))
      response.nbr.fold(response.asRight[NoBidReason])(RtbNoBidReason(_).asLeft)
    }

    def parseRes(response: WSResponse) = {
      def errorMessage(t: Throwable) =
        t.getMessage.split(',').headOption.getOrElse("Unknown parsing error")

      logger.debug(s"Dirty response from $bidderName :\n" + new String(response.bodyAsBytes.toArray))
      val r = Try(readFromArray[BidResponse](response.bodyAsBytes.toArray)).toEither.leftMap(errorMessage)
      logger.debug(
        "Parsing " + (if (r.isRight) "success" else s"failed with status: ${r.left.toOption.getOrElse("unknown")}")
      )
      r
    }

    val qps = bidder.maxRpm
    val key = bidder.id.get.value.toString

    val writeableOf_BytesAsJson: BodyWritable[ByteString] = BodyWritable(InMemoryBody.apply, "application/json")

    def doRequest(implicit ec: ExecutionContext) = {
      val startedTimer = BidderMetrics.startTimer(BidderMetrics.normalizedBidderName(bidder))

      measureMetrics(bidder, request)

      val result = {

        logger.debug(s"BidRequest for $bidderName : " + cachedRequest.utf8String)

        ws
          .url(bidder.endpoint.toString)
          .withMethod("POST")
          .addHttpHeaders("x-openrtb-version" -> bidder.rtbVersion.prettyValue)
          .withBody(cachedRequest)(writeableOf_BytesAsJson)
          .addHttpHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)
          .withRequestTimeout(request.tmax.map(_.milliseconds).getOrElse(tMaxDefaultDuration))
          .execute()
      }.map {
        case r if (r.status == NO_CONTENT || r.status == OK) && r.body.isEmpty =>
          logger.debug(s"NoFill for $bidderName and $agencyName with status ${r.status} and body isEmpty=${r.body.isEmpty} for ${bidder.endpoint.toString}")
          (false, NoFill.asLeft)
        case r if r.status == NO_CONTENT =>
          logger.debug(s"NoFill for $bidderName and $agencyName with status NoContent ${r.status} for ${bidder.endpoint.toString}")
          (false, NoFill.asLeft)
        case r if r.status == OK =>
          logger.debug(s"Fill for $bidderName and $agencyName with status ${r.status} for ${bidder.endpoint.toString}")
          (false, parseRes(r).leftMap(ParsingError).flatMap(nbr).map(_.toBids))
        case r =>
          logger.debug(s"UnexpectedResponse for $bidderName and $agencyName with status: ${r.status} for ${bidder.endpoint.toString}")
          (false, UnexpectedResponse(r.status).asLeft)
      }

      result.foreach(_ => startedTimer.stop)

      result.recover {
        case e: Exception =>
          logger.debug(s"Request exception ($bidderName, $agencyName, endpoint:`${bidder.endpoint.toString}`): $e")
          (false, RequestException(e.getClass.getName).asLeft)
      }
    }

    def limitRequest(implicit ec: ExecutionContext) = {
      logger.debug(s"Limited by QPS for $bidderName and $agencyName")
      Future.successful((false, QueriesLimitExceeded.asLeft))
    }

    Execute.deferFutureAction { implicit ctx =>
      for {
        c <- redisClient.incr(key)
        _ <- if (c == 1) redisClient.expire(key, 1) else Future(false)
        r <- if (c > qps) limitRequest else doRequest
      } yield r
    }
  }

  private def measureMetrics(bidder: Bidder, request: BidRequest): Unit = {

    val bidderName = BidderMetrics.normalizedBidderName(bidder)

    val country = request.device.flatMap(_.geo).flatMap(_.country).getOrElse("ZZZ").toUpperCase

    val banner = BidderMetrics.request(BidderMetrics.BANNER, bidderName)(_)
    val instl  = BidderMetrics.request(BidderMetrics.INTERSTITIAL, bidderName)(_)
    val video  = BidderMetrics.request(BidderMetrics.VIDEO, bidderName)(_)
    val native = BidderMetrics.request(BidderMetrics.NATIVE, bidderName)(_)

    request.imp.foreach { imp =>
      if (imp.banner.isDefined) {
        if (imp.instl.contains(true)) {
          instl(country).increment()
        } else {
          banner(country).increment()
        }
      }

      if (imp.video.isDefined) video(country).increment()
      if (imp.native.isDefined) native(country).increment()
    }
  }
}
