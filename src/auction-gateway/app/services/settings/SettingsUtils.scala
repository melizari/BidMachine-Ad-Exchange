package services.settings

import com.appodealx.exchange.common.models.Id

import scala.util.Try

object SettingsUtils {

  /**
   * Gets a string in the following format => id1: val1,val2; id2: val2; idN: valN;
   * And try to parse it to Map[Id, String].
   */
  def parseConfigStringOfOneToManyAssociations(config: String): Option[Map[Id, Set[String]]] =
    Try {
      require(config.nonEmpty)

      config
        .replaceAll("\\s+", "")
        .split(";")
        .map(s => (s.head.asDigit.toLong, s.split(":")(1)))
        .map { case (l, r) => l -> r.split(",").toSet }
        .toMap

    }.toOption

}
