package silhouette

import com.digitaltangible.playguard.{RateLimitActionFilter, RateLimiter}
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import play.api.mvc.{ActionFunction, Result}

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

/**
  * A Limiter for http requests produced by seller
  */
object IdentityLimiter {

  /**
    * A Rate limiter Function for user request limit.
    *
    * @param rl         The rate limiter implementation.
    * @param rf     The function to apply on reject.
    */
  def apply[E <: Env](rl: RateLimiter)(rf: SecuredRequest[E, _] => Result)(implicit executionContext: ExecutionContext) = {
    new RateLimitActionFilter[SecuredRequest[E, ?]](rl)(rf, _.identity)
  }

}
