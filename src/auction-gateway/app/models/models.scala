import com.appodealx.openrtb._

package object models {

  object conversions {

    implicit class ConnectionTypeHelper(obj: ConnectionType.type) {

      private val ConnectionTypes = Map(
        "1" -> ConnectionType.Ethernet,
        "2" -> ConnectionType.Wifi,
        "3" -> ConnectionType.CellularUnknownGen) ++
        List("4", "GPRS", "GSM", "EDGE", "CDMA", "CDMA - 1xRTT", "iDEN", "HPRD", "CDMA1x").map(_.toLowerCase -> ConnectionType.Cellular2G) ++
        List("5", "UMTS", "CDMA - EvDo rev. 0", "CDMA - EvDo rev. A", "CDMA - EvDo rev. B", "HSDPA", "HSUPA", "HSPA", "CDMA - eHRPD", "HSPA+", "TD_SCDMA", "WCDMA", "CDMAEVDORev0", "CDMAEVDORevA", "CDMAEVDORevB").map(_.toLowerCase -> ConnectionType.Cellular3G) ++
        List("6", "LTE", "IWLAN", "LTE_CA").map(_.toUpperCase -> ConnectionType.Cellular4G)

      def fromString(string: String): ConnectionType = ConnectionTypes.getOrElse(string, ConnectionType.Unknown)
    }
  }
}
