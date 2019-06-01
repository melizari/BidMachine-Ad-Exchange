package services.settings

case class AppsBySellerId(sellerId: Long, appsIds: List[String])

object AppsBySellerId {

  implicit final class AppsBySellerIdOps(val appsBySeller: AppsBySellerId) extends AnyVal {
    def isAppSupported(seller: Long, appId: String) = {
      val isAppSupported: List[String] => Boolean =
        as => as.headOption.exists(_.toLowerCase == "all") || as.contains(appId)

      if (appsBySeller.sellerId == seller) isAppSupported(appsBySeller.appsIds) else false
    }
  }
}