package com.appodealx.exchange.settings.persistance.common

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.dto
import com.appodealx.openrtb._

trait LiftedRtbVideoInstances {

  case class LiftedRtbVideo(mimes: Rep[List[String]],
                            minduration: Rep[Option[Int]],
                            maxduration: Rep[Option[Int]],
                            protocol: Rep[Option[Protocol]],
                            protocols: Rep[Option[List[Protocol]]],
                            w: Rep[Option[Int]],
                            h: Rep[Option[Int]],
                            startdelay: Rep[Option[Int]],
                            linearity: Rep[Option[VideoLinearity]],
                            battr: Rep[Option[List[CreativeAttribute]]],
                            maxextended: Rep[Option[Int]],
                            minbitrate: Rep[Option[Int]],
                            maxbitrate: Rep[Option[Int]],
                            boxingallowed: Rep[Option[Boolean]],
                            playbackmethod: Rep[Option[List[PlaybackMethod]]],
                            delivery: Rep[Option[List[ContentDeliveryMethod]]],
                            pos: Rep[Option[AdPosition]],
                            api: Rep[Option[List[ApiFramework]]],
                            ext: Rep[Option[Json]])

  implicit object RtbVideoShape extends CaseClassShape(LiftedRtbVideo.tupled, (dto.Video.apply _).tupled)

}
