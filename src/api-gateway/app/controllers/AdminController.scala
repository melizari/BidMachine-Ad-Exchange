package controllers

import cats.data.OptionT
import cats.instances.future._
import com.appodealx.exchange.common.services.crypto.Signer
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import io.swagger.annotations.{Api, _}
import models.{BasicAuthPasswordUpdate, HttpError}
import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import silhouette._
import silhouette.repositories.{PermissionsRepository, UserIdentityService, UserLoginsRepository}
import utils.mail.{Mailer, ResetPasswordLinkBuilder}

import scala.concurrent.{ExecutionContext, Future}


@Api(value = "Admin",
  produces = "application/json",
  consumes = "application/json")
class AdminController(cc: ControllerComponents,
                      userRepository: UserIdentityService,
                      loginRepository: UserLoginsRepository,
                      silhouette: Silhouette[DefaultEnv],
                      authInfoRepository: AuthInfoRepository,
                      hasherRegistry: PasswordHasherRegistry,
                      val permissionsRepository: PermissionsRepository,
                      users: UserIdentityService,
                      mailer: Mailer,
                      signer: Signer,
                      prefix: String,
                      configuration: Configuration,
                      linkBuilder: ResetPasswordLinkBuilder)(implicit ec: ExecutionContext) extends AbstractController(cc) with Authorizations[DefaultEnv] {

  @ApiOperation(value = "Get all users")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "All users", response = classOf[_root_.silhouette.User], responseContainer = "Seq"),
    new ApiResponse(code = 401, message = "not authorized")))
  def getAllUsers = {
    silhouette.SecuredAction(WithRole(UserRole.Admin)).async {
      userRepository.findAll.map { seq => Ok(Json.toJson(seq)) }
    }
  }

  @ApiOperation(value = "Get user by id")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "User", response = classOf[_root_.silhouette.User]),
    new ApiResponse(code = 401, message = "not authorized"),
    new ApiResponse(code = 404, message = "user not found")))
  def user(@ApiParam(value = "ID of the user to fetch")id: Long) = silhouette.SecuredAction (WithRole(UserRole.Admin)) .async {
    userRepository
      .user(id).map {
      _.fold[Result](NotFound)(user => Ok(Json.toJson(user)))
    }
  }

  @ApiOperation(value = "Update user")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.User")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Updated user", response = classOf[_root_.silhouette.User]),
    new ApiResponse(code = 401, message = "not authorized"),
    new ApiResponse(code = 404, message = "user not found")))
  def updateUser(id: Long) = {
    silhouette.SecuredAction(WithRole(UserRole.Admin))(parse.json[User]).async { request =>
      userRepository
        .update(request.body.copy(id = Some(id)))
        .map {
          _.fold[Result](NotFound)(user => Ok(Json.toJson(user)))
        }
    }
  }

  def addLogin(userId: Long) = silhouette.SecuredAction(WithRole(UserRole.Admin))(parse.json[LoginInfo]).async { request =>
    loginRepository
      .create(userId, request.body)
      .map(info => Created(Json.toJson(info)))
  }

  def updatePassword(userId: Long) = silhouette.SecuredAction(WithRole(UserRole.Admin))(parse.json[BasicAuthPasswordUpdate]).async { request =>
    val passwordUpdate = request.body
    val hasher = hasherRegistry.current

    val passwordInfo = hasher.hash(passwordUpdate.next)

    {
      for {
        login <- OptionT(loginRepository.retrieve(userId, CredentialsProvider.ID))
        info <- OptionT.liftF(authInfoRepository.save(login, passwordInfo))
      } yield info
    }.fold[Result](NotFound)(_ => Ok)
  }


  @ApiOperation(value = "Create user and send to email link for setting password")
  @ApiImplicitParams(Array(new ApiImplicitParam(required = true, paramType = "body", dataType = "models.swagger.User")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "User", response = classOf[_root_.silhouette.User]),
    new ApiResponse(code = 401, message = "not authorized")))
  def createUser = silhouette.SecuredAction(WithRole(UserRole.Admin)).async(parse.json[User]) { implicit request =>
    val user = request.body
    val loginInfo = LoginInfo(CredentialsProvider.ID, user.email)

    users.retrieve(loginInfo).flatMap {
      case None =>
        for {
          u <- userRepository.create(user.copy(id = None))
          _ <- loginRepository.create(u.id.get, loginInfo)
        } yield Ok(Json.toJson(u))

      case Some(_) => throw HttpError(400, "Error! There is another user with this email!")
    }
  }

  def announcingByAgencyIDs = silhouette.SecuredAction(WithRoleAdmin).async(parse.json[List[Long]]) { implicit request =>
    {
      val agencyIDs = request.body
      for {
        allAgencyPermissions <- permissionsRepository.findWithResourceType(ResourceType.Agency)
        permissions <- Future.successful(allAgencyPermissions.filter(p => agencyIDs.contains(p.resourceId)))
        allUsers <- userRepository.findAll
        usersForDispatch <- Future.successful(allUsers.filter(u => permissions.exists(p => p.userId == u.id.getOrElse(0))))

        seqTuple <- Future.successful(usersForDispatch.map(u => u -> linkBuilder.getLink(u.email, DateTime.now.plusHours(48).getMillis)))
        _ <- mailer.announcing(seqTuple)
      } yield usersForDispatch
    }.map(users => Ok(Json.toJson(users)))
  }

  def announcingByUserIDs = silhouette.SecuredAction(WithRoleAdmin).async(parse.json[List[Long]]) { implicit request =>

    {
      val userIDsForDispatch = request.body
      for {
        allUsers <- userRepository.findAll
        usersForDispatch <- Future.successful(allUsers.filter(u => userIDsForDispatch.contains(u.id.getOrElse(0))))
        seqTuple <- Future.successful(usersForDispatch.map(u => u -> linkBuilder.getLink(u.email, DateTime.now.plusHours(48).getMillis)))
        _ <- mailer.announcing(seqTuple)
      } yield usersForDispatch
    }.map(users => Ok(Json.toJson(users)))

  }

  def announcingForAll = silhouette.SecuredAction(WithRoleAdmin).async { implicit request =>

    {
      for {
        allUsers <- userRepository.findAll
        usersForDispatch <- Future.successful(allUsers.filter(u => u.id.isDefined && u.email.length > 0 && u.role == UserRole.Buyer))
        seqTuple <- Future.successful(usersForDispatch.map(u => u -> linkBuilder.getLink(u.email, DateTime.now.plusHours(48).getMillis)))
        _ <- mailer.announcing(seqTuple)
      } yield usersForDispatch
    }.map(users => Ok(Json.toJson(users)))
  }

}
