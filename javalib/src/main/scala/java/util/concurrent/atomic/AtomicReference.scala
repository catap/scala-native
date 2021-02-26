package java.util.concurrent.atomic

import java.util.function.UnaryOperator

import scala.language.implicitConversions
import scala.scalanative.runtime.CAtomicLong
import scala.scalanative.runtime.Intrinsics.{
  castLongToRawPtr,
  castObjectToRawPtr,
  castRawPtrToLong,
  castRawPtrToObject
}

class AtomicReference[T <: AnyRef](initValue: T) extends Serializable {

  def this() = this(null.asInstanceOf[T])

  // XXX Immix and Commix will not mark this reference
  private[this] val inner = CAtomicLong(initValue)

  final def get(): T = inner.load()

  final def set(newValue: T): Unit = inner.store(newValue)

  final def lazySet(newValue: T): Unit = inner.store(newValue)

  final def compareAndSet(expect: T, update: T): Boolean =
    inner.compareAndSwapStrong(expect, update)._1

  final def weakCompareAndSet(expect: T, update: T): Boolean =
    inner.compareAndSwapWeak(expect, update)._1

  final def getAndSet(newValue: T): T = {
    val old = inner.load()
    inner.store(newValue)
    old
  }

  final def getAndUpdate(updateFunction: UnaryOperator[T]): T = {
    var next: T = null.asInstanceOf[T]
    var prev: T = null.asInstanceOf[T]
    while ({
      prev = inner.load()
      next = updateFunction(prev)
      compareAndSet(prev, next)
    }) ()
    next
  }

  final def updateAndGet(updateFunction: UnaryOperator[T]): T = {
    var next: T = null.asInstanceOf[T]
    var prev: T = null.asInstanceOf[T]
    while ({
      prev = inner.load()
      next = updateFunction(prev)
      compareAndSet(prev, next)
    }) ()
    prev
  }

  override def toString(): String =
    String.valueOf(get())

  private implicit def toLong(e: T): Long =
    castRawPtrToLong(castObjectToRawPtr(e))
  private implicit def toRef(l: Long): T =
    castRawPtrToObject(castLongToRawPtr(l)).asInstanceOf[T]
}

object AtomicReference {

  private final val serialVersionUID: Long = -1848883965231344442L

}
