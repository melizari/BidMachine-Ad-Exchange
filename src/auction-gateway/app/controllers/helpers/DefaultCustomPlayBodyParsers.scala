package controllers.helpers

import akka.stream.Materializer
import play.api.http.{HttpErrorHandler, ParserConfiguration}
import play.api.libs.Files.TemporaryFileCreator
import play.api.mvc.CustomPlayBodyParsers

case class DefaultCustomPlayBodyParsers(config: ParserConfiguration,
                                        errorHandler: HttpErrorHandler,
                                        materializer: Materializer,
                                        temporaryFileCreator: TemporaryFileCreator) extends CustomPlayBodyParsers
