import com.appodealx.lagom.dcos.CloudServiceLocatorComponents
import play.api.{ApplicationLoader, LoggerConfigurator}

class ApiGatewayProdLoader extends ApplicationLoader {
  def load(context: ApplicationLoader.Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }

    (new ApiGatewayComponents(context) with CloudServiceLocatorComponents).application
  }
}
