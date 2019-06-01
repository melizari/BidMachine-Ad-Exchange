package controllers

import play.api.mvc._


class HealthController(components: ControllerComponents) extends AbstractController(components) {
  
  def index = Action {
    Ok
  }

}
