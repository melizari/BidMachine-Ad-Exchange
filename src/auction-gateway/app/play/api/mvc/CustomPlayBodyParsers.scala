package play.api.mvc

import controllers.helpers.DefaultCustomPlayBodyParsers
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

trait CustomPlayBodyParsers extends PlayBodyParsers {

  def protobuf[M <: GeneratedMessage with Message[M]](maxLength: Long)(implicit c: GeneratedMessageCompanion[M]): BodyParser[M] = {
    tolerantBodyParser[M]("protobuf", maxLength, "Error while parsing protobuf message " + c.scalaDescriptor.fullName)((_, b) => c.parseFrom(b.toArray))
  }

  def protobuf[M <: GeneratedMessage with Message[M]](implicit c: GeneratedMessageCompanion[M]): BodyParser[M] = protobuf(config.maxMemoryBuffer)

}

object CustomPlayBodyParsers {

  def apply(p: PlayBodyParsers) = DefaultCustomPlayBodyParsers(
    config = p.config,
    errorHandler = p.errorHandler,
    materializer = p.materializer,
    temporaryFileCreator = p.temporaryFileCreator
  )

}