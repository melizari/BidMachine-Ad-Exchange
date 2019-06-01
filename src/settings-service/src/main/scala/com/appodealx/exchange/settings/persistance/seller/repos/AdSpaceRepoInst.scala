package com.appodealx.exchange.settings.persistance.seller.repos

import cats.ApplicativeError
import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.models.auction.Plc
import com.appodealx.exchange.common.utils.{TypeClassInst, TypeClassSelector, TypeTag}
import com.appodealx.exchange.settings.models.seller.{AdSpace, AdSpaceId}
import scalacache.Mode


abstract class AdSpaceRepoInst[F[_], P: Plc] extends TypeClassInst[P] {

  def find(id: AdSpaceId): F[Option[AdSpace[P]]]

}

abstract class AdSpaceRepo[F[_]](repos: AdSpaceRepoInst[F, _]*)
    extends TypeClassSelector[AdSpaceRepoInst[F, ?]](repos) {

  def find[P: Plc](id: AdSpaceId): F[Option[AdSpace[P]]] = selectInst[P].find(id)

}

class AdSpaceRepoImpl[F[_]: ApplicativeError[?[_], Throwable]: Execute: Mode](
  bannerRepo: BannerAdSpaceRepo[F],
  videoRepo: VideoAdSpaceRepo[F],
  nativeRepo: NativeAdSpaceRepo[F]
) extends AdSpaceRepo[F](bannerRepo, videoRepo, nativeRepo)
