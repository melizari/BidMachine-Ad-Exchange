package services.auction.pb.adapters.criteo.s2s.model.rq

import com.appodealx.exchange.common.models.circe.CirceEnumInstances
import com.appodealx.openrtb.ApiFramework
import io.circe.Encoder
import io.circe.derivation.deriveEncoder

private[s2s] case class Video(api: Option[ApiFramework],
                 maxduration: Option[Int],
                 minduration: Option[Int],
                 skippable: Option[Boolean],
                 startdelay: Option[Int],
                 linearity: Option[Int],
                 size: Option[Size])

private[s2s] object Video extends CirceEnumInstances {
  private implicit val booleanEncoder = Encoder.encodeInt.contramap[Boolean](if (_) 1 else 0)

  implicit val encoder = deriveEncoder[Video]
}
