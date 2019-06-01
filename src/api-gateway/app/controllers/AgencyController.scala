package controllers

import cats.data.OptionT
import cats.instances.future._
import com.appodealx.exchange.common.models.auction.{Agency, AgencyId, Bidder}
import com.appodealx.exchange.settings.SettingsService
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import com.mohiva.play.silhouette.api.Silhouette
import io.circe.syntax._
import io.swagger.annotations._
import models.swagger
import play.api.libs.circe.Circe
import play.api.mvc.{AbstractController, ControllerComponents}
import silhouette.UserRole.Buyer
import silhouette.repositories.PermissionsRepository
import silhouette.{BuyerAuthorizations, DefaultEnv, ResourceType, UserRole}

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Agencies",
  produces = "application/json",
  consumes = "application/json")
class AgencyController(val permissionsRepository: PermissionsRepository,
                       val settingsService: SettingsService,
                       silhouette: Silhouette[DefaultEnv],
                       cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with CirceBuyerSettingsInstances
    with Circe
    with BuyerAuthorizations[DefaultEnv] {

  @ApiOperation(value = "Get all agencies", tags = Array("Agencies"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "All agencies", response = classOf[swagger.Agency], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readAllAgencies() =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Buyer)).async { request =>
      request.identity.role match {
        case UserRole.Admin =>
          settingsService
            .readAllAgencies
            .invoke()
            .map(seq => Ok(seq.asJson))
        case UserRole.Buyer => {
          for {
            perm <- OptionT(permissionsRepository.retrieve(request.identity.id.get, ResourceType.Agency))
            agency <- OptionT.liftF(settingsService.readAgency(perm.resourceId).invoke())
          } yield agency
        }.value.map(seq => Ok(seq.toVector.asJson))

        case _ => Future.successful(Forbidden)
      }
    }

  @ApiOperation(value = "Get agency by id", tags = Array("Agencies"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Agency", response = classOf[swagger.Agency]),
    new ApiResponse(code = 401, message = "not authorized")))
  def readAgency(@ApiParam(value = "ID of the agency to fetch") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithAgency(id))).async {
      settingsService
        .readAgency(id)
        .invoke()
        .map(agency => Ok(agency.asJson))
    }

  @ApiOperation(value = "Create agency", tags = Array("Agencies"))
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.Agency")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Created agency", response = classOf[swagger.Agency]),
    new ApiResponse(code = 401, message = "not authorized")))
  def createAgency() =
    silhouette.SecuredAction(WithRoleAdmin)(circe.json[Agency]).async { request =>
      settingsService
        .createAgency
        .invoke(request.body)
        .map(agency => Ok(agency.asJson))
    }

  @ApiOperation(value = "Update agency with ID", tags = Array("Agencies"))
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.Agency")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Updated agency", response = classOf[swagger.Agency]),
    new ApiResponse(code = 401, message = "not authorized")))
  def updateAgency(@ApiParam(value = "ID of the agency to update") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithAgency(id)))(circe.json[Agency]).async { request =>

      val agency = request.identity.role match {
        case UserRole.Buyer =>
          settingsService
            .readAgency(id)
            .invoke()
            .map { a =>
              request.body.copy(title = a.title)
            }
        case _ =>
          Future.successful(request.body)
      }

      agency.flatMap { a =>
        settingsService
          .updateAgency(id)
          .invoke(a)
          .map(agency => Ok(agency.asJson))
      }
    }

  @ApiOperation(value = "Delete agency by ID", tags = Array("Agencies"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deleteAgency(@ApiParam(value = "ID of the agency to delete") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin).async {
      settingsService
        .deleteAgency(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Activate agency with ID", tags = Array("Agencies"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Activated"),
    new ApiResponse(code = 401, message = "not authorized")))
  def activateAgency(@ApiParam(value = "ID of the agency to activate") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin).async {
      settingsService
        .activateAgency(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Deactivate agency with ID", tags = Array("Agencies"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deactivated"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deactivateAgency(@ApiParam(value = "ID of the agency to deactivate") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin).async {
      settingsService
        .deactivateAgency(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Get bidders by agency ID", tags = Array("Agencies", "Bidders"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Bidder", response = classOf[swagger.Bidder], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readBiddersByAgencyId(@ApiParam(value = "ID of the agency to fetch") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithAgency(id))).async {
      settingsService
        .readBiddersByAgencyId(id)
        .invoke()
        .map(seq => Ok(seq.asJson))
    }

  @ApiOperation(value = "Create bidder", tags = Array("Agencies", "Bidders"))
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.Bidder")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Created bidder", response = classOf[swagger.Bidder]),
    new ApiResponse(code = 401, message = "not authorized")))
  def createBidder(agencyId: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithAgency(agencyId)))(circe.json[Bidder]).async { request =>
      settingsService
        .createBidder
        .invoke(request.body.copy(agencyId = AgencyId(agencyId)))
        .map(bidder => Ok(bidder.asJson))
    }

}
