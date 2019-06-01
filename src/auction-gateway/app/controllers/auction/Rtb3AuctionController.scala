package controllers.auction

import controllers.helpers.{CustomBaseController, CustomControllerComponents}
import io.bidmachine.protobuf.openrtb.Openrtb
import models.DefaultWriteables
import monix.eval.Task
import monix.execution.Scheduler
import services.auction.rtb3.Rtb3Service

import scala.language.{higherKinds, postfixOps}

class Rtb3AuctionController(rtb3Service: Rtb3Service[Task], cc: CustomControllerComponents)(
  implicit val scheduler: Scheduler
) extends CustomBaseController(cc) with DefaultWriteables {

  def action = Action.async(parse.protobuf[Openrtb]) { req =>
    def ipFromHeaders = {
      val headers = req.headers

      headers
        .get("x-forwarded-for")
        .orElse(headers.get("X-Forwarded-For"))
        .flatMap(_.split(",").headOption)
        .getOrElse("")
    }

    rtb3Service
      .performAuction(req.body, ipFromHeaders, req.host)
      .map(res => res.fold(NoContent)(Ok(_)))
      .runToFuture
  }
}
