package silhouette.repositories

import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import play.api.db.slick.HasDatabaseConfig
import silhouette.persistence.tables.{Permissions, UserLogins, Users}
import silhouette.{PersistentUser, User}
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}

class UserIdentityService(protected val dbConfig: DatabaseConfig[PostgresProfile])(implicit ec: ExecutionContext)
  extends IdentityService[User]
    with HasDatabaseConfig[PostgresProfile]
    with DBIOActionSyntax {

  import profile.api._

  def create(user: User): Future[User] = {
//        (Users.returning(Users.map(_.id)).into((u, id) => u.copy(id = Some(id))) += user.copy(id = None)).run()
    (Users.returning(Users.map(_.id)).into((u, id) => u.copy(id = Some(id))) += PersistentUser(None, user.email, user.role, user.name, user.company)).run()
      .map(u => User(u.id, u.email, u.role, u.name, u.company, Vector.empty))

  }

  def user(id: Long): Future[Option[User]] = {
    //    Users.filter(_.id === id).result.headOption.run()

    {
      for {
        (u, p) <- Users.filter(_.id === id) joinLeft Permissions on (_.id === _.userId)
      } yield (u, p)
    }
      .result.run()
      .map {
        _.groupBy(_._1)
          .map(t => (t._1, t._2.flatMap(_._2)))
          .headOption.map { case (u, p) => User(u.id, u.email, u.role, u.name, u.company, p.toVector) }
      }
  }

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val userLoginQuery = UserLogins.filter(l => l.providerId === loginInfo.providerID && l.providerKey === loginInfo.providerKey)

    {
      for {
        (u, p) <- (userLoginQuery join Users on (_.userId === _.id)).map(_._2) joinLeft Permissions on (_.id === _.userId)
      } yield (u, p)
    }.result.run()
      .map {
        _.groupBy(_._1)
          .map(t => (t._1, t._2.flatMap(_._2)))
          .headOption.map { case (u, p) => User(u.id, u.email, u.role, u.name, u.company, p.toVector) }
      }
  }

  def findAll = {
    //    Users.sortBy(_.id).result.run()
    {
      for {
        (u, p) <- Users joinLeft Permissions on (_.id === _.userId)
      } yield (u, p)
    }
      .result.run()
      .map {
        _.groupBy(_._1)
          .map { t => (t._1, t._2.flatMap(_._2)) }
          .toList.map { case (u, p) => User(u.id, u.email, u.role, u.name, u.company, p.toVector) }.sortBy(_.id)
      }
  }


  def update(user: User) = {
    Users
      .filter(_.id === user.id)
      .update(PersistentUser(user.id, user.email, user.role, user.name, user.company))
      .run()
      .map(u => Option(user).filter(_ => u > 0))
  }

}
