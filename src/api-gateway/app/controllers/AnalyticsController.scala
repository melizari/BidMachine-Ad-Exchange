package controllers

import java.nio.file.AccessDeniedException

import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.druid.DruidService
import com.appodealx.exchange.druid.transport.models.{DictionaryItemDTO, ExportDTO, PerformanceDTO}
import com.appodealx.exchange.settings.SettingsService
import com.mohiva.play.silhouette.api.Silhouette
import io.circe.Printer
import io.circe.syntax._
import io.swagger.annotations._
import play.api.http.ContentTypes
import play.api.libs.circe.Circe
import play.api.mvc._
import silhouette.UserRole.{Admin, Buyer}
import silhouette.repositories.PermissionsRepository
import silhouette.{BuyerAuthorizations, DefaultEnv, Permission, ResourceType}

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Performance",
  produces = "application/json",
  consumes = "application/json")
class AnalyticsController(components: ControllerComponents,
                          silhouette: Silhouette[DefaultEnv],
                          val permissionsRepository: PermissionsRepository,
                          val settingsService: SettingsService,
                          druidService: DruidService)(implicit ec: ExecutionContext)
  extends AbstractController(components)
    with Circe
    with BuyerAuthorizations[DefaultEnv]
    with CirceModelsInstances {

  implicit val customPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  // Endpoint for dashboard:
  @ApiOperation(value = "Query")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "com.appodealx.exchange.druid.transport.models.PerformanceDTO")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Result", response = classOf[ExportDTO], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized"),
    new ApiResponse(code = 400, message = "Interval expected")))
  def performance: Action[PerformanceDTO] = silhouette.SecuredAction(WithRoleAdmin || WithRole(Buyer)).async(circe.json[PerformanceDTO]) { request =>

    val userIdOpt = request.identity.id

    val permissionFut: Future[Option[Permission]] = userIdOpt.map(permissionsRepository.retrieve(_, ResourceType.Agency)).getOrElse(Future.successful(None))

    def invoke(dto: PerformanceDTO) = druidService.performance.invoke(dto)

    val userRole = request.identity.role

    if (userRole == Admin) {
      invoke(request.body)
        .map { seq =>
          Ok(seq.asJson.pretty(Printer.noSpaces.copy(dropNullValues = true))).as(ContentTypes.JSON)
        }
    } else if (userRole == Buyer) {
      permissionFut.flatMap { permissionOpt =>
        permissionOpt
          .map { p =>
            val dto = request.body.copy(agencyId = Some(p.resourceId))
            invoke(dto)
              .map { seq =>
                // Remove fields 'for admin only'
                val noneFilledSeq = seq.map(_.copy(
                  lostImpressions = None,
                  lostImpressionsRevenue = None,
                  exchangeFee = None,
                  sspIncome = None,
                  finishes = None
                ))
                Ok(noneFilledSeq.asJson.pretty(Printer.noSpaces.copy(dropNullValues = true))).as(ContentTypes.JSON)
              }
          }
          .getOrElse(Future.failed(new AccessDeniedException(s"No permissions for agency with id:${userIdOpt.getOrElse("None")}")))
      }
    } else {
      Future.failed(new AccessDeniedException(s"No permission for role ${userRole.value}"))
    }

  }

  @ApiOperation(value = "Get all AdTypes")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Ad Types", response = classOf[DictionaryItemDTO], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def adTypes: Action[AnyContent] = silhouette.SecuredAction(WithRoleAdmin || WithRole(Buyer)).async {
    druidService.adTypes.invoke().map(seq => Ok(seq.asJson))
  }

  @ApiOperation(value = "Get all platforms")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Platforms", response = classOf[DictionaryItemDTO], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def platforms: Action[AnyContent] = silhouette.SecuredAction(WithRoleAdmin || WithRole(Buyer)).async {
    druidService.platforms.invoke().map(seq => Ok(seq.asJson))
  }

  @ApiOperation(value = "Get all Countries")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "date", value = "Query", required = false, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "start", value = "Query", required = false, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "end", value = "Query", required = false, dataType = "string", paramType = "query")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Countries", response = classOf[DictionaryItemDTO], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized"),
    new ApiResponse(code = 400, message = "Date or Start and End date expected")))
  def countries: Action[AnyContent] = silhouette.SecuredAction(WithRoleAdmin || WithRole(Buyer)).async { implicit request =>
    val date = request.getQueryString("date")
    val start = request.getQueryString("start")
    val end = request.getQueryString("end")
    druidService.countries(date, start, end).invoke().map(seq => Ok(seq.asJson).as(ContentTypes.JSON))
  }

  @ApiOperation(value = "Get agencies")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "date", value = "Query", required = false, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "start", value = "Query", required = false, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "end", value = "Query", required = false, dataType = "string", paramType = "query")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Agencies", response = classOf[DictionaryItemDTO], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized"),
    new ApiResponse(code = 400, message = "Date or Start and End date expected")))
  def agencies: Action[AnyContent] = silhouette.SecuredAction(WithRoleAdmin || WithRole(Buyer)).async { implicit request =>
    // For admin only
    if (request.identity.role == Admin) {
      val date = request.getQueryString("date")
      val start = request.getQueryString("start")
      val end = request.getQueryString("end")
      druidService.agencies(date, start, end).invoke().map(seq => Ok(seq.asJson).as(ContentTypes.JSON))
    } else {
      Future.failed(new AccessDeniedException(s"Only for admin role."))
    }

  }

  @ApiOperation(value = "Get sellers")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "date", value = "Query", required = false, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "start", value = "Query", required = false, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "end", value = "Query", required = false, dataType = "string", paramType = "query")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Sellers", response = classOf[DictionaryItemDTO], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized"),
    new ApiResponse(code = 400, message = "Date or Start and End date expected")))
  def sellers: Action[AnyContent] = silhouette.SecuredAction(WithRoleAdmin || WithRole(Buyer)).async { implicit request =>
    val date = request.getQueryString("date")
    val start = request.getQueryString("start")
    val end = request.getQueryString("end")
    druidService.sellers(date, start, end).invoke().map(seq => Ok(seq.asJson).as(ContentTypes.JSON))
  }

}
