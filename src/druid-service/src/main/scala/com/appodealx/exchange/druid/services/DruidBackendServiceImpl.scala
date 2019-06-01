package com.appodealx.exchange.druid.services

import java.time.format.DateTimeFormatter

import cats.syntax.option._
import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.models.analytics.AdType
import com.appodealx.exchange.common.utils.CountryParser
import com.appodealx.exchange.druid.models.scruid.models._
import com.appodealx.exchange.druid.services.queries.scruid._
import com.appodealx.exchange.druid.transport.models.{DictionaryItemDTO, ExportDTO, ReportSSPResponseRow}
import com.appodealx.exchange.settings.SettingsService
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import ing.wbaa.druid.definitions.FilterOperators._
import ing.wbaa.druid.definitions._
import ing.wbaa.druid.{DruidConfig, GroupByQuery}
import io.circe.Printer
import org.joda.time.{DateTimeZone, Interval}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class DruidBackendServiceImpl(settings: SettingsService) extends DruidBackendService with CirceBuyerSettingsInstances {

  import com.appodealx.exchange.common.utils.PriceRounder

  private val logger = LoggerFactory.getLogger(classOf[DruidBackendServiceImpl])


  private val impressionDataSource = "impressions"
  private val bidsDataSource       = "bids"
  private val clicksDataSource     = "clicks"
  private val winsDataSource       = "bids"
  private val errorsDataSource     = "errors"
  private val finishesDataSource   = "finish"
  private val lostDataSource       = "invalid-events"

  private val impressionDruidConfig = DruidConfig.DefaultConfig.copy(datasource = "impressions")
  private val bidsDruidConfig       = DruidConfig.DefaultConfig.copy(datasource = "bids")
  private val clicksDruidConfig     = DruidConfig.DefaultConfig.copy(datasource = "clicks")

  implicit val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  private val dtFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  override def query(interval: Interval,
                     granularity: Granularity,
                     country: Option[List[String]],
                     adType: Option[List[AdType]],
                     platform: Option[List[Platform]],
                     agency: Option[List[String]],
                     sellerIds: Option[List[Long]],
                     direction: Option[String],
                     isExport: Boolean,
                     agencyInternalId: Option[Long],
                     agencyExternalId: Option[Long])(implicit ex: ExecutionContext): Future[List[ExportDTO]] = {

    logger.debug(
      s"query:" +
        s"\ninterval:${interval.toString}" +
        s"\nagencyInternalId:${agencyInternalId.getOrElse(-1)}" +
        s"\nagencyExternalId:${agencyExternalId.getOrElse(-1)}"
    )

    def percentOf(one: Double, two: Double) =
      if (one != 0D && two != 0D) math.round(one * 100D / two * 100D) / 100D else 0D

    val adTypeDV = adType.map(s => (List("adType"), s.map(_.entryName)))
    val osDV     = platform.map(s => (List("deviceOs", "platform"), s.map(_.entryName)))
    val agencyDV = agency.map(
      (List("externalAgencyId", "agencyExternalId", "bidderAgencyExternalId", "agencyName", "bidderAgencyName"), _)
    )
    val countryDV = country.map((List("country"), _))
    val sellerDV  = sellerIds.map(_.map(_.toString)).map((List("sellerId", "sellerName"), _))

    val (dimensions, values) =
      adTypeDV
        .orElse(osDV.orElse(agencyDV.orElse(countryDV.orElse(sellerDV))))
        .getOrElse(Nil, Nil)

    import com.appodealx.exchange.druid.models.query._

    val scDimensions = queries.scruid.scDimensions(dimensions)

    val bidsResult = BidsQuery
      .make(bidsDataSource, interval, granularity, scDimensions, values, agencyExternalId, agencyInternalId)
      .execute
      .map(_.dto)
    val clicksResult = ClicksQuery
      .make(clicksDataSource,
            interval,
            granularity,
            scDimensions,
            values,
            agencyExternalId,
            agencyInternalId)
      .execute()
      .map(_.dto)
    val lostImpressions = InvalidEventQuery
      .make(lostDataSource, interval, granularity, scDimensions, values, agencyExternalId, agencyInternalId)
      .execute()
      .map(_.dto)
    val finishResult = FinishesQuery
      .make(finishesDataSource,
            interval,
            granularity,
            scDimensions,
            values,
            agencyExternalId,
            agencyInternalId)
      .execute
      .map(_.dto)
    val errorsResult = ErrorsQuery
      .make(errorsDataSource,
            interval,
            granularity,
            scDimensions,
            values,
            agencyExternalId,
            agencyInternalId)
      .execute
      .map(_.dto)
    val impressionsAndSpendResult = ImpressionsQuery
      .make(impressionDataSource,
            interval,
            granularity,
            scDimensions,
            values,
            agencyExternalId,
            agencyInternalId)
      .execute
      .map(_.dto)
    val winsResult = WinsQuery
      .make(winsDataSource, interval, granularity, scDimensions, values, agencyExternalId, agencyInternalId)
      .execute
      .map(_.dto)

    def fillWithZeroLines(seq: List[ExportDTO]) = {

      val groupedSeqByDimension: Map[Option[String], List[ExportDTO]] = dimensions match {
        case _ if dimensions contains "adType"   => seq.filter(_.adType.isDefined).groupBy(_.adType.map(_.value))
        case _ if dimensions contains "deviceOs" => seq.filter(_.platform.isDefined).groupBy(_.platform.map(_.value))
        case _ if dimensions contains "externalAgencyId" =>
          seq.filter(_.agency.isDefined).groupBy(_.agency.map(_.value))
        case _ if dimensions contains "country"  => seq.filter(_.country.isDefined).groupBy(_.country.map(_.value))
        case _ if dimensions contains "sellerId" => seq.filter(_.seller.isDefined).groupBy(_.seller.map(_.value))
        case _                                   => Map(None -> seq)
      }

      val rows = granularity match {
        case GranularityType.Hour =>
          val hoursInInterval = interval.toDuration.getStandardHours
          for (f <- List.range(0, hoursInInterval.toInt)) yield interval.getStart.toDateTime(DateTimeZone.UTC) plusHours f
        case GranularityType.Day => // Default days
          val i = interval.toDuration.getStandardDays
          for (f <- List.range(0, i.toInt)) yield interval.getStart.toDateTime(DateTimeZone.UTC) plusDays f
        case _ => Nil
      }

      val filled = groupedSeqByDimension.flatMap { grt =>
        val seqForZeroFill: List[ExportDTO] = grt._2
        if (seqForZeroFill.size >= rows.size) {
          seqForZeroFill // Do nothing
        } else {
          val dates       = seqForZeroFill.map(_.timestamp)
          val missedDates = rows.filterNot(dt => dates.contains(dt.toDateTime(DateTimeZone.UTC)))
          val missedDTOs = missedDates.flatMap { d =>
            val h = seqForZeroFill.headOption
            h.map { dto =>
              ExportDTO.empty.copy(
                timestamp = d.toDateTime(DateTimeZone.UTC),
                country = dto.country,
                adType = dto.adType,
                platform = dto.platform,
                agency = dto.agency,
                seller = dto.seller,
                spent = 0D.some,
                bids = 0L.some,
                wins = 0L.some,
                impressions = 0L.some,
                clicks = 0L.some,
                finishes = 0L.some,
                displayRate = 0D.some,
                ctr = 0D.some,
                ecpm = 0D.some,
                sspIncome = 0D.some,
                exchangeFee = 0D.some,
                errors = 0L.some,
                errorsRate = 0D.some,
                lostImpressions = 0L.some,
                lostImpressionsRevenue = 0D.some
              )
            }
          }
          seqForZeroFill ++ missedDTOs
        }
      }

      // Sort result
      filled.toList.sortBy(
        r =>
          (
            r.timestamp.getMillis,
            r.country.map(_.value).getOrElse(""),
            r.agency.map(_.value).getOrElse(""),
            r.platform.map(_.value).getOrElse(""),
            r.adType.map(_.value).getOrElse(""),
            r.seller.map(_.value).getOrElse("")
        )
      )
    }

    val result: Future[List[ExportDTO]] = {
      for {
        bids            <- bidsResult.map(_.map(_.fillZeros))
        wins            <- winsResult.map(_.map(_.fillZeros))
        imps            <- impressionsAndSpendResult.map(_.map(_.fillZeros))
        clicks          <- clicksResult.map(_.map(_.fillZeros))
        finishes        <- finishResult.map(_.map(_.fillZeros))
        errors          <- errorsResult.map(_.map(_.fillZeros))
        lostImpressions <- lostImpressions.map(_.map(_.fillZeros))
      } yield {

        val all = bids ++ wins ++ imps ++ clicks ++ finishes ++ errors ++ lostImpressions

        val resultRows =
          all.groupBy { exportDTO =>
            // Group by key with timestamp
            val country  = exportDTO.country.map(_.value).getOrElse("_")
            val adType   = exportDTO.adType.map(_.value).getOrElse("_")
            val platform = exportDTO.platform.map(_.value).getOrElse("_")
            val agency   = exportDTO.agency.map(_.value).getOrElse("_")
            val seller   = exportDTO.seller.map(_.value).getOrElse("_")

            val timestamp = exportDTO.timestamp.toString

            s"$timestamp#$adType#$country#$platform#$agency#$seller"

          }.mapValues { seq =>
            val someDTO = seq.headOption
            val semiEmptyDto = ExportDTO.empty.copy(
              timestamp = someDTO.map(_.timestamp).get,
              country = someDTO.flatMap(_.country),
              adType = someDTO.flatMap(_.adType),
              platform = someDTO.flatMap(_.platform),
              agency = someDTO.flatMap(_.agency),
              seller = someDTO.flatMap(_.seller)
            )
            // Merge to one "row" for key
            seq.fold(semiEmptyDto) { (acc, b) =>
              acc.copy(
                timestamp = b.timestamp, // from real one
                country = acc.country.orElse(b.country),
                adType = acc.adType.orElse(b.adType),
                platform = acc.platform.orElse(b.platform),
                agency = acc.agency.orElse(b.agency),
                seller = acc.seller.orElse(b.seller),
                spent = (acc.spent ++ b.spent).reduceOption(_ + _),
                bids = (acc.bids ++ b.bids).reduceOption(_ + _),
                wins = (acc.wins ++ b.wins).reduceOption(_ + _),
                impressions = (acc.impressions ++ b.impressions).reduceOption(_ + _),
                clicks = (acc.clicks ++ b.clicks).reduceOption(_ + _),
                finishes = (acc.finishes ++ b.finishes).reduceOption(_ + _),
                sspIncome = (acc.sspIncome ++ b.sspIncome).reduceOption(_ + _),
                exchangeFee = (acc.exchangeFee ++ b.exchangeFee).reduceOption(_ + _),
                errors = (acc.errors ++ b.errors).reduceOption(_ + _),
                lostImpressions = (acc.lostImpressions ++ b.lostImpressions).reduceOption(_ + _),
                lostImpressionsRevenue = (acc.lostImpressionsRevenue ++ b.lostImpressionsRevenue).reduceOption(_ + _)
              )
            }
          }.values.toList

        val computedRows = resultRows.map { row =>
          row.copy(
            // Already zeroFilled count fields
            displayRate = percentOf(row.impressions.getOrElse(0L).toDouble, row.wins.getOrElse(0L).toDouble).some,
            ctr = percentOf(row.clicks.getOrElse(0L).toDouble, row.impressions.getOrElse(0L).toDouble).some,
            ecpm = row.impressions
              .flatMap(i => row.spent.map(_ / i))
              .map(impressionPrice => (impressionPrice * 1000).roundPrice(2))
              .getOrElse(0D)
              .some,
            sspIncome = row.sspIncome.map(_ / 1000).map(_.roundPrice(6)).getOrElse(0D).some,
            exchangeFee = row.exchangeFee.map(_ / 1000).map(_.roundPrice(6)).getOrElse(0D).some,
            errors = row.errors.getOrElse(0L).some,
            errorsRate = percentOf(row.errors.getOrElse(0L).toDouble, row.wins.getOrElse(0L).toDouble).some,
            lostImpressionsRevenue = row.lostImpressionsRevenue.map(_.roundPrice(6)).getOrElse(0D).some
          )
        }

        fillWithZeroLines(computedRows)
      }
    }

    result
  }

  override def agencies(interval: Interval)(implicit ex: ExecutionContext): Future[List[DictionaryItemDTO]] =
    settings.readAllAgencies.invoke.map { agencies =>
      agencies.flatMap(a => a.externalId.map(id => DictionaryItemDTO(id.value.toString, a.title))).sortBy(_.label)
    }

  override def countries(interval: Interval)(implicit ex: ExecutionContext): Future[List[DictionaryItemDTO]] = {

    val topNQuery = ing.wbaa.druid.TopNQuery(
      dimension = DefaultDimension("country"),
      aggregations = List(LongSumAggregation(name = "count", fieldName = "bidRequestCount")),
      intervals = List(interval.toString),
      threshold = 500,
      metric = "count"
    )(bidsDruidConfig)

    topNQuery.execute.map { dr =>
      dr.list[CountryResponse].sortBy(_.country).flatMap { c =>
        for {
          code <- c.country
          name <- CountryParser.parseName(code)
        } yield DictionaryItemDTO(code, name)
      }
    }
  }

  override def sellers(interval: Interval)(implicit ex: ExecutionContext): Future[List[DictionaryItemDTO]] =
    settings.findAllSellers.invoke().map { sellers =>
      sellers.flatMap { seller =>
        for {
          value <- seller.id
          label <- seller.name
        } yield DictionaryItemDTO(value.toString, label)
      }.distinct.sortBy(_.label)
    }

  override def sellerReport(id: Long, interval: Interval)(
    implicit ex: ExecutionContext
  ): Future[Iterable[ReportSSPResponseRow]] = {
    val st = System.currentTimeMillis()

    logger.debug("Starting seller report in druid backend service.")

    val dimensions = List(
      Dimension("country", "country".some),
      Dimension("appName", "app_name".some),
      Dimension("appBundle", "app_bundle".some),
      Dimension("deviceOs", "platform".some),
      Dimension("adType", "ad_type".some),
      Dimension("sellerId", "publisher_id".some),
    )
    val granularity = GranularityType.Day
    val filter      = scSellerFilter(id)

    val impressionQuery = GroupByQuery(
      aggregations = List(LongSumAggregation(name = "impressions", fieldName = "count"), DoubleSumAggregation("sspIncome", "sspIncome")),
      intervals = List(interval.toString),
      filter = filter.some,
      dimensions = dimensions,
      granularity = granularity,
    )(impressionDruidConfig)

    val clicksQuery = GroupByQuery(
      aggregations = List(LongSumAggregation(name = "clicks", fieldName = "count")),
      intervals = List(interval.toString),
      filter = filter.some,
      dimensions = dimensions,
      granularity = granularity,
    )(clicksDruidConfig)

    val impressionsResult =
      impressionQuery.execute().map(_.series[ReportResult].flatMap(t => t._2.map(_.copy(date = t._1.some))))
    val clicksResult =
      clicksQuery.execute().map(_.series[ReportResult].flatMap(t => t._2.map(_.copy(date = t._1.some))))

    val result = Future
      .sequence(List(impressionsResult, clicksResult))
      .map(
        _.flatten.groupBy { r =>
          // Grouping by key: time + country + app (app id) + platform (device os)
          val date = r.date.map(_.toString).getOrElse("_")
          s"$date#${r.country.getOrElse("_")}#${r.`app_bundle`.getOrElse("_")}#${r.platform
            .getOrElse("_")}#${r.`ad_type`.getOrElse("_")}"
        }.mapValues { rs =>
          // Summarize all values in row
          rs.headOption.map {
            r =>
              val emptyRow: ReportResult =
                r.copy(impressions = 0L.some, clicks = 0L.some, ctr = None, ecpm = None, sspIncome = None)

              val row = rs.fold(emptyRow) { (acc, rr) =>
                val imps      = acc.impressions.getOrElse(0L) + rr.impressions.getOrElse(0L)
                val clicks    = acc.clicks.getOrElse(0L) + rr.clicks.getOrElse(0L)
                val sspIncome = acc.sspIncome.getOrElse(0D) + rr.sspIncome.getOrElse(0D)
                val appBundle = acc.`app_bundle`.orElse(rr.`app_bundle`)
                val appName   = acc.`app_name`.orElse(rr.`app_name`)
                val platform  = acc.platform.orElse(rr.platform)
                val adType    = acc.`ad_type`.orElse(rr.`ad_type`)
                emptyRow.copy(
                  impressions = imps.some,
                  clicks = clicks.some,
                  sspIncome = sspIncome.some,
                  `app_name` = appName,
                  `app_bundle` = appBundle,
                  `ad_type` = adType,
                  platform = platform
                )
              }
              val rowWithCalculatedParams = row.copy(
                country = row.country.orElse("ZZ".some),
                ctr = (for { c <- row.clicks; i <- row.impressions if row.impressions.exists(_ != 0L) } yield
                  c.toDouble / i * 100) orElse 0D.some,
                ecpm = (for { i <- row.impressions if row.impressions.exists(_ != 0L); s <- row.sspIncome } yield
                  s / i) orElse 0D.some
              )
              val resultRow = ReportSSPResponseRow(
                date = rowWithCalculatedParams.date.get.format(dtFormatter),
                country = rowWithCalculatedParams.country.getOrElse("ZZ"), // May be null = by default "ZZ"
                `publisher_id` = id,
                `app_name` = rowWithCalculatedParams.`app_name`, // May be null
                `app_bundle` = rowWithCalculatedParams.`app_bundle`, // May be null
                `platform` = rowWithCalculatedParams.platform, // May be null
                `ad_type` = rowWithCalculatedParams.`ad_type`,
                impressions = rowWithCalculatedParams.impressions.getOrElse(0L), // Possible anomalies (if exists or possible). Imps = 0, clicks > 0 and ctr must be ? (0 or null)
                clicks = rowWithCalculatedParams.clicks.getOrElse(0L),
                ctr = rowWithCalculatedParams.ctr.getOrElse(0D).roundPrice(2),
                ecpm = rowWithCalculatedParams.ecpm.getOrElse(0D).roundPrice(6),
                revenue = rowWithCalculatedParams.sspIncome.map(_ / 1000).getOrElse(0D).roundPrice(6)
              )
              resultRow
          }
        }.values.flatten
      )
      .recover {
        case e =>
          logger.error(e.getMessage, e)
          throw BadRequest(e.getMessage)
      }

    result.onComplete {
      case Failure(_) =>
        logger.error(s"failed sellerReport: elapsed time: ${System.currentTimeMillis() - st}")
      case Success(_) =>
        logger.debug(s"success sellerReport: elapsed time: ${System.currentTimeMillis() - st}")

    }
    result
  }
}
