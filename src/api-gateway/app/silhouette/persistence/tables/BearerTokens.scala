package silhouette.persistence.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import org.joda.time.DateTime

import scala.concurrent.duration.FiniteDuration

class BearerTokens(tag: Tag) extends Table[BearerTokenAuthenticator](tag, "bearer_tokens") {

  def id = column[String]("id", O.PrimaryKey)
  def providerID = column[String]("provider_id")
  def providerKey = column[String]("provider_key")
  def lastUsed = column[DateTime]("last_used")
  def expiration = column[DateTime]("expiration")
  def idleTimeout = column[Option[FiniteDuration]]("idle_timeout")

  def loginInfo = (providerID, providerKey) <> (LoginInfo.tupled, LoginInfo.unapply)

  def * = (id,
    loginInfo,
    lastUsed,
    expiration,
    idleTimeout) <> (BearerTokenAuthenticator.tupled, BearerTokenAuthenticator.unapply)
}

object BearerTokens extends TableQuery(new BearerTokens(_))
