package services.auction.pb.adapters.criteo.s2s.model.rq
import io.circe.Encoder
import io.circe.derivation.deriveEncoder

private[s2s] case class Device(id: String,
                  ip: String,
                  category: String,
                  system: String,
                  environment: String = "inapp",
                  carrier: Option[String],
                  connectiontype: Option[Int],
                  lmt: Option[Boolean])
private[s2s] object Device {
  private implicit val booleanEncoder = Encoder.encodeInt.contramap[Boolean](if (_) 1 else 0)

  implicit val encoder = deriveEncoder[Device]
}
