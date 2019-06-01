package models.validation.instances

import io.bidmachine.protobuf.RequestExtension
import io.bidmachine.protobuf.adcom.Context.{App, Device}
import io.bidmachine.protobuf.adcom.DeviceType.DEVICE_TYPE_INVALID
import io.bidmachine.protobuf.adcom.Placement.{DisplayPlacement, VideoPlacement}
import io.bidmachine.protobuf.adcom.{Context, Placement, SizeUnit}
import io.bidmachine.protobuf.openrtb.{Openrtb, Request}
import io.bidmachine.protobuf.openrtb.Request.Item
import io.bidmachine.protobuf.openrtb.Request.Item.Deal
import models.validation.Validator

trait Rtb3Validators {

  import models.validation.syntax._
  import models.validation.FieldSyntax._


  implicit val requestExtensionValidator = Validator.of[RequestExtension] { re =>
    re.field(_.sellerId, "sellerId") should beParsedAsLong
  }

  val displayPlacementValidator = Validator.all[DisplayPlacement] { dp =>

    val sizeValidator = List(
      dp.field(_.w, "w")       should bePositiveInt,
      dp.field(_.h, "h")       should bePositiveInt,
      dp.field(_.unit, "unit") should beEqualTo(SizeUnit.SIZE_UNIT_DIPS)
    )

    dp.nativefmt match {
      case Some(_) => List(valid(dp))
      case None => sizeValidator
    }
  }

  implicit val placementValidator = Validator.all[Placement] { pl =>
    val rootPath = "request.item.spec"
    def displayValidation = pl.field(_.display.get, s"$rootPath.display") validation displayPlacementValidator

    List(

      pl.field(_.sdk, s"$rootPath.sdk")       should beNonEmptyString,
      pl.field(_.sdkver, s"$rootPath.sdkver") should beNonEmptyString,

      (pl.display, pl.video) match {
        case(Some(_), None)    => displayValidation
        case(None, Some(_))    => valid(pl)
        case(Some(_), Some(_)) => displayValidation
        case _                 => invalid[Placement](rootPath, "display or video must provided")
      }
    )

  }

  val deviceValidator: Validator[Device, Device] = Validator.of[Device] { device =>
    device.field(_.`type`, "type") should filter(t => t != DEVICE_TYPE_INVALID, "device type must be valid")
  }

  val appValidator: Validator[App, App] = Validator.of[App] { app =>
    app.field(_.bundle, "bundle") should beNonEmptyString
  }

  implicit val contextValidator = Validator.all[Context] { ctx =>
    List(
      (ctx.field(_.app, "app") should filter(_.isDefined, "app must be provided."))

        .andThen(ctx.field(_.app.get, "request.context.app") validation appValidator),

      (ctx.field(_.device, "device") should filter(_.isDefined, "device must be provided."))

        .andThen(ctx.field(_.device.get, "request.context.device") validation deviceValidator)

    )
  }

  val dealValidator = Validator.all[Deal] { deal =>
    List(

      deal.field(_.flr, "flr")       should bePositiveDouble,
      deal.field(_.flrcur, "flrcur") should beEqualToOneOf("", "USD")

    )
  }

  val itemValidator: Validator[Item, Item] = Validator.all[Item] { item =>
    List(

      item.field(_.id, "id")         should beNonEmptyString,
      item.field(_.qty, "qty")       should beEqualTo(1),
      item.field(_.seq, "seq")       should beEqualTo(0),
      item.field(_.spec, "spec")     should beOptionOfAny[Placement],

      (item.field(_.deal, "deal")    should filter(_.nonEmpty, "deal must be provided"))

        .andThen(item.field(_.deal.head, "deal") validation dealValidator)

    )
  }

  val requestPayloadValidator = Validator.all[Request] { rq =>
    List(

      rq.field(_.at, "at")           should beEqualToOneOf(1, 2),
      rq.field(_.cur, "cur")         should filter(_.contains("USD"), "now supported only USD."),
      rq.field(_.context, "context") should beOptionOfAny[Context],
      rq.field(_.ext, "ext")         should beSeqOfAny[RequestExtension],

      (rq.field(_.item, "item")      should filter(_.nonEmpty, "item must be provided"))

        .andThen(rq.field(_.item.head, "item") validation itemValidator)

    )
  }

  implicit val openRtbValidator = Validator.all[Openrtb] { op =>
    List(

      op.field(_.ver, "ver")               should beEqualTo("3.0"),
      op.field(_.domainspec, "domainspec") should beSameStringIgnoringCase("adcom"),
      op.field(_.domainver, "domainver")   should beNonEmptyString,

      (op.field(_.payload, "payload") should filter(_.isRequest, "request is not provided"))

        .andThen(op.field(_.payload.request.get, "request") validation requestPayloadValidator),

    )
  }

}
