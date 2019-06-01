package com.appodealx.exchange.druid

import java.util.UUID

import akka.{Done, NotUsed}
import cats.syntax.option._
import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.models.analytics.AdType
import com.appodealx.exchange.common.utils.NotNullOption
import com.appodealx.exchange.druid.services.DruidBackendService
import com.appodealx.exchange.druid.transport.models.{DictionaryItemDTO, ExportDTO}
import com.appodealx.exchange.settings.SettingsService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport._
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import ing.wbaa.druid.definitions.GranularityType
import io.circe.Printer
import io.circe.syntax._
import org.joda.time.{Duration => _, _}
import play.api.Logger
import purecsv.safe._
import scalacache.caffeine.CaffeineCache
import scalacache.modes.scalaFuture._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


class DruidServiceImpl(druidService: DruidBackendService,
                       settingsService: SettingsService,
                       localCache: CaffeineCache[Long])
                      (implicit executionContext: ExecutionContext) extends DruidService {

  implicit val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  private val logger = Logger(getClass)

  private def agencyExternalId(id: Long): Future[Long] = {
    val key = s"agency/internal/id/$id"
    val ttl = Duration.create(1, SECONDS)
    val extIdEf: Future[Long] =
      settingsService
        .readAgency(id)
        .invoke()
        .map(_.externalId.map(_.value).get)
        .recoverWith {
          case t: Throwable => Future.failed(new Exception(s"Agency external id expected for agency id=`$id`.", t))
        }
    localCache.cachingF(key)(ttl.some)(extIdEf)
  }

  override def healthCheck = ServerServiceCall { _ =>
    Future.successful(Done)
  }

  override def performance = ServerServiceCall { performanceRequest =>

    logger.debug("Performance request:\n" + performanceRequest.asJson.pretty(printer))

    val now = DateTime.now(DateTimeZone.UTC)
    val interval = performanceRequest
      .interval
      .map { interval =>
        val i = if (interval.getStart.isAfter(now)) interval.withStart(now) else interval
        val semiEndDate = if (i.getEnd.isEqual(i.getStart)) {
          i.getEnd.plusDays(1)
        } else {
          i.getEnd
        }

        if (semiEndDate.isAfter(DateTime.now(DateTimeZone.UTC))) {
          i.withEnd(DateTime.now(DateTimeZone.UTC).property(DateTimeFieldType.hourOfDay()).roundCeilingCopy())
        } else {
          i.withEnd(semiEndDate.property(DateTimeFieldType.hourOfDay()).roundCeilingCopy())
        }

      }

    interval match {
      case Some(i) =>
        // For empty [] return empty response
        if (
          performanceRequest.adType.exists(_.isEmpty) ||
            performanceRequest.platform.exists(_.isEmpty) ||
            performanceRequest.agency.exists(_.isEmpty) ||
            performanceRequest.country.exists(_.isEmpty) ||
            performanceRequest.sellerIds.exists(_.isEmpty)
        ) {
          Future.successful(Nil)
        } else {
          val agencyIdOpt = performanceRequest.agencyId

          def query(agencyId: Option[Long] = None, externalAgencyId: Option[Long] = None) =
            druidService.query(
              interval = i,
              granularity = performanceRequest.granularity.flatMap(GranularityType.decode(_).toOption).getOrElse(granularity(i)),
              country = performanceRequest.country.map(_.map(_.toUpperCase)),
              adType = performanceRequest.adType,
              platform = performanceRequest.platform,
              agency = performanceRequest.agency,
              sellerIds = performanceRequest.sellerIds,
              direction = performanceRequest.direction,
              agencyInternalId = agencyId,
              agencyExternalId = externalAgencyId
            )

          val result: Future[List[ExportDTO]] =
            agencyIdOpt
              .map(agencyId => agencyExternalId(agencyId).flatMap(extId => query(agencyId.some, extId.some)))
              .getOrElse(query())

          result
        }
      case None => throw BadRequest("Interval expected")
    }

  }

  override def adTypes = ServerServiceCall { _ =>
    val responses = AdType.values.map(a => DictionaryItemDTO(a.entryName, a.prettyValue)).toList
    Future.successful(responses)
  }

  override def platforms = ServerServiceCall { _ =>
    val responses = Platform.values.map(p => DictionaryItemDTO(p.entryName, p.prettyValue)).toList
    Future.successful(responses)
  }

  override def countries(date: Option[String], start: Option[String], end: Option[String]) = ServerServiceCall { _ =>
    intervalFromParams(date, start, end)
      .map { i =>
        druidService.countries(i).recover {
          case e: Exception =>
            throw new TransportException(TransportErrorCode.InternalServerError, s"Request countries failed with exception: $e")
        }
      }.getOrElse(throw BadRequest("Start and End date expected"))
  }

  override def agencies(date: Option[String], start: Option[String], end: Option[String]) = ServerServiceCall { _ =>
    intervalFromParams(date, start, end)
      .map { i =>
        druidService.agencies(i)
          .map { seq =>
            logger.debug(s"AGENCY COUNT: ${seq.length}")
            seq
          }
          .recover {
            case e: Exception =>
              throw new TransportException(TransportErrorCode.InternalServerError, s"Request agency failed with exception: $e")
          }
      }.getOrElse(throw BadRequest("Start and End date expected"))
  }

  private def intervalFromParams(date: Option[String], start: Option[String], end: Option[String]): Option[Interval] = {
    val now = DateTime.now(DateTimeZone.UTC)
    if (start.isDefined) {
      Try {
        val s = DateTime.parse(start.get)
        val validEndDate = end.map { endDate =>
          val parsedEndDate = DateTime.parse(endDate)

          val semiEndDate = if (parsedEndDate.isEqual(s)) {
            s.plusDays(1)
          } else {
            parsedEndDate
          }

          if (semiEndDate.isAfter(now)) {
            now.property(DateTimeFieldType.hourOfDay()).roundCeilingCopy()
          } else {
            semiEndDate.property(DateTimeFieldType.hourOfDay()).roundCeilingCopy()
          }

        }

        val e = validEndDate.getOrElse(s.plusDays(1))
        new Interval(s, e)
      }.toNotNullOption
    } else if (date.isDefined) {
      Try {
        val d = DateTime.parse(date.get).withTimeAtStartOfDay()
        new Interval(d, d.plusDays(1))
      }.toNotNullOption
    } else {
      None
    }
  }

  private def granularity(interval: Interval) =
    /*if (interval.toDuration.getStandardDays > 31) {
      GranularityType.Month
    } else if (interval.toDuration.getStandardDays > 7) {
      GranularityType.Week
    } else*/
    if (interval.toDuration.getStandardDays > 1) {
      GranularityType.Day
    } else {
      GranularityType.Hour
    }

  override def sellers(date: Option[String], start: Option[String], end: Option[String]): ServiceCall[NotUsed, List[DictionaryItemDTO]] = ServerServiceCall { _ =>
    intervalFromParams(date, start, end)
      .map { i =>
        druidService.sellers(i)
          .map { seq =>
            logger.debug(s"Sellers COUNT: ${seq.length}")
            seq
          }
          .recover {
            case e: Exception =>
              throw new TransportException(TransportErrorCode.InternalServerError, s"Request sellers failed with exception: $e")
          }
      }.getOrElse(throw BadRequest("Start and End date expected"))
  }

  // format for test purpose, possible: "json" for JsonString response format.
  override def sspReport(id: Long, start: String, end: String, format: Option[String], csvWithHeader: Option[Boolean]) = ServerServiceCall { _ =>
    val st = System.currentTimeMillis()
    val s = DateTime.parse(start).withZoneRetainFields(DateTimeZone.UTC).withTimeAtStartOfDay()
    val e = DateTime.parse(end).withZoneRetainFields(DateTimeZone.UTC).withTimeAtStartOfDay()
    val interval = new Interval(s, e)
    val csvHeaderEnable = csvWithHeader.getOrElse(false)

    val formatResponse = format.map(_.toLowerCase).getOrElse("csv")

    logger.debug(s"filter by seller id: $id for Interval: ${interval.toString}")

    val result =
      druidService.sellerReport(id, interval)
        .map { seq =>
          logger.debug(s"Druid response size ${seq.size}")
          formatResponse match {
            case "csv" =>
              val header = """date,country,publisher_id,app_name,app_bundle,platform,ad_type,impressions,clicks,ctr,ecpm,revenue"""
              if (seq.nonEmpty) {
                if (csvHeaderEnable) {
                  (header :: seq.toCSVLines(",").toList).mkString(System.lineSeparator())
                } else {
                  seq.toCSVLines(",").mkString(System.lineSeparator())
                }
              } else {
                ""
              }
            case "json" =>
              seq.map(_.asJson.pretty(printer)).mkString(System.lineSeparator())
            case string => throw new NoSuchElementException(s"Response format `$string` not supported")
          }
        }
        .recover {
          case error =>
            val errorId = UUID.randomUUID().toString
            logger.error(s"$errorId : ${error.getMessage}", error)
            throw new TransportException(TransportErrorCode.InternalServerError, new ExceptionMessage("druid error", error.getMessage), error)
        }
    result.onComplete(_ => logger.debug(s"sspReport: elapsed time: ${System.currentTimeMillis() - st}"))
    result
  }

}
