package controllers.helpers

import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ActionBuilder, AnyContent, CustomPlayBodyParsers, Request}

case class DefaultCustomControllerComponents(actionBuilder: ActionBuilder[Request, AnyContent],
                                             parsers: CustomPlayBodyParsers,
                                             messagesApi: MessagesApi,
                                             langs: Langs,
                                             fileMimeTypes: FileMimeTypes,
                                             executionContext: scala.concurrent.ExecutionContext) extends CustomControllerComponents
