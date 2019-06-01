package services

import akka.http.scaladsl.model.Uri
import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.models.Failure
import com.appodealx.exchange.common.models.FailureReason.AppDecodingFailure
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.settings.models.buyer.AppodealResponseStatus
import com.appodealx.exchange.settings.models.circe.{CirceBuyerSettingsInstances, CirceExternalInstances}
import com.appodealx.openrtb
import io.circe.parser._
import play.api.http.{ContentTypes, HeaderNames, Status}
import play.api.libs.ws.{WSAuthScheme, WSClient}
import play.api.{Configuration, Logger}
import scalacache.Mode
import scalacache.caffeine.CaffeineCache
import scalacache.redis.{RedisCache, RedisSerialization}

import cats.MonadError
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._

import scala.concurrent.duration.{Duration, MILLISECONDS}

class AppodealAppRepo[F[_]: Execute: Mode](ws: WSClient,
                                           configuration: Configuration,
                                           localCache: CaffeineCache[openrtb.App],
                                           redisCache: RedisCache[openrtb.App])(implicit M: MonadError[F, Throwable])
    extends AppRepo[F]
    with CirceModelsInstances
    with CirceBuyerSettingsInstances
    with CirceExternalInstances
    with RedisSerialization {


  private val F      = MonadError[F, Throwable]
  private val logger = Logger(getClass)

  private val User     = configuration.getOptional[String]("external.service.appodeal.user").getOrElse("")
  private val Password = configuration.getOptional[String]("external.service.appodeal.password").getOrElse("")
  private val CacheTTL = Duration(configuration.get[String]("external.app.service.cache.ttl")).some

  private val secure = true

  private val ApiHost     = Uri.Host(configuration.get[String]("external.domain"))
  private val ApiPath     = Uri.Path(configuration.get[String]("external.app.service.url"))
  private val ApiEndpoint = Uri(Uri.httpScheme(secure), Uri.Authority(ApiHost), ApiPath)

  private val AuthHeader = configuration.get[String]("external.service.appodeal.key")
  private val AuthSecret = configuration.get[String]("external.service.appodeal.secret")

  private val ApiTimeout =
    Duration.create(configuration.get[Long]("external.app.service.request.timeout.ms"), MILLISECONDS)

  private def errorMessage(status: Int, message: String = "undefined", appId: String = "undefined") =
    s"API response with status: $status, message: $message, app id: $appId"

  private def decodeError[T](body: String, appId: String) =
    decode[AppodealResponseStatus](body).map { res =>
      Failure(AppDecodingFailure, errorMessage(res.status, res.error.getOrElse("undefined"), appId))
    }.merge

  def findAppInKs(ks: String, appEid: String) = {
    import HeaderNames._
    import WSAuthScheme._

    val key = s"apps/v4/$ks/$appEid"
    val uri = ApiEndpoint.withPath(ApiEndpoint.path + appEid)

    def wsRequestFuture =
      ws.url(uri.toString)
        .withAuth(User, Password, BASIC)
        .addHttpHeaders(ACCEPT -> ContentTypes.JSON, AuthHeader -> AuthSecret)
        .withRequestTimeout(ApiTimeout)
        .get

    val fromApi = Execute[F].deferFuture(wsRequestFuture).flatMap {
      case res if res.status == Status.OK && res.body.nonEmpty =>
        decode[openrtb.App](res.body)
          .filterOrElse(_.id.isDefined, decodeError(res.body, appEid))
          .liftTo[F]

      case res =>
        logger.error(errorMessage(status = res.status, appId = appEid))
        F.raiseError[openrtb.App](Failure(AppDecodingFailure, errorMessage(res.status, res.statusText)))
    }

    val fromRedis = redisCache.cachingF(key)(CacheTTL)(fromApi)
    val fromCache = localCache.cachingF(key)(CacheTTL)(fromRedis)

    fromCache.map(Option.apply)
  }
}
