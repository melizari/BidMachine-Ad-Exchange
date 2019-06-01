package silhouette

import com.mohiva.play.silhouette.api.Identity

case class Account[R](user: User, resource: R) extends Identity