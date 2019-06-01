package services

import akka.util.ByteString
import monix.eval.Task
import monix.execution.Scheduler
import redis.{ByteStringDeserializer, RedisClient}

import cats.syntax.option._

import scala.concurrent.duration.Duration

trait CustomFillTime {

  val client: RedisClient

  def push(key: String, value: Long, ttl: Duration): Task[Unit]

  def pull(key: String)(implicit s: Scheduler): Task[Option[Long]]
}

class CustomFillTimeImpl(redisClient: RedisClient) extends CustomFillTime {

  override val client = redisClient

  implicit val LongDeserializer = new ByteStringDeserializer[Long] {
    def deserialize(bs: ByteString) = bs.utf8String.toLong
  }

  override def push(key: String, value: Long, ttl: Duration) =
    Task.deferFuture(
      client.set(
        key = s"cf#$key",
        value = value,
        exSeconds = ttl.toSeconds.some,
        NX = true
      )
    ).forkAndForget

  override def pull(key: String)(implicit s: Scheduler) = Task.deferFuture(client.get[Long](s"cf#$key"))
}
