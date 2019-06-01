package services.settings
import com.typesafe.config.Config
import play.api.ConfigLoader

case class MaxMindLocalSettings(lruCacheSize: Int, geoFile: String)

object MaxMindLocalSettings {

  implicit val configLoader: ConfigLoader[MaxMindLocalSettings] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)

    val maxmindLruCacheSize = config.getInt("lru-cache-size")
    val maxmindDbPath       = config.getString("path")

    MaxMindLocalSettings(
      lruCacheSize = maxmindLruCacheSize,
      geoFile = maxmindDbPath
    )
  }

}