package scala.concurrent

import org.junit.Test
import org.junit.Assert._

import scala.concurrent.ExecutionContext.Implicits.global

import scalanative.junit.utils.MultiThreadHelper._

class FutureSuite {
  def getResult[T](delay: Long = eternity)(future: Future[T]): Option[T] = {
    var value: Option[T] = None
    val mutex            = new Object
    future.foreach { v: T =>
      mutex.synchronized {
        value = Some(v)
        mutex.notifyAll()
      }
    }
    if (value.isEmpty) {
      mutex.synchronized {
        if (value.isEmpty) {
          mutex.wait(delay)
        }
      }
    }
    value
  }

  @Test def `Future.successful`(): Unit = {
    val future = Future.successful(3)
    assertEquals(getResult()(future), Some(3))
  }

  @Test def `Future.failed`(): Unit = {
    val future = Future.failed(new NullPointerException("Nothing here"))
    assertEquals(getResult(200)(future), None)
  }

  @Test def `Future.apply`(): Unit = {
    val future = Future(3)
    assertEquals(getResult()(future), Some(3))
  }

  private val futureDelay = 1000
  @Test def `Future.apply delayed`(): Unit = {
    val future = Future {
      Thread.sleep(futureDelay)
      3
    }
    assertEquals(getResult()(future), Some(3))
  }

  @Test def `Future.map`(): Unit = {
    val future = Future(7).map(_ * 191)
    assertEquals(getResult()(future), Some(1337))
  }

  @Test def `Future.map delayed`(): Unit = {
    val future = Future {
      Thread.sleep(futureDelay)
      7
    }.map { x =>
      Thread.sleep(futureDelay)
      x * 191
    }
    assertEquals(getResult()(future), Some(1337))
  }

  @Test def `Future.flatMap instant`(): Unit = {
    val future1 = Future.successful(7)
    val future =
      Future.successful(6).flatMap { b => future1.map { a => a * b } }
    assertEquals(getResult()(future), Some(42))
  }

  @Test def `Future.flatMap`(): Unit = {
    val future1 = Future(7)
    val future  = Future(6).flatMap { b => future1.map { a => a * b } }
    assertEquals(getResult()(future), Some(42))
  }

  @Test def `Future.flatMap delayed`(): Unit = {
    val future1 = Future {
      Thread.sleep(futureDelay)
      7
    }
    val future = Future {
      Thread.sleep(futureDelay)
      6
    }.flatMap { b => future1.map { a => a * b } }
    assertEquals(getResult()(future), Some(42))
  }

  @Test def `Future.reduce instant`(): Unit = {
    val futures =
      Seq(Future.successful(1), Future.successful(2), Future.successful(3))
    val sumFuture = Future.reduce(futures)(_ + _)
    assertEquals(getResult()(sumFuture), Some(6))
  }

  @Test def `Future.reduce`(): Unit = {
    val futures   = Seq(Future(1), Future(2), Future(3))
    val sumFuture = Future.reduce(futures)(_ + _)
    assertEquals(getResult()(sumFuture), Some(6))
  }

  @Test def `Future.reduce delayed`(): Unit = {
    val futures = Seq(Future {
      Thread.sleep(futureDelay)
      1
    }, Future {
      Thread.sleep(futureDelay)
      2
    }, Future {
      Thread.sleep(futureDelay)
      3
    })
    val sumFuture = Future.reduce(futures)(_ + _)
    assertEquals(getResult()(sumFuture), Some(6))
  }

  @Test def `Future.fold instant`(): Unit = {
    val futures =
      Seq(Future.successful(1), Future.successful(2), Future.successful(3))
    val sumFuture = Future.fold(futures)(1)(_ + _)
    assertEquals(getResult()(sumFuture), Some(7))
  }

  @Test def `Future.fold`(): Unit = {
    val futures   = Seq(Future(1), Future(2), Future(3))
    val sumFuture = Future.fold(futures)(1)(_ + _)
    assertEquals(getResult()(sumFuture), Some(7))
  }

  @Test def `Future.fold delayed`(): Unit = {
    val futures = Seq(Future {
      Thread.sleep(futureDelay)
      1
    }, Future {
      Thread.sleep(futureDelay)
      2
    }, Future {
      Thread.sleep(futureDelay)
      3
    })
    val sumFuture = Future.fold(futures)(1)(_ + _)
    assertEquals(getResult()(sumFuture), Some(7))
  }
}
