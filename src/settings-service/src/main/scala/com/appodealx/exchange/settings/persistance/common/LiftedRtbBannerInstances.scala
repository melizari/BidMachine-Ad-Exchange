package com.appodealx.exchange.settings.persistance.common

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.dto
import com.appodealx.openrtb._

trait LiftedRtbBannerInstances {

  case class LiftedRtbBanner(w: Rep[Option[Int]],
                             h: Rep[Option[Int]],
                             btype: Rep[Option[List[BannerAdType]]],
                             battr: Rep[Option[List[CreativeAttribute]]],
                             pos: Rep[Option[AdPosition]],
                             mimes: Rep[Option[List[String]]],
                             topframe: Rep[Option[Boolean]],
                             expdir: Rep[Option[List[ExpandableDirection]]],
                             api: Rep[Option[List[ApiFramework]]],
                             ext: Rep[Option[Json]])


  implicit object RtbBannerShape extends CaseClassShape(LiftedRtbBanner.tupled, (dto.Banner.apply _).tupled)

}
