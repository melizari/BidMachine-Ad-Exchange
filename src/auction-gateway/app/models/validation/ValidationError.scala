package models.validation

case class ValidationError(pathComponents: List[String], message: String) extends RuntimeException(message) {

  override def toString = s"ValidationError at path ${pathComponents.mkString(".")}: $message"
}
