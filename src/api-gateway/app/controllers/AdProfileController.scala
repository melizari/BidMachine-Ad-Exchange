package controllers

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
import silhouette.{BuyerAuthorizations, DefaultEnv}

import scala.concurrent.ExecutionContext

@Api(value = "AdProfiles",
  produces = "application/json",
  consumes = "application/json")
class AdProfileController(val permissionsRepository: PermissionsRepository,
                          val settingsService: SettingsService,
                          silhouette: Silhouette[DefaultEnv],
                          cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with CirceBuyerSettingsInstances
    with Circe
    with BuyerAuthorizations[DefaultEnv] {

  @ApiOperation(value = "Get all BannerAdProfiles", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "All BannerAdProfiles", response = classOf[swagger.BannerAdProfileWithBidder], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readAllBannerAdProfiles = silhouette.SecuredAction(WithRoleAdmin).async {
    settingsService
      .readAllBannerAdProfiles
      .invoke()
      .map(seq => Ok(seq.asJson))
  }

  @ApiOperation(value = "Get BannerAdProfile by id", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "BannerAdProfile", response = classOf[swagger.BannerAdProfile]),
    new ApiResponse(code = 401, message = "not authorized")))
  def readBannerAdProfile(@ApiParam(value = "ID of the BannerAdProfile to fetch") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBannerAdProfile(id))).async {
      settingsService
        .readBannerAdProfile(id)
        .invoke()
        .map(bannerAdProfile => Ok(bannerAdProfile.asJson))
    }

  @ApiOperation(value = "Update BannerAdProfile with ID", tags = Array("AdProfiles"))
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.BannerAdProfile")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Updated BannerAdProfile", response = classOf[swagger.BannerAdProfile]),
    new ApiResponse(code = 401, message = "not authorized")))
  def updateBannerAdProfile(@ApiParam(value = "ID of the BannerAdProfile to update") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBannerAdProfile(id)))(circe.json[BannerAdProfile]).async { request =>
      settingsService
        .updateBannerAdProfile(id)
        .invoke(request.body)
        .map(bannerAdProfile => Ok(bannerAdProfile.asJson))
    }

  @ApiOperation(value = "Delete BannerAdProfile by ID", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deleteBannerAdProfile(@ApiParam(value = "ID of the BannerAdProfile to delete") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin).async {
      settingsService
        .deleteBannerAdProfile(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Activate BannerAdProfile with ID", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Activated"),
    new ApiResponse(code = 401, message = "not authorized")))
  def activateBannerAdProfile(@ApiParam(value = "ID of the BannerAdProfile to activate") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBannerAdProfile(id))).async {
      settingsService
        .activateBannerAdProfile(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Deactivate BannerAdProfile with ID", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deactivated"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deactivateBannerAdProfile(@ApiParam(value = "ID of the BannerAdProfile to deactivate") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithBannerAdProfile(id))).async {
      settingsService
        .deactivateBannerAdProfile(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Get all VideoAdProfiles", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "All VideoAdProfiles", response = classOf[swagger.VideoAdProfileWithBidder], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readAllVideoAdProfiles = silhouette.SecuredAction(WithRoleAdmin).async {
    settingsService
      .readAllVideoAdProfiles
      .invoke()
      .map(seq => Ok(seq.asJson))
  }

  @ApiOperation(value = "Get VideoAdProfile by id", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "VideoAdProfile", response = classOf[swagger.VideoAdProfile]),
    new ApiResponse(code = 401, message = "not authorized")))
  def readVideoAdProfile(@ApiParam(value = "ID of the VideoAdProfile to fetch") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithVideoAdProfile(id))).async {
      settingsService
        .readVideoAdProfile(id)
        .invoke()
        .map(videoAdProfile => Ok(videoAdProfile.asJson))
    }



  @ApiOperation(value = "Update VideoAdProfile with ID", tags = Array("AdProfiles"))
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.VideoAdProfile")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Updated VideoAdProfile", response = classOf[swagger.VideoAdProfile]),
    new ApiResponse(code = 401, message = "not authorized")))
  def updateVideoAdProfile(@ApiParam(value = "ID of the VideoAdProfile to update") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithVideoAdProfile(id)))(circe.json[VideoAdProfile]).async { request =>
      settingsService
        .updateVideoAdProfile(id)
        .invoke(request.body)
        .map(videoAdProfile => Ok(videoAdProfile.asJson))
    }

  @ApiOperation(value = "Delete VideoAdProfile by ID", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deleteVideoAdProfile(@ApiParam(value = "ID of the VideoAdProfile to delete") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin).async {
      settingsService
        .deleteVideoAdProfile(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Activate VideoAdProfile with ID", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Activated"),
    new ApiResponse(code = 401, message = "not authorized")))
  def activateVideoAdProfile(@ApiParam(value = "ID of the VideoAdProfile to activate") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithVideoAdProfile(id))).async {
      settingsService
        .activateVideoAdProfile(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Deactivate VideoAdProfile with ID", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deactivated"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deactivateVideoAdProfile(@ApiParam(value = "ID of the VideoAdProfile to deactivate") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) && WithVideoAdProfile(id))).async {
      settingsService
        .deactivateVideoAdProfile(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Get all NativeAdProfiles", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "All NativeAdProfiles", response = classOf[swagger.NativeAdProfileWithBidder], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readAllNativeAdProfiles = silhouette.SecuredAction(WithRoleAdmin).async {
    settingsService
      .readAllNativeAdProfiles
      .invoke()
      .map(seq => Ok(seq.asJson))
  }

  @ApiOperation(value = "Get NativeAdProfile by id", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "NativeAdProfile", response = classOf[swagger.NativeAdProfile]),
    new ApiResponse(code = 401, message = "not authorized")))
  def readNativeAdProfile(@ApiParam(value = "ID of the NativeAdProfile to fetch") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) &&WithNativeAdProfile(id))).async {
      settingsService
        .readNativeAdProfile(id)
        .invoke()
        .map(nativeAdProfile => Ok(nativeAdProfile.asJson))
    }

  @ApiOperation(value = "Update NativeAdProfile with ID", tags = Array("AdProfiles"))
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.NativeAdProfile")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Updated NativeAdProfile", response = classOf[swagger.NativeAdProfile]),
    new ApiResponse(code = 401, message = "not authorized")))
  def updateNativeAdProfile(@ApiParam(value = "ID of the NativeAdProfile to update") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) &&WithNativeAdProfile(id)))(circe.json[NativeAdProfile]).async { request =>
      settingsService
        .updateNativeAdProfile(id)
        .invoke(request.body)
        .map(bannerAdProfile => Ok(bannerAdProfile.asJson))
    }

  @ApiOperation(value = "Delete NativeAdProfile by ID", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deleteNativeAdProfile(@ApiParam(value = "ID of the NativeAdProfile to delete") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin).async {
      settingsService
        .deleteNativeAdProfile(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Activate NativeAdProfile with ID", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Activated"),
    new ApiResponse(code = 401, message = "not authorized")))
  def activateNativeAdProfile(@ApiParam(value = "ID of the NativeAdProfile to activate") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) &&WithNativeAdProfile(id))).async {
      settingsService
        .activateNativeAdProfile(id)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Deactivate NativeAdProfile with ID", tags = Array("AdProfiles"))
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deactivated"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deactivateNativeAdProfile(@ApiParam(value = "ID of the NativeAdProfile to deactivate") id: Long) =
    silhouette.SecuredAction(WithRoleAdmin || (WithRole(Buyer) &&WithNativeAdProfile(id))).async {
      settingsService
        .deactivateNativeAdProfile(id)
        .invoke()
        .map(_ => NoContent)
    }
}
