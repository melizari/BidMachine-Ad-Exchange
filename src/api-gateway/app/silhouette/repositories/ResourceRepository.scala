package silhouette.repositories

import scala.concurrent.Future

trait ResourceRepository[R] {

  def retrieve(id: Long): Future[Option[R]]

}
