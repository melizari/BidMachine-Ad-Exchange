import play.api.{Application, ApplicationLoader}

class AdTrackerApplicationLoader extends ApplicationLoader {

  override def load(context: ApplicationLoader.Context): Application = new AdTrackerComponents(context).application

}