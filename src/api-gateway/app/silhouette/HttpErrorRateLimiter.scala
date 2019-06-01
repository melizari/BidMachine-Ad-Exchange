package silhouette

import com.digitaltangible.playguard.{FailureRateLimitFunction, RateLimiter}
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import play.api.mvc.{ActionFunction, Result}

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

/**
  * A Limiter for http errors produced by seller
  */
object HttpErrorRateLimiter {

  /**
    * A Rate limiter Function for http errors limit.
    *
    * @param rateLimiter         The rate limiter implementation.
    * @param reject              The function to apply on reject.
    * @param requestKeyExtractor The Request Parameter we want to filter from.
    * @tparam K the key by which to identify the user.
    */
  def apply[T <: Env, R[_] <: SecuredRequest[T, _], K](rateLimiter: RateLimiter)
                                                      (reject: R[_] => Result,
                                                       requestKeyExtractor: R[_] => K)
                                                      (implicit executionContext: ExecutionContext): FailureRateLimitFunction[R] with ActionFunction[R, R] = {
    new FailureRateLimitFunction[R](rateLimiter)(reject, requestKeyExtractor, r => !(400 to 499 contains r.header.status)) with ActionFunction[R, R]
  }

}
