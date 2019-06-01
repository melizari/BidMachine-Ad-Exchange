package services.init
import io.bidmachine.protobuf.InitResponse
import play.api.mvc.RequestHeader

trait InitService[F[_]] {
  def init(ip: String): F[Option[InitResponse]]
}
