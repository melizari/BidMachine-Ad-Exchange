package models.swagger

import io.swagger.annotations.ApiModel

@ApiModel
case class PaginatedSequence(items: List[Creative],
                             page: Int,
                             pageSize: Int,
                             count: Int)
