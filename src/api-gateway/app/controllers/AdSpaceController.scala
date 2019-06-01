package controllers

import com.appodealx.exchange.settings.SettingsService
import com.appodealx.exchange.settings.models.circe.CirceSellerSettingsInstances
import com.appodealx.exchange.settings.models.seller.{AdSpaceId, BannerAdSpace, NativeAdSpace, VideoAdSpace}
import com.mohiva.play.silhouette.api.Silhouette
import io.circe.Printer
import io.circe.syntax._
import io.swagger.annotations._
import models.swagger
import monix.execution.Scheduler
import play.api.libs.circe.Circe
import play.api.mvc._
import silhouette.UserRole.Seller
import silhouette.repositories.PermissionsRepository
import silhouette.{DefaultEnv, SellerAuthorizations}


@Api(value = "AdSpaces",
  produces = "application/json",
  consumes = "application/json")
class AdSpaceController(components: ControllerComponents,
                        silhouette: Silhouette[DefaultEnv],
                        val settingsService: SettingsService,
                        val permissionsRepository: PermissionsRepository)(implicit val scheduler: Scheduler)
  extends AbstractController(components)
    with Circe
    with CirceSellerSettingsInstances
    with SellerAuthorizations[DefaultEnv] {


  implicit val customPrinter = Printer.noSpaces.copy(dropNullValues = true)

  @ApiOperation(value = "Create BannerAdSpace with SellerId")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.BannerAdSpace")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Created BannerAdSpace", response = classOf[swagger.BannerAdSpace]),
    new ApiResponse(code = 401, message = "not authorized")))
  def createBannerAdSpaceWithSellerId(@ApiParam(value = "ID of the seller") sellerId: Long) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSeller(sellerId)).async(circe.json[BannerAdSpace]) { request =>
      settingsService
        .createBannerAdSpaceWithSellerId(sellerId)
        .invoke(request.body)
        .map(seq => Ok(seq.asJson))
    }

  @ApiOperation(value = "Read BannerAdSpace by SellerId")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "BannerAdSpaces", response = classOf[swagger.BannerAdSpace], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readBannerBySellerId(@ApiParam(value = "ID of the seller") sellerId: Long) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSeller(sellerId)).async {
      settingsService
        .readBannerBySellerId(sellerId)
        .invoke()
        .map(seq => Ok(seq.asJson))
    }

  @ApiOperation(value = "Update BannerAdSpace")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.BannerAdSpace")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Updated BannerAdSpace", response = classOf[swagger.BannerAdSpace]),
    new ApiResponse(code = 401, message = "not authorized")))
  def updateBannerAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfBannerAdSpace(id.value)).async(circe.json[BannerAdSpace]) { request =>
      settingsService
        .updateBannerAdSpace(id.value)
        .invoke(request.body)
        .map(adSpace => Ok(adSpace.asJson))
    }

  @ApiOperation(value = "Activate BannerAdSpace")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Activated"),
    new ApiResponse(code = 404, message = "AdSpace not found"),
    new ApiResponse(code = 401, message = "not authorized")))
  def activateBannerAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfBannerAdSpace(id.value)).async {
      settingsService
        .activateBannerAdSpace(id.value)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Deactivate BannerAdSpace")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deactivated"),
    new ApiResponse(code = 404, message = "AdSpace not found"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deactivateBannerAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfBannerAdSpace(id.value)).async {
      settingsService
        .deactivateBannerAdSpace(id.value)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Delete BannerAdSpace")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 404, message = "AdSpace not found"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deleteBannerAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfBannerAdSpace(id.value)).async {
      settingsService
        .deleteBannerAdSpace(id.value)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Create VideoAdSpace with SellerId")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.VideoAdSpace")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Created VideoAdSpace", response = classOf[swagger.VideoAdSpace]),
    new ApiResponse(code = 401, message = "not authorized")))
  def createVideoAdSpaceWithSellerId(@ApiParam(value = "ID of the Seller") sellerId: Long) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSeller(sellerId)).async(circe.json[VideoAdSpace]) { request =>
      settingsService
        .createVideoAdSpaceWithSellerId(sellerId)
        .invoke(request.body)
        .map(seq => Ok(seq.asJson))
    }

  @ApiOperation(value = "Read VideoAdSpace by SellerId")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "VedeoAdSpaces", response = classOf[swagger.VideoAdSpace], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readVideoAdSpaceBySellerId(@ApiParam(value = "ID of the Seller") sellerId: Long) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSeller(sellerId)).async {
      settingsService
        .readVideoAdSpaceBySellerId(sellerId)
        .invoke()
        .map(seq => Ok(seq.asJson))
    }

  @ApiOperation(value = "Update VideoAdSpace")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.VideoAdSpace")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Updated VideoAdSpace", response = classOf[swagger.VideoAdSpace]),
    new ApiResponse(code = 401, message = "not authorized")))
  def updateVideoAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfVideoAdSpace(id.value)).async(circe.json[VideoAdSpace]) { request =>
      settingsService
        .updateVideoAdSpace(id.value)
        .invoke(request.body)
        .map(adSpace => Ok(adSpace.asJson))
    }

  @ApiOperation(value = "Activate VideoAdSpace")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Activated"),
    new ApiResponse(code = 404, message = "AdSpace not found"),
    new ApiResponse(code = 401, message = "not authorized")))
  def activateVideoAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId)=
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfVideoAdSpace(id.value)).async {
      settingsService
        .activateVideoAdSpace(id.value)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Deactivate VideoAdSpace")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deactivated"),
    new ApiResponse(code = 404, message = "AdSpace not found"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deactivateVideoAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfVideoAdSpace(id.value)).async {
      settingsService
        .deactivateVideoAdSpace(id.value)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Delete VideoAdSpace")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 404, message = "AdSpace not found"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deleteVideoAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfVideoAdSpace(id.value)).async {
      settingsService
        .deleteVideoAdSpace(id.value)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Create NativeAdSpace with SellerId")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.NativeAdSpace")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Created NativeAdSpace", response = classOf[swagger.NativeAdSpace]),
    new ApiResponse(code = 401, message = "not authorized")))
  def createNativeAdSpaceWithSellerId(@ApiParam(value = "ID of the Seller") sellerId: Long) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSeller(sellerId)).async(circe.json[NativeAdSpace]) { request =>
      settingsService
        .createNativeAdSpaceWithSellerId(sellerId)
        .invoke(request.body)
        .map(seq => Ok(seq.asJson))
    }

  @ApiOperation(value = "Read NativeAdSpace by SellerId")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "NativeAdSpaces", response = classOf[swagger.NativeAdSpace], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readNativeBySellerId(@ApiParam(value = "ID of the Seller") sellerId: Long) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSeller(sellerId)).async {
      settingsService
        .readNativeBySellerId(sellerId)
        .invoke()
        .map(seq => Ok(seq.asJson))
    }

  @ApiOperation(value = "Update NativeAdSpace")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.NativeAdSpace")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Updated NativeAdSpace", response = classOf[swagger.NativeAdSpace]),
    new ApiResponse(code = 401, message = "not authorized")))
  def updateNativeAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfNativeAdSpace(id.value)).async(circe.json[NativeAdSpace]) { request =>
      settingsService
        .updateNativeAdSpace(id.value)
        .invoke(request.body)
        .map(adSpace => Ok(adSpace.asJson))
    }

  @ApiOperation(value = "Activate NativeAdSpace")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Activated"),
    new ApiResponse(code = 404, message = "AdSpace not found"),
    new ApiResponse(code = 401, message = "not authorized")))
  def activateNativeAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId)=
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfNativeAdSpace(id.value)).async {
      settingsService
        .activateNativeAdSpace(id.value)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Deactivate NativeAdSpace")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deactivated"),
    new ApiResponse(code = 404, message = "AdSpace not found"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deactivateNativeAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfNativeAdSpace(id.value)).async {
      settingsService
        .deactivateNativeAdSpace(id.value)
        .invoke()
        .map(_ => NoContent)
    }

  @ApiOperation(value = "Delete NativeAdSpace")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 404, message = "AdSpace not found"),
    new ApiResponse(code = 401, message = "not authorized")))
  def deleteNativeAdSpace(@ApiParam(value = "ID of the AdSpace") id: AdSpaceId) =
    silhouette.SecuredAction(WithRoleAdmin || WithRole(Seller) && WithSellerOfNativeAdSpace(id.value)).async {
      settingsService
        .deleteNativeAdSpace(id.value)
        .invoke()
        .map(_ => NoContent)
    }
}
