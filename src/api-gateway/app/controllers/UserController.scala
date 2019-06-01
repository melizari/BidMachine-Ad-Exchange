package controllers

import cats.data.OptionT
import cats.instances.future._
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses, _}
import models._
import org.joda.time.DateTime
import play.api.libs.circe.Circe
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import silhouette.repositories.{UserIdentityService, UserLoginsRepository}
import silhouette.{DefaultEnv, Password}
import utils.mail.{MandrillMailer, ResetPasswordLinkBuilder}

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Users",
  produces = "application/json",
  consumes = "application/json")
class UserController(cc: ControllerComponents,
                     userRepository: UserIdentityService,
                     loginRepository: UserLoginsRepository,
                     silhouette: Silhouette[DefaultEnv],
                     credentialsProvider: CredentialsProvider,
                     authInfoRepository: AuthInfoRepository,
                     linkBuilder: ResetPasswordLinkBuilder,
                     hasherRegistry: PasswordHasherRegistry,
                     mandrillMailer: MandrillMailer)(implicit ec: ExecutionContext)
  extends AbstractController(cc)  with CirceBuyerSettingsInstances with Circe {

  @ApiOperation(value = "Log in by user and password")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.LoginPass")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Authorization success", response = classOf[swagger.AuthJWT]),
    new ApiResponse(code = 401, message = "Invalid credentials")))
  def login = Action.async(circe.json[LoginPass]) { implicit request =>
    val credentials = Credentials(request.body.login, request.body.password)

    { for {
      loginInfo <- OptionT.liftF(credentialsProvider.authenticate(credentials))
      identity <- OptionT(silhouette.env.identityService.retrieve(loginInfo))
      authenticator <- OptionT.liftF(silhouette.env.authenticatorService.create(loginInfo))
      token <- OptionT.liftF(silhouette.env.authenticatorService.init(authenticator))
    } yield Json.obj("token" -> token, "role" -> identity.role.value)
    }.fold[Result](Unauthorized)(Ok(_))
  }

  @ApiOperation(value = "Get self")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "self user", response = classOf[swagger.User]),
    new ApiResponse(code = 401, message = "not authorized")))
  def me = silhouette.SecuredAction { request =>
    Ok(Json.toJson(request.identity))
  }

  @ApiOperation(value = "Update self password")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "silhouette.Password")))
  def updatePassword = silhouette.SecuredAction(parse.json[Password]).async { request =>
    val password = request.body

    if (password.next.length < 5) {
      throw HttpError(400, "Password must be at least 5 characters long")
    }

    val nextInfo = hasherRegistry.current.hash(password.next)
    val credentials = Credentials(request.authenticator.loginInfo.providerKey, password.current)

    for {
      loginInfo <- credentialsProvider.authenticate(credentials)
      _ <- authInfoRepository.update(loginInfo, nextInfo)
    } yield Ok
  }

  @ApiOperation(value = "Get link for password recovery")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.Email")))
  def resetPassword= Action.async(parse.json[Email]) { implicit request =>
    val email = request.body.email
    val loginInfo = LoginInfo(CredentialsProvider.ID, email)
    val recoveryLink = linkBuilder.getLink(email, DateTime.now.plusHours(2).getMillis)

    { for {
      u <- OptionT(userRepository.retrieve(loginInfo))
      _ <- OptionT.liftF(mandrillMailer.forgotPassword(email, u.name.getOrElse(""), u.company.getOrElse(""), recoveryLink))
    } yield Ok }.getOrElse(throw HttpError(code = 400, message = "Email not found"))

  }

  @ApiOperation(value = "Renew password with email data")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.PasswordReset")))
  def renewPassword = Action.async(parse.json[PasswordReset]) { request =>
    val PasswordReset(password, email, expiresAt, signature) = request.body
    val isGenuine = linkBuilder.verify(email, expiresAt, signature)
    val notExpired = expiresAt > DateTime.now.getMillis

    if (isGenuine && notExpired) {
      if (password.length < 5) {
        throw HttpError(400, "Password must be at least 5 characters long")
      }

      val loginInfo = LoginInfo(CredentialsProvider.ID, email)
      val passwordInfo = hasherRegistry.current.hash(password)

      authInfoRepository.save(loginInfo, passwordInfo).map { _ => Ok }
    } else {
      Future.failed(HttpError(400, "Password restoration link is expired or invalid"))
    }
  }
}
