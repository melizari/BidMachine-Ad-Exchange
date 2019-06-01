package services.settings.rubicon

import java.nio.charset.StandardCharsets
import java.util.UUID

import com.appodealx.exchange.common.models.auction.Plc
import models.auction.AdRequest
import services.settings.{AdNetworkSettings, PbSettings}

import scala.util.Try

final case class RubiconSettings(config: RubiconConfiguration, pbSettings: PbSettings) extends AdNetworkSettings {

  val timeout     = pbSettings.pbTmax
  val accountId   = config.accountId
  val siteId      = config.siteId
  val zoneId      = config.zoneId
  val videoSizeId = config.videoSizeId

  def bannerSizeId(size: String) = config.zoneBySize.get(size)

  val base64EncodedCredentials: String = java.util.Base64.getEncoder.encodeToString(
    s"${config.user}:${config.password}".getBytes(StandardCharsets.UTF_8)
  )

  override def enabled[T: Plc](request: AdRequest[T]): Boolean = {

    val allowedApp = config.apps.isEmpty || request.app.bundle.exists(config.apps)

    val allowedIpv6 =
      request.device.ip.isDefined || (request.device.ip.isEmpty && request.device.ipv6.isEmpty) || config.allowIpv6

    val allowedCountry = !request.device.geo.flatMap(_.country).exists(config.excludedCountries)

    val isValidIfa = {
      val nonZeroUUID = (uuid: UUID) => uuid.getLeastSignificantBits != 0 && uuid.getMostSignificantBits != 0
      val ifa         = request.device.ifa.flatMap(ifa => Try(UUID.fromString(ifa)).toOption)

      ifa.nonEmpty && ifa.exists(nonZeroUUID)
    }

    allowedApp && allowedIpv6 && isValidIfa && allowedCountry
  }
}
