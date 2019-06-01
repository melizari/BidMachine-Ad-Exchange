package silhouette.persistence.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import io.circe.Json
import org.joda.time.DateTime
import play.api.libs.json.JsObject

import scala.concurrent.duration.FiniteDuration


class JWTTokens(tag: Tag) extends Table[JWTAuthenticator](tag, "jwt_tokens") {

  import com.appodealx.exchange.common.utils.json.FoldableJsValue


  implicit val jsColumnType = MappedColumnType.base[JsObject, Json](
    _.foldWith(com.appodealx.exchange.common.utils.json.CirceJsonFolder),
    _.foldWith(com.appodealx.exchange.common.utils.json.PlayJsonFolder).as[JsObject]
  )

  def id = column[String]("id", O.PrimaryKey)
  def providerID = column[String]("provider_id")
  def providerKey = column[String]("provider_key")
  def lastUsed = column[DateTime]("last_used")
  def expiration = column[DateTime]("expiration")
  def idleTimeout = column[Option[FiniteDuration]]("idle_timeout")
  def customClaims = column[Option[JsObject]]("custom_claims")

  def loginInfo = (providerID, providerKey) <> (LoginInfo.tupled, LoginInfo.unapply)


  override def * = (id,
    loginInfo,
    lastUsed,
    expiration,
    idleTimeout,
    customClaims) <> ((JWTAuthenticator.apply _).tupled, JWTAuthenticator.unapply)
}

object JWTTokens extends TableQuery(new JWTTokens(_))