package models.swagger

final case class Publisher(id: Option[String] = None,
                           name: Option[String] = None,
                           cat: Option[Vector[String]] = None,
                           domain: Option[String] = None)
