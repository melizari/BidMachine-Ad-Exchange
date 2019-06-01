import com.appodealx.exchange.common.db.PostgresProfile
import play.api.db.slick.{DbName, SlickComponents}

trait PostgresComponents extends SlickComponents {

  lazy val dbConfig = slickApi.dbConfig[PostgresProfile](DbName("default"))

}
