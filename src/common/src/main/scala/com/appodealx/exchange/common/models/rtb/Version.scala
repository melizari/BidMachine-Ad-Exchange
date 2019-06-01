package com.appodealx.exchange.common.models.rtb

import com.appodealx.openrtb.Pretty
import enumeratum.values.{IntCirceEnum, IntEnum, IntEnumEntry}

sealed abstract class Version(override val value: Int) extends IntEnumEntry


object Version extends IntEnum[Version] with IntCirceEnum[Version] {

  import com.appodealx.exchange.common.db.PostgresProfile.api._

  case object `2.3` extends Version(23)
  case object `2.4` extends Version(24)

  val values = findValues

  implicit lazy val rtbVersionMapper = MappedColumnType.base[Version, Int](
    version => version.value,
    raw => Version.withValue(raw)
  )

  implicit class VersionOps(v: Version) extends Pretty {
    override def prettyValue: String = v match {
      case `2.3` => "2.3"
      case `2.4` => "2.4"
    }
  }

}