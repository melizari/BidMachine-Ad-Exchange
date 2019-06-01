package silhouette

import play.api.libs.json.Json

case class Permission(userId: Long,
                      resourceId: Long,
                      resourceType: ResourceType)

object Permission {

  implicit val permissionFormat = Json.format[Permission]

}
