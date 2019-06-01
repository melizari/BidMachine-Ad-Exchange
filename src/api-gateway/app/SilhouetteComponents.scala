
import com.appodealx.exchange.common.db.PostgresProfile
import com.appodealx.exchange.settings.SettingsService
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.crypto.Base64AuthenticatorEncoder
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api.{Environment, EventBus, SilhouetteProvider}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.softwaremill.macwire.wire
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.{BodyParsers, PlayBodyParsers}
import silhouette.repositories._
import silhouette.{CredentialsBasicAuthProvider, DefaultEnv, SellerEnvBearer, SellerEnvDummy}
import slick.basic.DatabaseConfig

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

trait SilhouetteComponents
  extends SecuredActionComponents
    with UnsecuredActionComponents
    with UserAwareActionComponents {

  implicit def executionContext: ExecutionContext

  def dbConfig: DatabaseConfig[PostgresProfile]
  def playBodyParsers: PlayBodyParsers
  def messagesApi: MessagesApi
  def settingsService: SettingsService

  def configuration: Configuration

  lazy val hasherRegistry = PasswordHasherRegistry(new BCryptSha256PasswordHasher)
  lazy val authInfoRepository = new DelegableAuthInfoRepository(wire[PasswordsRepository])
  lazy val credentialsBasicAuthProvider: CredentialsBasicAuthProvider = wire[CredentialsBasicAuthProvider]


  lazy val clock = Clock()
  lazy val idGenerator = new SecureRandomIDGenerator

  lazy val userRepository: UserIdentityService = wire[UserIdentityService]
  lazy val userLoginsRepository: UserLoginsRepository = wire[UserLoginsRepository]
  lazy val sellersRepository = wire[SellersRepository]

  lazy val bearerTokenRepository = wire[BearerTokenRepository]


  lazy val accountsDao = wire[PermissionsRepository]

  lazy val bearerSettings = BearerTokenAuthenticatorSettings(
    fieldName = "Authorization",
    authenticatorExpiry = 365 days
  )

  lazy val sellerEnvDummy = Environment[SellerEnvDummy](
    wire[SellerIdentityService],
    wire[DummyAuthenticatorService],
    Seq(credentialsBasicAuthProvider),
    wire[EventBus])

  lazy val sellerEnvBearer = Environment[SellerEnvBearer](
    wire[SellerIdentityService],
    wire[BearerTokenAuthenticatorService],
    Seq(credentialsBasicAuthProvider),
    wire[EventBus]
  )

  lazy val securedErrorHandler = wire[DefaultSecuredErrorHandler]
  lazy val unsecuredErrorHandler = wire[DefaultUnsecuredErrorHandler]

  private lazy val defaultBodyParser: BodyParsers.Default = wire[BodyParsers.Default]

  lazy val securedBodyParser = defaultBodyParser
  lazy val unsecuredBodyParser = defaultBodyParser
  lazy val userAwareBodyParser = defaultBodyParser

  lazy val jWTAuthenticatorSettings = JWTAuthenticatorSettings(
    sharedSecret = configuration.get[String]("silhouette.authenticator.sharedSecret"),
    authenticatorExpiry = 30 days
  )
  lazy val jWTRepository = Some(wire[JWTRepository])
  lazy val credentialsProvider: CredentialsProvider = wire[CredentialsProvider]
  val encoder = new Base64AuthenticatorEncoder

  lazy val buyerEnv = Environment[DefaultEnv](
    userRepository,
    wire[JWTAuthenticatorService],
    Seq(credentialsBasicAuthProvider),
    wire[EventBus]
  )

  lazy val silhouetteSellerDummy = wire[SilhouetteProvider[SellerEnvDummy]]
  lazy val silhouetteSellerBearer = wire[SilhouetteProvider[SellerEnvBearer]]
  lazy val silhouetteBuyer = wire[SilhouetteProvider[DefaultEnv]]
}
