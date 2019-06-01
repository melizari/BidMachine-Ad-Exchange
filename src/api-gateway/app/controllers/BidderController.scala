package controllers

import com.appodealx.exchange.common.models.auction.{Bidder, BidderId}
import com.appodealx.exchange.settings.SettingsService
import com.appodealx.exchange.settings.models.buyer.{BannerAdProfile, NativeAdProfile, VideoAdProfile}
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

@Api(value = "Bidder",
  produces = "application/json",
  consumes = "application/json")
class BidderController(val permissionsRepository: PermissionsRepository,
                       val settingsService: SettingsService,
                       silhouette: Silhouette[DefaultEnv],
                       cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with CirceBuyerSettingsInstances
    with Circe
    with BuyerAuthorizations[DefaultEnv] {

  @ApiOperation(value = "Get all bidders", tags = Array("Bidders"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "All bidders", response = classOf[swagger.Bidder], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readAllBidders() = silhouette.SecuredAction(WithRoleAdmin || WithRole(Buyer)).async { request =>

    request.identity.role match {
      case UserRole.Admin =>
        settingsService
          .readAllBidders
          .invoke()
          .map(seq => Ok(seq.asJson))

      case UserRole.Buyer =>
        request.identity.permissions
          .find(p => p.resourceType == ResourceType.Agency)
          .map(_.resourceId) match {
            case Some(agencyId) => settingsService
              .readBiddersByAgencyId(agencyId)
              .invoke()
              .map(seq => Ok(seq.toVector.asJson))
            case None => Future.successful(Ok)
        }

      case _ => Future.successful(Forbidden)
    }
  }

  @ApiOperation(value = "Get bidder by ID", tags = Array("Bidders"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Bidder", response = classOf[swagger.Bidder]),
    new ApiResponse(code = 401, message = "not authorized")))
  def readBidder(@ApiParam(value = "ID of the bidder to fetch") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBidder(id))).async {
      settingsService
        .readBidder(id)
        .invoke()
        .map(bidder => Ok(bidder.asJson))
    }

  @ApiOperation(value = "Update bidder with ID", tags = Array("Bidders"))
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.Bidder")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Updated bidder", response = classOf[swagger.Bidder]),
    new ApiResponse(code = 401, message = "not authorized")))
  def updateBidder(@ApiParam(value = "ID of the bidder to update") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBidder(id)))(circe.json[Bidder]).async { request =>
      val bidder = request.identity.role match {
        case UserRole.Buyer =>
          settingsService
            .readBidder(id)
            .invoke()
            .map { b =>
              request.body.copy(title = b.title, adControl = b.adControl)
            }
        case _ =>
          settingsService
            .readBidder(id)
            .invoke()
      }

      bidder.flatMap(b => settingsService.updateBidder(id).invoke(b).map(bidder => Ok(bidder.asJson)))
    }

  @ApiOperation(value = "Delete bidder by ID", tags = Array("Bidders"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deleteBidder(@ApiParam(value = "ID of the bidder to delete") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin).async {
      settingsService
        .deleteBidder(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Get bannerAdProfiles by bidder ID", tags = Array("Bidders", "AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "BannerAdProfile", response = classOf[swagger.BannerAdProfile], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readBannerAdProfiles(@ApiParam(value = "ID of the bidder to fetch") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBidder(id))).async {
      settingsService
        .readBannerAdProfiles(id)
        .invoke()
        .map(seq => Ok(seq.asJson))
    }

  @ApiOperation(value = "Get videoAdProfiles by bidder ID", tags = Array("Bidders", "AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "VideoAdProfile", response = classOf[swagger.VideoAdProfile], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readVideoAdProfiles(@ApiParam(value = "ID of the bidder to fetch") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBidder(id))).async {
      settingsService
        .readVideoAdProfiles(id)
        .invoke()
        .map(seq => Ok(seq.asJson))
    }

  @ApiOperation(value = "Get nativeAdProfiles by bidder ID", tags = Array("Bidders", "AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "NativeAdProfile", response = classOf[swagger.NativeAdProfile], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readNativeAdProfiles(@ApiParam(value = "ID of the bidder to fetch") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBidder(id))).async {
      settingsService
        .readNativeAdProfiles(id)
        .invoke()
        .map(seq => Ok(seq.asJson))
    }

  @ApiOperation(value = "Create BannerAdProfile", tags = Array("Bidders", "AdProfiles"))
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.BannerAdProfile")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Created BannerAdProfile", response = classOf[swagger.BannerAdProfile]),
    new ApiResponse(code = 401, message = "not authorized")))
  def createBannerAdProfile(bidderId: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBidder(bidderId)))(circe.json[BannerAdProfile]).async { request =>
      settingsService
        .createBannerAdProfile
        .invoke(request.body.copy(bidderId = BidderId(bidderId)))
        .map(bannerAdProfile => Ok(bannerAdProfile.asJson))
    }

  @ApiOperation(value = "Create VideoAdProfile", tags = Array("Bidders", "AdProfiles"))
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.VideoAdProfile")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Created VideoAdProfile", response = classOf[swagger.VideoAdProfile]),
    new ApiResponse(code = 401, message = "not authorized")))
  def createVideoAdProfile(bidderId: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBidder(bidderId)))(circe.json[VideoAdProfile]).async { request =>
      settingsService
        .createVideoAdProfile
        .invoke(request.body.copy(bidderId = BidderId(bidderId)))
        .map(videoAdProfile => Ok(videoAdProfile.asJson))
    }

  @ApiOperation(value = "Create NativeAdProfile", tags = Array("Bidders", "AdProfiles"))
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.NativeAdProfile")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Created NativeAdProfile", response = classOf[swagger.NativeAdProfile]),
    new ApiResponse(code = 401, message = "not authorized")))
  def createNativeAdProfile(bidderId: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBidder(bidderId)))(circe.json[NativeAdProfile]).async { request =>
      settingsService
        .createNativeAdProfile
        .invoke(request.body.copy(bidderId = BidderId(bidderId)))
        .map(nativeAdProfile => Ok(nativeAdProfile.asJson))
    }
}
