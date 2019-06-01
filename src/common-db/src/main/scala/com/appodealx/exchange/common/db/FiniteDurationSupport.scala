package com.appodealx.exchange.common.db

import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

trait FiniteDurationSupport { driver: JdbcProfile =>
  import driver.api._

  trait FiniteDurationImplicits {

    implicit lazy val finiteDurationColumnType = MappedColumnType.base[FiniteDuration, Long](
      _.toMillis,
      FiniteDuration(_, MILLISECONDS)
    )
  }

}
