package services.auction.pb.adapters.criteo.s2s.model.rq
import com.appodealx.exchange.common.models.circe.CirceEnumInstances
import com.appodealx.openrtb.ApiFramework
import io.circe.Encoder
import io.circe.derivation.deriveEncoder

private[s2s] case class Impression(id: String,
                      zoneid: String,
                      tid: String,
                      tagid: String,
                      sourcetype: Int,
                      creativetype: String,
                      displaytype: Option[String],
                      displaymanager: Option[String],
                      displaymanagerver: Option[String],
                      instl: Option[Boolean],
                      secure: Option[Boolean],
                      visibility: Option[Int],
                      viewability: Option[Int],
                      api: Option[List[ApiFramework]],
                      floorprice: Double,
                      sizes: Option[List[Size]],
                      nativeimagesize: Option[Size] = None,
                      deals: List[Deal],
                      video: Option[Video] = None)

private[s2s] object Impression extends CirceEnumInstances {

  private implicit val booleanEncoder = Encoder.encodeInt.contramap[Boolean](if (_) 1 else 0)

  implicit val encoder = deriveEncoder[Impression]

}
