package controllers.adtracker

import play.api.mvc._


class HealthCheckController(components: ControllerComponents) extends AbstractController(components) {

  def check = Action {
    Ok
  }

}
