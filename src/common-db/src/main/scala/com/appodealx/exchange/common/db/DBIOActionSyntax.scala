package com.appodealx.exchange.common.db

import com.appodealx.exchange.common.db.typeclasses.Execute
import monix.eval.Task
import play.api.db.slick.HasDatabaseConfig
import slick.dbio.{DBIOAction, NoStream}

trait DBIOActionSyntax { self: HasDatabaseConfig[_] =>
  implicit class DBIOActionOps[R](a: DBIOAction[R, NoStream, Nothing]) {
    def run() = db.run(a)
    def toTask = Task.deferFuture(db.run(a))
    def lift[F[_]: Execute] = Execute[F].deferFuture(db.run(a))
  }
}
