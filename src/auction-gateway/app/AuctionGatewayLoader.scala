import com.appodealx.lagom.dcos.CloudServiceLocatorComponents
import play.api.ApplicationLoader.Context
import play.api._

class AuctionGatewayLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    (new AuctionGatewayComponents(context) with CloudServiceLocatorComponents).application
  }
}
