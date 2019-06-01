package services.auction.rtb3

import io.bidmachine.protobuf.openrtb.Openrtb
import models.rtb3.Rtb3Request
import models.validation.instances.Rtb3Validators
import services.ValidationService
import models.rtb3.unpackers.{contextOf, placementOf, requestExtOf}

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._

trait Rtb3Unpacker[F[_]] {
  def makeRtb3ReqOf(rtb: Openrtb): F[Rtb3Request]
}

class Rtb3UnpackerImpl[F[_]: Monad](VS: ValidationService[F]) extends Rtb3Unpacker[F] with Rtb3Validators {

  def makeRtb3ReqOf(rtb: Openrtb): F[Rtb3Request] =
    for {
      vrtb <- VS validate rtb
      plc  <- VS unpackAndValidate placementOf(vrtb)
      ctx  <- VS unpackAndValidate contextOf(vrtb)
      re   <- VS unpackAndValidate requestExtOf(vrtb)
      req  = vrtb.payload.request.get
    } yield Rtb3Request(plc, ctx, req.item.head, re, req.id, req.test)
}
