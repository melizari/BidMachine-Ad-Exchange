package com.appodealx.exchange.druid.models

import java.time.ZonedDateTime
import java.util.TimeZone

import com.appodealx.exchange.common.utils.CountryParser
import com.appodealx.exchange.druid.models.dto.Event
import com.appodealx.exchange.druid.transport.models.{ DictionaryItemDTO, ExportDTO }
import ing.wbaa.druid.{ DruidResponse, DruidResult, QueryType }
import io.circe.{ Decoder, Json }
import org.joda.time.{ DateTime, DateTimeZone }

package object query {

  implicit class SCDruidResultOps(dr: DruidResponse) {

    private def decodeListWithTimeStamp[T](implicit decoder: Decoder[T]) = dr.results.map {
      case DruidResult(ts, result) => decodeWithTimeStamp(ts, result)(decoder)
    }

    private def decodeWithTimeStamp[T](ts: ZonedDateTime, result: Json)(implicit decoder: Decoder[T]) =
      decoder.decodeJson(result) match {
        case Left(e) => throw e
        case Right(event) =>
          val dt = new DateTime(ts.toInstant.toEpochMilli, javaZdt2JodaDateTime(ts))
          dt -> event
      }

    private def decodeListOfListWithTimeStamp[T](implicit decoder: Decoder[List[T]]) = dr.results.map {
      case DruidResult(ts, result) => decodeListsWithTimeStamp(ts, result)(decoder)
    }

    private def decodeListsWithTimeStamp[T](ts: ZonedDateTime, result: Json)(implicit decoder: Decoder[List[T]]) =
      decoder.decodeJson(result) match {
        case Left(e) => throw e
        case Right(list) =>
          val dt = new DateTime(ts.toInstant.toEpochMilli, javaZdt2JodaDateTime(ts))
          list.map(dt -> _)
      }

    def listDto[T](implicit decoder: Decoder[T]) = dr.queryType match {
      case QueryType.TopN => decodeListOfListWithTimeStamp.flatten
      case _              => decodeListWithTimeStamp
    }

    def dto = dr.listDto[Event].map { case (d, e) => e.exportDto(d) }
  }

  implicit class LongOps(l: Long) {

    def zeroToOption: Option[Long] = Some(l).filter(_ > 0)
  }

  implicit class EventOps(event: Event) {

    import com.appodealx.exchange.common.utils.PriceRounder

    import scala.math.round

    def exportDto(dt: DateTime) =
      ExportDTO(
        country = event.country.map(c => DictionaryItemDTO(c, CountryParser.parseName(c).getOrElse("Unknown"))),
        adType = event.adType.map(a => DictionaryItemDTO(a.entryName, a.prettyValue)),
        platform = event.deviceOs.orElse(event.platform).map(p => DictionaryItemDTO(p.entryName, p.prettyValue)),
        agencyName = event.agencyName,
        agency = for {
          label <- event.agencyName
          value <- event.externalAgencyId.orElse(event.agencyExternalId).orElse(event.bidderAgencyExternalId)
        } yield DictionaryItemDTO(value.value.toString, label),
        seller = for {
          id    <- event.sellerId
          label <- event.sellerName.orElse(Some("null"))
        } yield DictionaryItemDTO(id.toString, label),
        timestamp = dt.toDateTime(DateTimeZone.UTC),
        bids = event.bids,
        spent = event.clearPrice.orElse(event.predictedPrice).map(_ / 1000).map(_.roundPrice(6)),
        wins = event.wins,
        impressions = event.impressions,
        clicks = event.clicks,
        finishes = event.finishes,
        sspIncome = event.sspIncome.map(_.roundPrice(6)),
        exchangeFee = event.exchangeFee.map(_.roundPrice(6)),
        errors = event.errors,
        lostImpressions = event.lostImpressions.map(round),
        lostImpressionsRevenue = event.lostImpressionsClearingPriceSum
          .orElse(event.lostImpressionsPredictedPriceSum)
          .map(_ / 1000)
          .map(_.roundPrice(6))
      )
  }

  implicit class ExportDTOOps(e: ExportDTO) {

    import cats.syntax.option._

    def fillZeros: ExportDTO = e.copy(
      spent = e.spent.getOrElse(0D).some,
      bids = e.bids.getOrElse(0L).some,
      wins = e.wins.getOrElse(0L).some,
      impressions = e.impressions.getOrElse(0L).some,
      clicks = e.clicks.getOrElse(0L).some,
      finishes = e.finishes.getOrElse(0L).some,
      errors = e.errors.getOrElse(0L).some,
      sspIncome = e.sspIncome.getOrElse(0D).some,
      exchangeFee = e.exchangeFee.getOrElse(0D).some,
      lostImpressions = e.lostImpressions.getOrElse(0L).some,
      lostImpressionsRevenue = e.lostImpressionsRevenue.getOrElse(0D).some
    )
  }

  private def javaZdt2JodaDateTime(zdt: ZonedDateTime) = DateTimeZone.forTimeZone(TimeZone.getTimeZone(zdt.getZone))
}
