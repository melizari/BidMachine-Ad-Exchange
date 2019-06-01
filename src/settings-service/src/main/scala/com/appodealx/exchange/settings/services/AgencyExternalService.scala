package com.appodealx.exchange.settings.services

import akka.http.scaladsl.model.Uri
import com.appodealx.exchange.common.models.auction.AgencyExternalId
import com.appodealx.exchange.common.services.SubstitutionService
import com.appodealx.exchange.settings.models.buyer.{
  AppodealResponseStatus,
  ExternalAgencyCreateRequest,
  ExternalAgencyCreateResponse,
  ExternalAgencyUpdateRequest
}
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import com.typesafe.config.Config
import io.circe.parser.decode
import io.circe.syntax._
import monix.eval.Task
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.ws.{WSAuthScheme, WSClient}

import scala.util.Try

/**
 * Service for create and get info about system campaign on external appodeal
 */
trait AgencyExternalService {

  /**
   * Create external agency(campaign)
   *
   * @param request create agency request
   * @return external agency id
   */
  def createExtAgency(request: ExternalAgencyCreateRequest): Task[Option[AgencyExternalId]]

  /**
   * Update external agency(campaign)
   *
   * @param request request
   * @return true if updates success, false id failure
   */
  def updateExtAgency(request: ExternalAgencyUpdateRequest): Task[Boolean]

}

class AgencyExternalServiceImpl(wsClient: WSClient, config: Config, substitutionService: SubstitutionService)
    extends AgencyExternalService
    with CirceBuyerSettingsInstances {

  private val secure = false
  private val host   = config.getString("external.domain")

  private val uriScheme    = Uri.httpScheme(secure)
  private val uriHost      = Uri.Host(host)
  private val uriAuthority = Uri.Authority(uriHost)
  private val baseUri      = Uri(uriScheme, uriAuthority)

  private val createExtAgencyUrl      = baseUri.withPath(Uri.Path(config.getString("external.agency.service.create.url")))
  private val updateExtAgencyUrl      = baseUri.withPath(Uri.Path(config.getString("external.agency.service.update.url")))

  private val user     = Try(config.getString("external.service.appodeal.user")).toOption.getOrElse("")
  private val password = Try(config.getString("external.service.appodeal.password")).toOption.getOrElse("")

  private val exchangeCustomAuthKey    = config.getString("external.service.appodeal.key")
  private val exchangeCustomAuthSecret = config.getString("external.service.appodeal.secret")

  /**
   * Create external agency(campaign)
   *
   * @param request create agency request
   * @return external agency id
   */
  override def createExtAgency(request: ExternalAgencyCreateRequest): Task[Option[AgencyExternalId]] =
    Task.deferFuture {
      wsClient
        .url(createExtAgencyUrl.toString)
        .withAuth(user, password, WSAuthScheme.BASIC)
        .addHttpHeaders(exchangeCustomAuthKey -> exchangeCustomAuthSecret)
        .addHttpHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)
        .post(s"""{"title":"${request.title}"}""")
    }.map { wsResponse =>
      decode[ExternalAgencyCreateResponse](wsResponse.body).right.toOption.map(_.`campaign_type_id`)
    }

  /**
   * Update external agency(campaign): update title '{"id": 123, "title": "new title"}'
   *
   * @param request request
   * @return true if updates success, false id failure
   */
  override def updateExtAgency(request: ExternalAgencyUpdateRequest): Task[Boolean] =
    Task.deferFuture {
      wsClient
        .url(updateExtAgencyUrl.toString)
        .withAuth(user, password, WSAuthScheme.BASIC)
        .addHttpHeaders(exchangeCustomAuthKey -> exchangeCustomAuthSecret)
        .addHttpHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)
        .post(request.asJson.noSpaces)
    }.map { wsResponse =>
      decode[AppodealResponseStatus](wsResponse.body).right.toOption.exists(p => p.status == 200)
    }

}
