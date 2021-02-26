package scala.scalanative.junit

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

package object async {
  type AsyncResult = Unit
  def await(future: Future[_]): AsyncResult =
    Await.result(future, Duration.Inf)
}
