package com.appodealx.exchange.common

import _root_.slick.lifted.MappedTo
import com.appodealx.exchange.common.models.auction.{AdProfileId, AgencyId, BidderId}
import play.api.mvc.PathBindable

package object models {

  type Id = Long

  type Uri = io.lemonlabs.uri.Uri

  object binders {
    def mappedToPathBindable[A <: MappedTo[T], T](b: T => A)(implicit d: PathBindable[T]) = d.transform[A](b, _.value)

    implicit val appIdPathBindable = mappedToPathBindable(AppId.apply)
    implicit val publisherIdPathBindable = mappedToPathBindable(PublisherId.apply)
    implicit val agencyIdPathBindable = mappedToPathBindable(AgencyId.apply)
    implicit val bidderIdPathBindable = mappedToPathBindable(BidderId.apply)
    implicit val adProfileIdPathBindable = mappedToPathBindable(AdProfileId.apply)
  }
}
