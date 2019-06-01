package services.settings

import com.appodealx.exchange.common.models.Id
import models.MarketplaceType
import services.auction.Auction

case class SellerAuctionsSettings[F[_]](getAuctionsBySellerId: Id => Option[List[Auction[F]]],
                                        defaultAuctions: List[Auction[F]])

object SellerAuctionsSettings {

  def apply[F[_]](sellerPerMarketplacesSettings: SellersMarketplacesSettings,
                  auctionsPerMarketplace: Map[MarketplaceType, Auction[F]],
                  defaultAuctions: List[Auction[F]]) = {
    val sellerPerMarketplaces  = sellerPerMarketplacesSettings.settings

    val getAuctionsBySellerId = (id: Long) => {
      sellerPerMarketplaces.get(id) map { marketplaces =>
        marketplaces.flatMap(auctionsPerMarketplace.get).toList
      }
    }

    new SellerAuctionsSettings(getAuctionsBySellerId, defaultAuctions)
  }
}
