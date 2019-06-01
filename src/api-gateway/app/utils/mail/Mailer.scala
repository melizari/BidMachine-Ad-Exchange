package utils.mail

import silhouette.User

import scala.concurrent.Future

abstract class Mailer {

  def welcome(emailTo: String, userName: String, companyName: String, url: String): Future[Unit]

  def forgotPassword(emailTo: String, userName: String, companyName: String, url: String): Future[Unit]

  def announcing(usersWithURLs: List[(User, String)]): Future[Unit]
}
