package models.auction

import io.circe.{Decoder, Json}

import cats.instances.either._
import cats.syntax.contravariantSemigroupal._

case class AdUnit(sdk: String,
                  sdkVer: String,
                  externalId: Option[String],
                  cpmEstimate: Option[Double],
                  customParams: Option[Json])

object AdUnit {

  implicit val decoder = Decoder.instance { cur =>
    (cur.downField("displaymanager").as[String],
     cur.downField("displaymanager_ver").as[String],
     cur.downField("appodeal").downField("id").as[Option[String]],
     cur.downField("appodeal").downField("ecpm").as[Option[Double]],
     cur.downField("ext").as[Option[Json]]).mapN(AdUnit.apply)
  }

}
