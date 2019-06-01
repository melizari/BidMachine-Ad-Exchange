package controllers

import com.appodealx.exchange.settings.SettingsService
import com.appodealx.exchange.settings.models.seller.Seller
import com.mohiva.play.silhouette.api.Silhouette
import io.swagger.annotations._
import models.swagger
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import silhouette.UserRole.Buyer
import silhouette.repositories.PermissionsRepository
import silhouette.{Authorizations, DefaultEnv, UserRole}

import scala.concurrent.ExecutionContext


@Api(value = "Seller",
  produces = "application/json",
  consumes = "application/json")
class SellersController(settingsService: SettingsService,
                        val permissionsRepository: PermissionsRepository,
                        silhouette: Silhouette[DefaultEnv],
                        cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with Authorizations[DefaultEnv] {

  @ApiOperation(value = "Get all Sellers")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "All sellers", response = classOf[swagger.Seller], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def readAll = silhouette.SecuredAction(WithRoleAdmin || WithRole(Buyer)).async {
    settingsService
      .findAllSellers
      .invoke()
      .map(sellers => Ok(Json.toJson(sellers)))
  }

  @ApiOperation(value = "Get seller by ID")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Seller", response = classOf[swagger.Seller]),
    new ApiResponse(code = 401, message = "not authorized")))
  def seller(@ApiParam(value = "ID of the seller to get") id: Long) = silhouette.SecuredAction(WithRole(UserRole.Admin) || WithRole(Buyer)).async {
    settingsService
      .findSeller(id)
      .invoke()
      .map(seller => Ok(Json.toJson(seller)))
  }

  @ApiOperation(value = "Create Seller")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.Seller")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Created Seller", response = classOf[swagger.Seller]),
    new ApiResponse(code = 401, message = "not authorized")))
  def create = silhouette.SecuredAction(WithRole(UserRole.Admin))(parse.json[Seller]).async { request =>
    settingsService
      .insertSeller
      .invoke(request.body)
      .map(seller => Ok(Json.toJson(seller)))
  }

  @ApiOperation(value = "Update seller with ID")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.Seller")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Updated seller", response = classOf[swagger.Seller]),
    new ApiResponse(code = 401, message = "not authorized")))
  def update(@ApiParam(value = "ID of the seller to update") id: Long) = silhouette.SecuredAction(WithRoleAdmin)(parse.json[Seller]).async { request =>
    settingsService
      .updateSeller(id)
      .invoke(request.body)
      .map(seller => Ok(Json.toJson(seller)))
  }

  @ApiOperation(value = "Delete seller by ID")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 401, message = "not authorized")))
  def delete(@ApiParam(value = "ID of the seller to delete") id: Long) = silhouette.SecuredAction(WithRole(UserRole.Admin)).async {
    settingsService
      .deleteSeller(id)
      .invoke()
      .map(_ => NoContent)
  }

}
