package utils.mail

import io.circe.syntax._
import models.mandrill._
import play.Logger
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global

case class MandrillClient(configuration: Configuration, ws: WSClient) {

  lazy val apiKey: String = configuration.get[String]("mandrill.apiKey")
  lazy val mandrillUrl = configuration.get[String]("mandrill.url")
  lazy val fromEmail = configuration.get[String]("mandrill.fromEmail")
  lazy val fromName = configuration.get[String]("mandrill.fromName")


  def sendWithTemplate(templateName: String, to: List[To], subject: Option[String], mergeVars: List[MergeVar]) = {

    val body = SendTemplateBody(
      apiKey,
      templateName,
      Nil,
      Message(subject = subject,
              from_email = fromEmail,
              from_name = fromName,
              to = to,
              merge_vars = mergeVars)
    )

    Logger.warn(body.asJson.toString())

    ws.url("https://mandrillapp.com/api/1.0/messages/send-template.json")
      .withBody(body.asJson.toString)
      .execute("POST")
      .map {res => Logger.warn(res.body)}
  }

}
