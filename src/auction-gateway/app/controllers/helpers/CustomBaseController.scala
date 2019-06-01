package controllers.helpers

import play.api.mvc.BaseController

abstract class CustomBaseController(protected val controllerComponents: CustomControllerComponents) extends BaseController {

  override def parse = controllerComponents.parsers

}