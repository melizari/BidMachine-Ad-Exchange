package services

import com.appodealx.openrtb
import monix.eval.Task

trait AppRepo[F[_]] {

  def findAppInKs(ks: String, appEid: String): F[Option[openrtb.App]]

}

