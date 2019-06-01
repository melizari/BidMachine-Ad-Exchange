package utils.mail

import models.mandrill.{MergeVar, To, Var}
import play.api.Configuration
import silhouette.User

case class MandrillMailer(configuration: Configuration, client: MandrillClient) extends Mailer {

  lazy val templateWelcomeName = configuration.get[String]("mandrill.templates.welcome.name")
  lazy val templateForgotPasswordName = configuration.get[String]("mandrill.templates.forgotPassword.name")
  lazy val templateAnnouncing = configuration.get[String]("mandrill.templates.announcing.name")

  lazy val contentUserName = configuration.get[String]("mandrill.contentUser.name")
  lazy val contentCompanyName = configuration.get[String]("mandrill.contentCompany.name")
  lazy val contentUrlName = configuration.get[String]("mandrill.contentUrl.name")

  lazy val subjectWelcome = configuration.get[String]("mandrill.templates.welcome.subject")
  lazy val subjectForgotPassword = configuration.get[String]("mandrill.templates.forgotPassword.subject")
  lazy val subjectAnnouncing = configuration.get[String]("mandrill.templates.announcing.subject")


  override def welcome(emailTo: String, userName: String, companyName: String, url: String) = {

    val mergeVar = MergeVar(emailTo, List(Var(contentCompanyName, companyName), Var(contentUrlName, url)))
    val to = To(emailTo, userName) :: Nil

    client.sendWithTemplate(templateName = templateWelcomeName, to = to, Some(subjectWelcome), mergeVar :: Nil)
  }

  override def forgotPassword(emailTo: String, userName: String, companyName: String, url: String) = {

    val mergeVar = MergeVar(emailTo, List(Var(contentCompanyName, companyName), Var(contentUrlName, url)))
    val to = To(emailTo, userName) :: Nil

    client.sendWithTemplate(templateName = templateForgotPasswordName, to = to, Some(subjectForgotPassword), mergeVar :: Nil)
  }

  override def announcing(usersWithURLs: List[(User, String)]) = {

    val mergeVars = usersWithURLs.map(t =>
      MergeVar(t._1.email, List(Var(contentCompanyName, t._1.company.getOrElse("")), Var(contentUrlName, t._2)))
    )
    val to = usersWithURLs.map(t =>
      To(t._1.email, t._1.name.getOrElse(""))
    )

    client.sendWithTemplate(templateName = templateAnnouncing, to = to, subject = Some(subjectAnnouncing), mergeVars = mergeVars)
  }
}
