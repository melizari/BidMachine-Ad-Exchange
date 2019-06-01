package services.auction.pb.adapters.hangmyads.model.rs

import io.circe.derivation.deriveDecoder

private[hangmyads] case class Cpm(`current_CPM`: String, `min_CPM`: String, `max_CPM`: Option[String])

private[hangmyads] object Cpm {
  implicit val decoder = deriveDecoder[Cpm]
}
