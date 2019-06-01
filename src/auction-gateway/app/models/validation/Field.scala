package models.validation

case class Field[A, B](f: A => B, path: String) {

  def validation[C](implicit v: Validator[B, C]): Validator[A, C] = (a: A) => {
    val inner = v.leftMap(e => e.copy(pathComponents =  path +: e.pathComponents))
    inner.validate(f(a))
  }

  def should[C](implicit v: Validator[B, C]): Validator[A, C] = validation[C]
}

object FieldSyntax {

  implicit class ToField[A](val a: A) extends AnyVal {
    def field[B](f: A => B, path: String) = Field(f, path)
  }

}
