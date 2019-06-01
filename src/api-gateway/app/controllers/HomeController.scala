package controllers


import play.api.mvc._


class HomeController(cc: ControllerComponents, proxyPath: String) extends AbstractController(cc) {

  def index() = Action { implicit request =>
    Ok(views.html.swagger(request.host, proxyPath))
  }

  def status = Action(Ok)

}
