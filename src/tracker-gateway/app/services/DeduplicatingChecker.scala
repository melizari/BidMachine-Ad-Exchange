package services

import cats.syntax.option._
import monix.eval.Task
import redis.RedisClient

import scala.concurrent.duration._


trait DeDuplicatingChecker {

  val client: RedisClient

  /**
    *
    * @param id key for event
    * @param instanceId random id (not used anywhere)
    * @param ttl lifetime of event
    * @return 'true' if no event exist in cache, 'false' if event in cache.
    */
  def verify(id: String, instanceId: String, ttl: Duration): Task[Boolean] =
    Task.deferFuture(
      client.set(
        key = s"dd#$id",
        value = instanceId,
        exSeconds = ttl.toSeconds.some,
        NX = true
      )
    )
}

class DeDuplicatingCheckerImpl(redisClient: RedisClient) extends DeDuplicatingChecker {

  override val client: RedisClient = redisClient
}
