package com.appodealx.exchange.common.services.aws

import java.security.MessageDigest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import com.typesafe.config.Config
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * import akka.actor.ActorSystem
  * implicit val system = ActorSystem()
  * import com.typesafe.config.ConfigFactory
  * val config = ConfigFactory.load()
  * import com.appodealx.adwatch.common.service.AWSStorageService
  * val s3 = new AWSStorageService(config)
  */
class AWSStorageService(config: Config)(implicit system: ActorSystem) extends StorageService {

  case class SigningKey(date: DateTime, key: Array[Byte])

  private val EMPTY_STRING_SHA256 = "UNSIGNED-PAYLOAD"
  private val AWS_V4_REQUEST = "aws4_request"
  private val AWS_SERVICE = "s3"

  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  private val accessKey = config.getString("accessKey")
  private val secretKey = config.getString("secretKey")
  private val bucketName = config.getString("bucketName")
  private val region = config.getString("region")

  private val isoDateFormatter = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'").withZoneUTC()

  private val host = s"$bucketName.$AWS_SERVICE.amazonaws.com"
  private val hostCDN = "creatives.appodealx.com"

  private val connectionFlow = Http().outgoingConnection(host)

  private val timeout = 5.seconds


  private def bytesToHex(bytes: Array[Byte]): String = bytes.map("%02x".format(_)).mkString

  private def sha256(string: String): String = {
    bytesToHex(MessageDigest.getInstance("SHA-256").digest(string.getBytes("utf-8")))
  }

  private def hmacSha256(secret: Array[Byte], stringToSign: String) = {
    val m = Mac.getInstance("HmacSHA256")
    m.init(new SecretKeySpec(secret, "HmacSHA256"))
    m.doFinal(stringToSign.getBytes("utf-8"))
  }

  private def calculateSigningKey(date: DateTime) = {
    val secret = ("AWS4" + secretKey).getBytes("utf-8")
    val dateString = date.toString("yyyyMMdd")
    val dateKey = hmacSha256(secret, dateString)
    val dateRegionKey = hmacSha256(dateKey, region)
    val dateRegionServiceKey = hmacSha256(dateRegionKey, AWS_SERVICE)

    hmacSha256(dateRegionServiceKey, AWS_V4_REQUEST)
  }

  private def signingKey = {
    val dt = DateTime.now
    SigningKey(dt, calculateSigningKey(dt))
  }

  private def calculateCanonicalRequest(verb: String, canonicalUri: String, headers: List[HttpHeader], payloadHash: String) = {
    val canonicalHeaders = headers.map(h => h.name.toLowerCase + ":" + h.value.trim).sorted.map(_ + "\n").mkString
    val signedHeaders = headers.map(_.name).sorted.map(_.toLowerCase).mkString(";")
    val canonicalQuery = ""

    verb + "\n" + canonicalUri + "\n" + canonicalQuery + "\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + payloadHash
  }


  private def calculateSignature(signingKey: SigningKey, date: DateTime, canonicalRequest: String) = {
    val canonicalRequestHash = sha256(canonicalRequest)
    val dateString = signingKey.date.toString("yyyyMMdd")
    val timeStamp = date.toString(isoDateFormatter)
    val scope = s"$dateString/$region/$AWS_SERVICE/$AWS_V4_REQUEST"

    val stringToSign = "AWS4-HMAC-SHA256" + "\n" + timeStamp + "\n" + scope + "\n" + canonicalRequestHash

    bytesToHex(hmacSha256(signingKey.key, stringToSign))
  }

  private def calculateAuthHeader(signedHeaders: List[String], signature: String, signingKey: SigningKey) = {
    val credentials = s"$accessKey/${signingKey.date.toString("yyyyMMdd")}/$region/$AWS_SERVICE/$AWS_V4_REQUEST"
    s"AWS4-HMAC-SHA256 Credential=$credentials,SignedHeaders=${signedHeaders.sorted.map(_.toLowerCase).mkString(";")},Signature=$signature"
  }

  private def signRequest(request: HttpRequest, payloadHash: String): HttpRequest = {
    val date = DateTime.now
    val key = signingKey

    val awsHeaders = List(
      RawHeader("x-amz-date", date.toString(isoDateFormatter)),
      RawHeader("x-amz-content-sha256", payloadHash)
    ) ++ request.headers

    val canonicalReq = calculateCanonicalRequest(request.method.value, request.uri.path.toString, awsHeaders, payloadHash)
    val signature = calculateSignature(key, date, canonicalReq)
    val authHeader = calculateAuthHeader(awsHeaders.map(_.name), signature, key)

    val signedHeaders = awsHeaders :+ RawHeader("Authorization", authHeader)

    request.withHeaders(signedHeaders)
  }

  def get(path: String) = {
    val headers = List[HttpHeader](Host(host))
    val req = HttpRequest(uri = s"/$path", headers = headers)

    val signedReq = signRequest(req, EMPTY_STRING_SHA256)

    Source.single(signedReq)
      .via(connectionFlow)
      .runWith(Sink.head)
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          entity.toStrict(timeout).map(e => Some(e.contentType -> e.data.toArray[Byte]))

        case a =>
          Future(None)
      }.recover {
      case e => None
    }
  }


  override def check(hash: String): Future[Boolean] = {
    val headers = List[HttpHeader](Host(host))
    val req = HttpRequest(method = HttpMethods.HEAD, uri = s"/$hash", headers = headers)

    val signedReq = signRequest(req, EMPTY_STRING_SHA256)

    Source.single(signedReq)
      .via(connectionFlow)
      .runWith(Sink.head)
      .map {
        case HttpResponse(StatusCodes.OK, _, _, _) => true
        case _ => false
      }
  }


  override def put(path: String,
                   contentType: ContentType,
                   content: Array[Byte],
                   contentSha256: Option[String]): Future[Boolean] = {
    val headers = List[HttpHeader](Host(host))
    val entity = HttpEntity(contentType = contentType, bytes = content)
    val req = HttpRequest(method = HttpMethods.PUT, uri = s"/$path", headers = headers, entity = entity)

    val signedReq = signRequest(req, contentSha256.get)

    Source.single(signedReq)
      .via(connectionFlow)
      .runWith(Sink.head)
      .map {
        case HttpResponse(StatusCodes.OK, _, _, _) => true
        case HttpResponse(StatusCodes.NoContent, _, _, _) => true
        case a =>
          false
      }.recover {
      case _ => false
    }
  }

  override def putSource(hash: String,
                         contentLength: Long,
                         contentType: ContentType,
                         content: Source[ByteString, Any],
                         contentSha256: Option[String]): Future[Boolean] = {

    val headers = List[HttpHeader](Host(host))
    val entity = HttpEntity(contentType = contentType, contentLength = contentLength, data = content)
    val req = HttpRequest(method = HttpMethods.PUT, uri = s"/$hash", headers = headers, entity = entity)

    val signedReq = signRequest(req, contentSha256.get)

    Source.single(signedReq)
      .via(connectionFlow)
      .runWith(Sink.head)
      .map {
        case HttpResponse(StatusCodes.OK, _, _, _) => true
        case _ => false
      }.recover {
      case _ => false
    }
  }

  def fullUriForKey(key: String): String = s"http://creatives.appodealx.com/$key"
}