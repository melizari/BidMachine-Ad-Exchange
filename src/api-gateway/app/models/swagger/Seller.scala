package models.swagger

import io.swagger.annotations.ApiModel

@ApiModel
case class Seller(id: Option[Long],
                  ks: Option[String],
                  name: Option[String],
                  fee: Option[Double],
                  active: Option[Boolean] = Some(false))
