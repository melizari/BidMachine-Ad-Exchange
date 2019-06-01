package services.auction.pb.adapters.smaato.model.nast

import io.circe.derivation.{deriveDecoder, deriveEncoder}


// method 0 - pixel tracker
// method 1 - js tag (must be ignored while converting to imptrackers
case class EventTrackerObject(event: Int, method: Int, url: String)

object EventTrackerObject {
  implicit val eventTrackerObjectDecoder = deriveDecoder[EventTrackerObject]
  implicit val eventTrackerObjectEncoder = deriveEncoder[EventTrackerObject]
}

