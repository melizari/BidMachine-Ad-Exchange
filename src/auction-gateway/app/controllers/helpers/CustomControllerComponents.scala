package controllers.helpers

import play.api.mvc.{ControllerComponents, CustomPlayBodyParsers}

trait CustomControllerComponents extends ControllerComponents {
  def parsers: CustomPlayBodyParsers
}

object CustomControllerComponents {

  def apply(cc: ControllerComponents, p: CustomPlayBodyParsers) = DefaultCustomControllerComponents(
    actionBuilder = cc.actionBuilder,
    parsers = p,
    messagesApi = cc.messagesApi,
    langs = cc.langs,
    fileMimeTypes = cc.fileMimeTypes,
    executionContext = cc.executionContext
  )

}