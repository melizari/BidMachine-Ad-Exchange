package com.appodealx.exchange.common.db

import com.github.tminglei.slickpg._

trait PostgresProfile extends ExPostgresProfile
  with PgArraySupport
  with PgCirceJsonSupport
  with PgDateSupportJoda
  with VectorSupport
  with SeqSupport
  with JsonVectorSupport
  with EnumeratumSupport
  with FiniteDurationSupport
  with PgHStoreSupport {

  final val pgjson = "jsonb"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  // override def computeCapabilities = super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api = new API
    with ArrayImplicits
    with JodaDateTimeImplicits
    with VectorImplicits
    with SeqImplicits
    with JsonVectorImplicits
    with CirceImplicits
    with StringEnumImplicits
    with IntEnumImplicits
    with FiniteDurationImplicits
    with HStoreImplicits
}

object PostgresProfile extends PostgresProfile