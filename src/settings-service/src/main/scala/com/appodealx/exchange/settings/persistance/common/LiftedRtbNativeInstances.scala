package com.appodealx.exchange.settings.persistance.common

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.dto
import com.appodealx.openrtb.{ApiFramework, CreativeAttribute, Json}

trait LiftedRtbNativeInstances {

  case class  LiftedRtbNative(ver: Rep[Option[String]],
                              api: Rep[Option[List[ApiFramework]]],
                              battr: Rep[Option[List[CreativeAttribute]]],
                              request: Rep[Option[String]],
                              ext: Rep[Option[Json]])

  implicit object RtbNativeShape extends CaseClassShape(LiftedRtbNative.tupled, (dto.Native.apply _).tupled)

}
