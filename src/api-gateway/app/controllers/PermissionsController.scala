package controllers

import com.mohiva.play.silhouette.api.Silhouette
import io.swagger.annotations._
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import silhouette.UserRole.Admin
import silhouette.repositories.PermissionsRepository
import silhouette.{Authorizations, DefaultEnv, Permission, ResourceType}

import scala.concurrent.ExecutionContext

@Api(value = "Permissions",
  produces = "application/json",
  consumes = "application/json")
class PermissionsController(val permissionsRepository: PermissionsRepository,
                            silhouette: Silhouette[DefaultEnv],
                            cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
  with Authorizations[DefaultEnv] {

  @ApiOperation(value = "Add permission")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.Permission")))
  @ApiResponses(Array(new ApiResponse(code = 201, message = "Success"),
    new ApiResponse(code = 401, message = "not authorized")))
  def create = silhouette.SecuredAction(WithRole(Admin))(parse.json[Permission]).async { request =>
    permissionsRepository.create(request.body).map(perm => Created(Json.toJson(perm)))
  }

  @ApiOperation(value = "Delete permission by userId and resourceType")
  @ApiResponses(Array(new ApiResponse(code = 204, message = "Deleted"),
    new ApiResponse(code = 401, message = "not authorized")))
  def delete(userId: Long, resourceType: ResourceType) = silhouette.SecuredAction(WithRole(Admin)).async {
    permissionsRepository
      .delete(userId, resourceType)
      .map(if (_) NoContent else NotFound)
  }

}
