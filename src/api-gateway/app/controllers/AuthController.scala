package controllers

import cats.data.OptionT
import cats.instances.future._
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.providers.BasicAuthProvider
import io.swagger.annotations._
import models.swagger
import play.api.libs.circe.Circe
import play.api.libs.json.JodaWrites.JodaDateTimeWrites
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import silhouette.SellerEnvBearer

import scala.concurrent.ExecutionContext

@Api(value = "Auth",
  produces = "application/json",
  consumes = "application/json")
class AuthController(basic: BasicAuthProvider,
                     silhouette: Silhouette[SellerEnvBearer],
                     cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) with CirceBuyerSettingsInstances with Circe {
  @ApiOperation(value = "Sign in")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Authorization success", response = classOf[swagger.Auth]),
    new ApiResponse(code = 401, message = "not authorized")))
  def signIn = Action.async { implicit request =>
    { for {
      loginInfo <- OptionT(basic.authenticate(request))
      authenticator <- OptionT.liftF(silhouette.env.authenticatorService.create(loginInfo))
      token <- OptionT.liftF(silhouette.env.authenticatorService.init(authenticator))
    } yield Ok(Json.obj("token" -> token, "expires" -> authenticator.expirationDateTime)) }
      .getOrElse(Unauthorized.withHeaders(WWW_AUTHENTICATE -> "Basic"))
  }

}
