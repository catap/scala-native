// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 1)
package scala.scalanative
package runtime

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.MultiThreadHelper._

class AtomicSuite {

// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 30)

// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Strong byte`(): Unit = {
    val a = CAtomicByte()

    val b = 3.asInstanceOf[Byte]

    assertFalse(
      a.compareAndSwapStrong(1.asInstanceOf[Byte], 3.asInstanceOf[Byte])._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[Byte], 3.asInstanceOf[Byte])._2 == 3
        .asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Weak byte`(): Unit = {
    val a = CAtomicByte()

    val b = 3.asInstanceOf[Byte]

    assertFalse(
      a.compareAndSwapWeak(1.asInstanceOf[Byte], 3.asInstanceOf[Byte])._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[Byte], 3.asInstanceOf[Byte])._2 == 3
        .asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 60)

  @Test def `compare_and_swap (weak and strong) is atomic for byte`(): Unit = {

    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        @inline def badCaS(expectedValue: Int, newValue: Int): Int = {
          val oldValue = number
          if (number == expectedValue) {
            number = newValue
          }
          oldValue
        }

        var i = n
        val b = 1.asInstanceOf[Byte]
        // making this as fast as possible
        while (i > 0) {
          var newValue = 0
          var expected = 0
          do {
            expected = number
            newValue = expected + b
          } while (badCaS(expected, newValue) != expected)
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[Byte]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[Byte]
            var expected = 0.asInstanceOf[Byte]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[Byte]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapStrong(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[Byte]
            var expected = 0.asInstanceOf[Byte]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[Byte]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapWeak(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 118)
      true
    }
  }

  @Test def `load and store byte`(): Unit = {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.load() == 0.asInstanceOf[Byte])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add byte`(): Unit = {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchAdd(b) == 0.asInstanceOf[Byte])

    assert(a.load() == b)

    a.free()
  }

  @Test def `add_fetch byte`(): Unit = {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add, add_fetch, fetch_sub, sub_fetch are atomic for byte`()
      : Unit = {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        var i = n
        val b = 1.asInstanceOf[Byte]
        // making this as fast as possible
        while (i > 0) {
          number = number + b
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[Byte]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            number.fetchAdd(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            number.addFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            number.fetchSub(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            number.subFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 216)
      true
    }
  }

  @Test def `fetch_sub byte`(): Unit = {
    val a = CAtomicByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  @Test def `sub_fetch byte`(): Unit = {
    val a = CAtomicByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.subFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  @Test def `fetch_and byte`(): Unit = {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchAnd(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  @Test def `and_fetch byte`(): Unit = {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.andFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  @Test def `fetch_or byte`(): Unit = {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchOr(b) == 0.asInstanceOf[Byte])

    assert(a.load() == b)

    a.free()
  }

  @Test def `or_fetch byte`(): Unit = {
    val a = CAtomicByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_xor byte`(): Unit = {
    val a = CAtomicByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchXor(b) == 1.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  @Test def `xor_fetch byte`(): Unit = {
    val a = CAtomicByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.xorFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Strong short`(): Unit = {
    val a = CAtomicShort()

    val b = 3.asInstanceOf[CShort]

    assertFalse(
      a.compareAndSwapStrong(1.asInstanceOf[CShort], 3.asInstanceOf[CShort])._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CShort], 3.asInstanceOf[CShort])
        ._2 == 3.asInstanceOf[CShort])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Weak short`(): Unit = {
    val a = CAtomicShort()

    val b = 3.asInstanceOf[CShort]

    assertFalse(
      a.compareAndSwapWeak(1.asInstanceOf[CShort], 3.asInstanceOf[CShort])._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CShort], 3.asInstanceOf[CShort])
        ._2 == 3.asInstanceOf[CShort])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 60)

  @Test def `compare_and_swap (weak and strong) is atomic for short`(): Unit = {

    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        @inline def badCaS(expectedValue: Int, newValue: Int): Int = {
          val oldValue = number
          if (number == expectedValue) {
            number = newValue
          }
          oldValue
        }

        var i = n
        val b = 1.asInstanceOf[CShort]
        // making this as fast as possible
        while (i > 0) {
          var newValue = 0
          var expected = 0
          do {
            expected = number
            newValue = expected + b
          } while (badCaS(expected, newValue) != expected)
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CShort]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CShort]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CShort]
            var expected = 0.asInstanceOf[CShort]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[CShort]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapStrong(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CShort]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CShort]
            var expected = 0.asInstanceOf[CShort]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[CShort]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapWeak(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 118)
      true
    }
  }

  @Test def `load and store short`(): Unit = {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.load() == 0.asInstanceOf[CShort])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add short`(): Unit = {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CShort])

    assert(a.load() == b)

    a.free()
  }

  @Test def `add_fetch short`(): Unit = {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add, add_fetch, fetch_sub, sub_fetch are atomic for short`()
      : Unit = {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        var i = n
        val b = 1.asInstanceOf[CShort]
        // making this as fast as possible
        while (i > 0) {
          number = number + b
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CShort]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CShort]
          // making this as fast as possible
          while (i > 0) {
            number.fetchAdd(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CShort]
          // making this as fast as possible
          while (i > 0) {
            number.addFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1.asInstanceOf[CShort]
          // making this as fast as possible
          while (i > 0) {
            number.fetchSub(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1.asInstanceOf[CShort]
          // making this as fast as possible
          while (i > 0) {
            number.subFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 216)
      true
    }
  }

  @Test def `fetch_sub short`(): Unit = {
    val a = CAtomicShort(1.asInstanceOf[CShort])

    val b = 1.asInstanceOf[CShort]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }

  @Test def `sub_fetch short`(): Unit = {
    val a = CAtomicShort(1.asInstanceOf[CShort])

    val b = 1.asInstanceOf[CShort]

    assert(a.subFetch(b) == 0.asInstanceOf[CShort])

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }

  @Test def `fetch_and short`(): Unit = {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CShort])

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }

  @Test def `and_fetch short`(): Unit = {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.andFetch(b) == 0.asInstanceOf[CShort])

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }

  @Test def `fetch_or short`(): Unit = {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.fetchOr(b) == 0.asInstanceOf[CShort])

    assert(a.load() == b)

    a.free()
  }

  @Test def `or_fetch short`(): Unit = {
    val a = CAtomicShort()

    val b = 1.asInstanceOf[CShort]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_xor short`(): Unit = {
    val a = CAtomicShort(1.asInstanceOf[CShort])

    val b = 1.asInstanceOf[CShort]

    assert(a.fetchXor(b) == 1.asInstanceOf[CShort])

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }

  @Test def `xor_fetch short`(): Unit = {
    val a = CAtomicShort(1.asInstanceOf[CShort])

    val b = 1.asInstanceOf[CShort]

    assert(a.xorFetch(b) == 0.asInstanceOf[CShort])

    assert(a.load() == 0.asInstanceOf[CShort])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Strong int`(): Unit = {
    val a = CAtomicInt()

    val b = 3

    assertFalse(a.compareAndSwapStrong(1, 3)._1)

    assert(a.compareAndSwapStrong(0, 3)._2 == 3)

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Weak int`(): Unit = {
    val a = CAtomicInt()

    val b = 3

    assertFalse(a.compareAndSwapWeak(1, 3)._1)

    assert(a.compareAndSwapWeak(0, 3)._2 == 3)

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 60)

  @Test def `compare_and_swap (weak and strong) is atomic for int`(): Unit = {

    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        @inline def badCaS(expectedValue: Int, newValue: Int): Int = {
          val oldValue = number
          if (number == expectedValue) {
            number = newValue
          }
          oldValue
        }

        var i = n
        val b = 1
        // making this as fast as possible
        while (i > 0) {
          var newValue = 0
          var expected = 0
          do {
            expected = number
            newValue = expected + b
          } while (badCaS(expected, newValue) != expected)
          i -= 1
        }
      }
      number != (n * numThreads)
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0
            var expected = 0
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b)
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapStrong(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads)
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0
            var expected = 0
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b)
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapWeak(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads)
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 118)
      true
    }
  }

  @Test def `load and store int`(): Unit = {
    val a = CAtomicInt()

    val b = 1

    assert(a.load() == 0)

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add int`(): Unit = {
    val a = CAtomicInt()

    val b = 1

    assert(a.fetchAdd(b) == 0)

    assert(a.load() == b)

    a.free()
  }

  @Test def `add_fetch int`(): Unit = {
    val a = CAtomicInt()

    val b = 1

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add, add_fetch, fetch_sub, sub_fetch are atomic for int`()
      : Unit = {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        var i = n
        val b = 1
        // making this as fast as possible
        while (i > 0) {
          number = number + b
          i -= 1
        }
      }
      number != (n * numThreads)
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1
          // making this as fast as possible
          while (i > 0) {
            number.fetchAdd(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads)
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1
          // making this as fast as possible
          while (i > 0) {
            number.addFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads)
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1
          // making this as fast as possible
          while (i > 0) {
            number.fetchSub(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads)
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1
          // making this as fast as possible
          while (i > 0) {
            number.subFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads)
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 216)
      true
    }
  }

  @Test def `fetch_sub int`(): Unit = {
    val a = CAtomicInt(1)

    val b = 1

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0)

    a.free()
  }

  @Test def `sub_fetch int`(): Unit = {
    val a = CAtomicInt(1)

    val b = 1

    assert(a.subFetch(b) == 0)

    assert(a.load() == 0)

    a.free()
  }

  @Test def `fetch_and int`(): Unit = {
    val a = CAtomicInt()

    val b = 1

    assert(a.fetchAnd(b) == 0)

    assert(a.load() == 0)

    a.free()
  }

  @Test def `and_fetch int`(): Unit = {
    val a = CAtomicInt()

    val b = 1

    assert(a.andFetch(b) == 0)

    assert(a.load() == 0)

    a.free()
  }

  @Test def `fetch_or int`(): Unit = {
    val a = CAtomicInt()

    val b = 1

    assert(a.fetchOr(b) == 0)

    assert(a.load() == b)

    a.free()
  }

  @Test def `or_fetch int`(): Unit = {
    val a = CAtomicInt()

    val b = 1

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_xor int`(): Unit = {
    val a = CAtomicInt(1)

    val b = 1

    assert(a.fetchXor(b) == 1)

    assert(a.load() == 0)

    a.free()
  }

  @Test def `xor_fetch int`(): Unit = {
    val a = CAtomicInt(1)

    val b = 1

    assert(a.xorFetch(b) == 0)

    assert(a.load() == 0)

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Strong long`(): Unit = {
    val a = CAtomicLong()

    val b = 3.asInstanceOf[CLong]

    assertFalse(
      a.compareAndSwapStrong(1.asInstanceOf[CLong], 3.asInstanceOf[CLong])._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CLong], 3.asInstanceOf[CLong])
        ._2 == 3.asInstanceOf[CLong])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Weak long`(): Unit = {
    val a = CAtomicLong()

    val b = 3.asInstanceOf[CLong]

    assertFalse(
      a.compareAndSwapWeak(1.asInstanceOf[CLong], 3.asInstanceOf[CLong])._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CLong], 3.asInstanceOf[CLong])._2 == 3
        .asInstanceOf[CLong])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 60)

  @Test def `compare_and_swap (weak and strong) is atomic for long`(): Unit = {

    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0.asInstanceOf[CLong]
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        @inline def badCaS(expectedValue: CLong, newValue: CLong): CLong = {
          val oldValue = number
          if (number == expectedValue) {
            number = newValue
          }
          oldValue
        }

        var i = n
        val b = 1.asInstanceOf[CLong]
        // making this as fast as possible
        while (i > 0) {
          var newValue = 0.asInstanceOf[CLong]
          var expected = 0.asInstanceOf[CLong]
          do {
            expected = number
            newValue = expected + b
          } while (badCaS(expected, newValue) != expected)
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CLong]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CLong]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CLong]
            var expected = 0.asInstanceOf[CLong]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[CLong]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapStrong(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CLong]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CLong]
            var expected = 0.asInstanceOf[CLong]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[CLong]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapWeak(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 118)
      true
    }
  }

  @Test def `load and store long`(): Unit = {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.load() == 0.asInstanceOf[CLong])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add long`(): Unit = {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CLong])

    assert(a.load() == b)

    a.free()
  }

  @Test def `add_fetch long`(): Unit = {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add, add_fetch, fetch_sub, sub_fetch are atomic for long`()
      : Unit = {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0.asInstanceOf[CLong]
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        var i = n
        val b = 1.asInstanceOf[CLong]
        // making this as fast as possible
        while (i > 0) {
          number = number + b
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CLong]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CLong]
          // making this as fast as possible
          while (i > 0) {
            number.fetchAdd(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CLong]
          // making this as fast as possible
          while (i > 0) {
            number.addFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1.asInstanceOf[CLong]
          // making this as fast as possible
          while (i > 0) {
            number.fetchSub(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1.asInstanceOf[CLong]
          // making this as fast as possible
          while (i > 0) {
            number.subFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 216)
      true
    }
  }

  @Test def `fetch_sub long`(): Unit = {
    val a = CAtomicLong(1.asInstanceOf[CLong])

    val b = 1.asInstanceOf[CLong]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }

  @Test def `sub_fetch long`(): Unit = {
    val a = CAtomicLong(1.asInstanceOf[CLong])

    val b = 1.asInstanceOf[CLong]

    assert(a.subFetch(b) == 0.asInstanceOf[CLong])

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }

  @Test def `fetch_and long`(): Unit = {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CLong])

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }

  @Test def `and_fetch long`(): Unit = {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.andFetch(b) == 0.asInstanceOf[CLong])

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }

  @Test def `fetch_or long`(): Unit = {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.fetchOr(b) == 0.asInstanceOf[CLong])

    assert(a.load() == b)

    a.free()
  }

  @Test def `or_fetch long`(): Unit = {
    val a = CAtomicLong()

    val b = 1.asInstanceOf[CLong]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_xor long`(): Unit = {
    val a = CAtomicLong(1.asInstanceOf[CLong])

    val b = 1.asInstanceOf[CLong]

    assert(a.fetchXor(b) == 1.asInstanceOf[CLong])

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }

  @Test def `xor_fetch long`(): Unit = {
    val a = CAtomicLong(1.asInstanceOf[CLong])

    val b = 1.asInstanceOf[CLong]

    assert(a.xorFetch(b) == 0.asInstanceOf[CLong])

    assert(a.load() == 0.asInstanceOf[CLong])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Strong ubyte`(): Unit = {
    val a = CAtomicUnsignedByte()

    val b = 3.asInstanceOf[Byte]

    assertFalse(
      a.compareAndSwapStrong(1.asInstanceOf[Byte], 3.asInstanceOf[Byte])._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[Byte], 3.asInstanceOf[Byte])._2 == 3
        .asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Weak ubyte`(): Unit = {
    val a = CAtomicUnsignedByte()

    val b = 3.asInstanceOf[Byte]

    assertFalse(
      a.compareAndSwapWeak(1.asInstanceOf[Byte], 3.asInstanceOf[Byte])._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[Byte], 3.asInstanceOf[Byte])._2 == 3
        .asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 60)

  @Test def `compare_and_swap (weak and strong) is atomic for ubyte`(): Unit = {

    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        @inline def badCaS(expectedValue: Int, newValue: Int): Int = {
          val oldValue = number
          if (number == expectedValue) {
            number = newValue
          }
          oldValue
        }

        var i = n
        val b = 1.asInstanceOf[Byte]
        // making this as fast as possible
        while (i > 0) {
          var newValue = 0
          var expected = 0
          do {
            expected = number
            newValue = expected + b
          } while (badCaS(expected, newValue) != expected)
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[Byte]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicUnsignedByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[Byte]
            var expected = 0.asInstanceOf[Byte]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[Byte]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapStrong(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicUnsignedByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[Byte]
            var expected = 0.asInstanceOf[Byte]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[Byte]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapWeak(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 118)
      true
    }
  }

  @Test def `load and store ubyte`(): Unit = {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.load() == 0.asInstanceOf[Byte])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add ubyte`(): Unit = {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchAdd(b) == 0.asInstanceOf[Byte])

    assert(a.load() == b)

    a.free()
  }

  @Test def `add_fetch ubyte`(): Unit = {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add, add_fetch, fetch_sub, sub_fetch are atomic for ubyte`()
      : Unit = {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        var i = n
        val b = 1.asInstanceOf[Byte]
        // making this as fast as possible
        while (i > 0) {
          number = number + b
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[Byte]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicUnsignedByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            number.fetchAdd(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicUnsignedByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            number.addFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicUnsignedByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            number.fetchSub(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicUnsignedByte()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1.asInstanceOf[Byte]
          // making this as fast as possible
          while (i > 0) {
            number.subFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[Byte]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 216)
      true
    }
  }

  @Test def `fetch_sub ubyte`(): Unit = {
    val a = CAtomicUnsignedByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  @Test def `sub_fetch ubyte`(): Unit = {
    val a = CAtomicUnsignedByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.subFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  @Test def `fetch_and ubyte`(): Unit = {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchAnd(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  @Test def `and_fetch ubyte`(): Unit = {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.andFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  @Test def `fetch_or ubyte`(): Unit = {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchOr(b) == 0.asInstanceOf[Byte])

    assert(a.load() == b)

    a.free()
  }

  @Test def `or_fetch ubyte`(): Unit = {
    val a = CAtomicUnsignedByte()

    val b = 1.asInstanceOf[Byte]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_xor ubyte`(): Unit = {
    val a = CAtomicUnsignedByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.fetchXor(b) == 1.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }

  @Test def `xor_fetch ubyte`(): Unit = {
    val a = CAtomicUnsignedByte(1.asInstanceOf[Byte])

    val b = 1.asInstanceOf[Byte]

    assert(a.xorFetch(b) == 0.asInstanceOf[Byte])

    assert(a.load() == 0.asInstanceOf[Byte])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Strong ushort`(): Unit = {
    val a = CAtomicUnsignedShort()

    val b = 3.asInstanceOf[CUnsignedShort]

    assertFalse(
      a.compareAndSwapStrong(1.asInstanceOf[CUnsignedShort],
                              3.asInstanceOf[CUnsignedShort])
        ._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CUnsignedShort],
                              3.asInstanceOf[CUnsignedShort])
        ._2 == 3.asInstanceOf[CUnsignedShort])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Weak ushort`(): Unit = {
    val a = CAtomicUnsignedShort()

    val b = 3.asInstanceOf[CUnsignedShort]

    assertFalse(
      a.compareAndSwapWeak(1.asInstanceOf[CUnsignedShort],
                            3.asInstanceOf[CUnsignedShort])
        ._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CUnsignedShort],
                            3.asInstanceOf[CUnsignedShort])
        ._2 == 3.asInstanceOf[CUnsignedShort])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 60)

  @Test def `compare_and_swap (weak and strong) is atomic for ushort`()
      : Unit = {

    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0.asInstanceOf[CUnsignedInt]
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        @inline def badCaS(expectedValue: UInt, newValue: UInt): UInt = {
          val oldValue = number
          if (number == expectedValue) {
            number = newValue
          }
          oldValue
        }

        var i = n
        val b = 1.asInstanceOf[CUnsignedShort]
        // making this as fast as possible
        while (i > 0) {
          var newValue = 0.asInstanceOf[CUnsignedInt]
          var expected = 0.asInstanceOf[CUnsignedInt]
          do {
            expected = number
            newValue = expected + b
          } while (badCaS(expected, newValue) != expected)
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CUnsignedShort]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicUnsignedShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedShort]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CUnsignedShort]
            var expected = 0.asInstanceOf[CUnsignedShort]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 103)
              newValue = (expected + b).toUShort
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapStrong(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicUnsignedShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedShort]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CUnsignedShort]
            var expected = 0.asInstanceOf[CUnsignedShort]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 103)
              newValue = (expected + b).toUShort
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapWeak(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 118)
      true
    }
  }

  @Test def `load and store ushort`(): Unit = {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add ushort`(): Unit = {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == b)

    a.free()
  }

  @Test def `add_fetch ushort`(): Unit = {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add, add_fetch, fetch_sub, sub_fetch are atomic for ushort`()
      : Unit = {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0.asInstanceOf[CUnsignedInt]
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        var i = n
        val b = 1.asInstanceOf[CUnsignedShort]
        // making this as fast as possible
        while (i > 0) {
          number = number + b
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CUnsignedShort]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicUnsignedShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedShort]
          // making this as fast as possible
          while (i > 0) {
            number.fetchAdd(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicUnsignedShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedShort]
          // making this as fast as possible
          while (i > 0) {
            number.addFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicUnsignedShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = UShort.MaxValue
          // making this as fast as possible
          while (i > 0) {
            number.fetchSub(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicUnsignedShort()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = UShort.MaxValue
          // making this as fast as possible
          while (i > 0) {
            number.subFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedShort]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 216)
      true
    }
  }

  @Test def `fetch_sub ushort`(): Unit = {
    val a = CAtomicUnsignedShort(1.asInstanceOf[CUnsignedShort])

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }

  @Test def `sub_fetch ushort`(): Unit = {
    val a = CAtomicUnsignedShort(1.asInstanceOf[CUnsignedShort])

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.subFetch(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }

  @Test def `fetch_and ushort`(): Unit = {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }

  @Test def `and_fetch ushort`(): Unit = {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.andFetch(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }

  @Test def `fetch_or ushort`(): Unit = {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.fetchOr(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == b)

    a.free()
  }

  @Test def `or_fetch ushort`(): Unit = {
    val a = CAtomicUnsignedShort()

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_xor ushort`(): Unit = {
    val a = CAtomicUnsignedShort(1.asInstanceOf[CUnsignedShort])

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.fetchXor(b) == 1.asInstanceOf[CUnsignedShort])

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }

  @Test def `xor_fetch ushort`(): Unit = {
    val a = CAtomicUnsignedShort(1.asInstanceOf[CUnsignedShort])

    val b = 1.asInstanceOf[CUnsignedShort]

    assert(a.xorFetch(b) == 0.asInstanceOf[CUnsignedShort])

    assert(a.load() == 0.asInstanceOf[CUnsignedShort])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Strong uint`(): Unit = {
    val a = CAtomicUnsignedInt()

    val b = 3.asInstanceOf[CUnsignedInt]

    assertFalse(
      a.compareAndSwapStrong(1.asInstanceOf[CUnsignedInt],
                              3.asInstanceOf[CUnsignedInt])
        ._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CUnsignedInt],
                              3.asInstanceOf[CUnsignedInt])
        ._2 == 3.asInstanceOf[CUnsignedInt])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Weak uint`(): Unit = {
    val a = CAtomicUnsignedInt()

    val b = 3.asInstanceOf[CUnsignedInt]

    assertFalse(
      a.compareAndSwapWeak(1.asInstanceOf[CUnsignedInt],
                            3.asInstanceOf[CUnsignedInt])
        ._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CUnsignedInt],
                            3.asInstanceOf[CUnsignedInt])
        ._2 == 3.asInstanceOf[CUnsignedInt])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 60)

  @Test def `compare_and_swap (weak and strong) is atomic for uint`(): Unit = {

    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0.asInstanceOf[CUnsignedInt]
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        @inline def badCaS(expectedValue: CUnsignedInt,
                           newValue: CUnsignedInt): CUnsignedInt = {
          val oldValue = number
          if (number == expectedValue) {
            number = newValue
          }
          oldValue
        }

        var i = n
        val b = 1.asInstanceOf[CUnsignedInt]
        // making this as fast as possible
        while (i > 0) {
          var newValue = 0.asInstanceOf[CUnsignedInt]
          var expected = 0.asInstanceOf[CUnsignedInt]
          do {
            expected = number
            newValue = expected + b
          } while (badCaS(expected, newValue) != expected)
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CUnsignedInt]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicUnsignedInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedInt]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CUnsignedInt]
            var expected = 0.asInstanceOf[CUnsignedInt]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[CUnsignedInt]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapStrong(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedInt]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicUnsignedInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedInt]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CUnsignedInt]
            var expected = 0.asInstanceOf[CUnsignedInt]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[CUnsignedInt]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapWeak(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedInt]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 118)
      true
    }
  }

  @Test def `load and store uint`(): Unit = {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add uint`(): Unit = {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == b)

    a.free()
  }

  @Test def `add_fetch uint`(): Unit = {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add, add_fetch, fetch_sub, sub_fetch are atomic for uint`()
      : Unit = {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0.asInstanceOf[CUnsignedInt]
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        var i = n
        val b = 1.asInstanceOf[CUnsignedInt]
        // making this as fast as possible
        while (i > 0) {
          number = number + b
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CUnsignedInt]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicUnsignedInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedInt]
          // making this as fast as possible
          while (i > 0) {
            number.fetchAdd(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedInt]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicUnsignedInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedInt]
          // making this as fast as possible
          while (i > 0) {
            number.addFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedInt]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicUnsignedInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = UInt.MaxValue
          // making this as fast as possible
          while (i > 0) {
            number.fetchSub(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedInt]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicUnsignedInt()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = UInt.MaxValue
          // making this as fast as possible
          while (i > 0) {
            number.subFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedInt]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 216)
      true
    }
  }

  @Test def `fetch_sub uint`(): Unit = {
    val a = CAtomicUnsignedInt(1.asInstanceOf[CUnsignedInt])

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }

  @Test def `sub_fetch uint`(): Unit = {
    val a = CAtomicUnsignedInt(1.asInstanceOf[CUnsignedInt])

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.subFetch(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }

  @Test def `fetch_and uint`(): Unit = {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }

  @Test def `and_fetch uint`(): Unit = {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.andFetch(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }

  @Test def `fetch_or uint`(): Unit = {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.fetchOr(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == b)

    a.free()
  }

  @Test def `or_fetch uint`(): Unit = {
    val a = CAtomicUnsignedInt()

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_xor uint`(): Unit = {
    val a = CAtomicUnsignedInt(1.asInstanceOf[CUnsignedInt])

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.fetchXor(b) == 1.asInstanceOf[CUnsignedInt])

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }

  @Test def `xor_fetch uint`(): Unit = {
    val a = CAtomicUnsignedInt(1.asInstanceOf[CUnsignedInt])

    val b = 1.asInstanceOf[CUnsignedInt]

    assert(a.xorFetch(b) == 0.asInstanceOf[CUnsignedInt])

    assert(a.load() == 0.asInstanceOf[CUnsignedInt])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Strong ulong`(): Unit = {
    val a = CAtomicUnsignedLong()

    val b = 3.asInstanceOf[CUnsignedLong]

    assertFalse(
      a.compareAndSwapStrong(1.asInstanceOf[CUnsignedLong],
                              3.asInstanceOf[CUnsignedLong])
        ._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CUnsignedLong],
                              3.asInstanceOf[CUnsignedLong])
        ._2 == 3.asInstanceOf[CUnsignedLong])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Weak ulong`(): Unit = {
    val a = CAtomicUnsignedLong()

    val b = 3.asInstanceOf[CUnsignedLong]

    assertFalse(
      a.compareAndSwapWeak(1.asInstanceOf[CUnsignedLong],
                            3.asInstanceOf[CUnsignedLong])
        ._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CUnsignedLong],
                            3.asInstanceOf[CUnsignedLong])
        ._2 == 3.asInstanceOf[CUnsignedLong])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 60)

  @Test def `compare_and_swap (weak and strong) is atomic for ulong`(): Unit = {

    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0.asInstanceOf[CUnsignedLong]
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        @inline def badCaS(expectedValue: CUnsignedLong,
                           newValue: CUnsignedLong): CUnsignedLong = {
          val oldValue = number
          if (number == expectedValue) {
            number = newValue
          }
          oldValue
        }

        var i = n
        val b = 1.asInstanceOf[CUnsignedLong]
        // making this as fast as possible
        while (i > 0) {
          var newValue = 0.asInstanceOf[CUnsignedLong]
          var expected = 0.asInstanceOf[CUnsignedLong]
          do {
            expected = number
            newValue = expected + b
          } while (badCaS(expected, newValue) != expected)
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CUnsignedLong]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicUnsignedLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedLong]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CUnsignedLong]
            var expected = 0.asInstanceOf[CUnsignedLong]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[CUnsignedLong]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapStrong(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicUnsignedLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedLong]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CUnsignedLong]
            var expected = 0.asInstanceOf[CUnsignedLong]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[CUnsignedLong]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapWeak(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 118)
      true
    }
  }

  @Test def `load and store ulong`(): Unit = {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add ulong`(): Unit = {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == b)

    a.free()
  }

  @Test def `add_fetch ulong`(): Unit = {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add, add_fetch, fetch_sub, sub_fetch are atomic for ulong`()
      : Unit = {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0.asInstanceOf[CUnsignedLong]
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        var i = n
        val b = 1.asInstanceOf[CUnsignedLong]
        // making this as fast as possible
        while (i > 0) {
          number = number + b
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CUnsignedLong]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicUnsignedLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedLong]
          // making this as fast as possible
          while (i > 0) {
            number.fetchAdd(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicUnsignedLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CUnsignedLong]
          // making this as fast as possible
          while (i > 0) {
            number.addFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicUnsignedLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = ULong.MaxValue
          // making this as fast as possible
          while (i > 0) {
            number.fetchSub(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicUnsignedLong()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = ULong.MaxValue
          // making this as fast as possible
          while (i > 0) {
            number.subFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CUnsignedLong]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 216)
      true
    }
  }

  @Test def `fetch_sub ulong`(): Unit = {
    val a = CAtomicUnsignedLong(1.asInstanceOf[CUnsignedLong])

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }

  @Test def `sub_fetch ulong`(): Unit = {
    val a = CAtomicUnsignedLong(1.asInstanceOf[CUnsignedLong])

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.subFetch(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }

  @Test def `fetch_and ulong`(): Unit = {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }

  @Test def `and_fetch ulong`(): Unit = {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.andFetch(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }

  @Test def `fetch_or ulong`(): Unit = {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.fetchOr(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == b)

    a.free()
  }

  @Test def `or_fetch ulong`(): Unit = {
    val a = CAtomicUnsignedLong()

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_xor ulong`(): Unit = {
    val a = CAtomicUnsignedLong(1.asInstanceOf[CUnsignedLong])

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.fetchXor(b) == 1.asInstanceOf[CUnsignedLong])

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }

  @Test def `xor_fetch ulong`(): Unit = {
    val a = CAtomicUnsignedLong(1.asInstanceOf[CUnsignedLong])

    val b = 1.asInstanceOf[CUnsignedLong]

    assert(a.xorFetch(b) == 0.asInstanceOf[CUnsignedLong])

    assert(a.load() == 0.asInstanceOf[CUnsignedLong])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Strong csize`(): Unit = {
    val a = CAtomicCSize()

    val b = 3.asInstanceOf[CSize]

    assertFalse(
      a.compareAndSwapStrong(1.asInstanceOf[CSize], 3.asInstanceOf[CSize])._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CSize], 3.asInstanceOf[CSize])
        ._2 == 3.asInstanceOf[CSize])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 47)

  @Test def `compare and swap Weak csize`(): Unit = {
    val a = CAtomicCSize()

    val b = 3.asInstanceOf[CSize]

    assertFalse(
      a.compareAndSwapWeak(1.asInstanceOf[CSize], 3.asInstanceOf[CSize])._1)

    assert(
      a.compareAndSwapWeak(0.asInstanceOf[CSize], 3.asInstanceOf[CSize])._2 == 3
        .asInstanceOf[CSize])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 60)

  @Test def `compare_and_swap (weak and strong) is atomic for csize`(): Unit = {

    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0.asInstanceOf[CSize]
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        @inline def badCaS(expectedValue: CSize, newValue: CSize): CSize = {
          val oldValue = number
          if (number == expectedValue) {
            number = newValue
          }
          oldValue
        }

        var i = n
        val b = 1.asInstanceOf[CSize]
        // making this as fast as possible
        while (i > 0) {
          var newValue = 0.asInstanceOf[CSize]
          var expected = 0.asInstanceOf[CSize]
          do {
            expected = number
            newValue = expected + b
          } while (badCaS(expected, newValue) != expected)
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CSize]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicCSize()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CSize]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CSize]
            var expected = 0.asInstanceOf[CSize]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[CSize]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapStrong(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CSize]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 91)
      {
        val number = CAtomicCSize()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CSize]
          // making this as fast as possible
          while (i > 0) {
            var newValue = 0.asInstanceOf[CSize]
            var expected = 0.asInstanceOf[CSize]
            do {
              expected = number.load()
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 105)
              newValue = (expected + b).asInstanceOf[CSize]
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 107)
            } while (!number.compareAndSwapWeak(expected, newValue)._1)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CSize]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 118)
      true
    }
  }

  @Test def `load and store csize`(): Unit = {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.load() == 0.asInstanceOf[CSize])

    a.store(b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add csize`(): Unit = {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.fetchAdd(b) == 0.asInstanceOf[CSize])

    assert(a.load() == b)

    a.free()
  }

  @Test def `add_fetch csize`(): Unit = {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.addFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_add, add_fetch, fetch_sub, sub_fetch are atomic for csize`()
      : Unit = {
    val numThreads = 2
    testWithMinRepetitions() { n: Int =>
      var number = 0.asInstanceOf[CSize]
      withThreads(numThreads, label = "CounterExample") { _: Int =>
        var i = n
        val b = 1.asInstanceOf[CSize]
        // making this as fast as possible
        while (i > 0) {
          number = number + b
          i -= 1
        }
      }
      number != (n * numThreads).asInstanceOf[CSize]
    } { n: Int =>
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicCSize()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CSize]
          // making this as fast as possible
          while (i > 0) {
            number.fetchAdd(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CSize]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 178)
      {
        val number = CAtomicCSize()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = 1.asInstanceOf[CSize]
          // making this as fast as possible
          while (i > 0) {
            number.addFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CSize]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicCSize()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1.asInstanceOf[CSize]
          // making this as fast as possible
          while (i > 0) {
            number.fetchSub(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CSize]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 198)
      {
        val number = CAtomicCSize()
        withThreads(numThreads, label = "Test") { _: Int =>
          var i = n
          val b = -1.asInstanceOf[CSize]
          // making this as fast as possible
          while (i > 0) {
            number.subFetch(b)
            i -= 1
          }
        }

        val value    = number.load()
        val expected = (n * numThreads).asInstanceOf[CSize]
        number.free()
        assertEquals(value, expected)
      }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 216)
      true
    }
  }

  @Test def `fetch_sub csize`(): Unit = {
    val a = CAtomicCSize(1.asInstanceOf[CSize])

    val b = 1.asInstanceOf[CSize]

    assert(a.fetchSub(b) == b)

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }

  @Test def `sub_fetch csize`(): Unit = {
    val a = CAtomicCSize(1.asInstanceOf[CSize])

    val b = 1.asInstanceOf[CSize]

    assert(a.subFetch(b) == 0.asInstanceOf[CSize])

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }

  @Test def `fetch_and csize`(): Unit = {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.fetchAnd(b) == 0.asInstanceOf[CSize])

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }

  @Test def `and_fetch csize`(): Unit = {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.andFetch(b) == 0.asInstanceOf[CSize])

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }

  @Test def `fetch_or csize`(): Unit = {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.fetchOr(b) == 0.asInstanceOf[CSize])

    assert(a.load() == b)

    a.free()
  }

  @Test def `or_fetch csize`(): Unit = {
    val a = CAtomicCSize()

    val b = 1.asInstanceOf[CSize]

    assert(a.orFetch(b) == b)

    assert(a.load() == b)

    a.free()
  }

  @Test def `fetch_xor csize`(): Unit = {
    val a = CAtomicCSize(1.asInstanceOf[CSize])

    val b = 1.asInstanceOf[CSize]

    assert(a.fetchXor(b) == 1.asInstanceOf[CSize])

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }

  @Test def `xor_fetch csize`(): Unit = {
    val a = CAtomicCSize(1.asInstanceOf[CSize])

    val b = 1.asInstanceOf[CSize]

    assert(a.xorFetch(b) == 0.asInstanceOf[CSize])

    assert(a.load() == 0.asInstanceOf[CSize])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 316)

// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 320)

  @Test def `compare and swap Strong Char`(): Unit = {
    val a = CAtomicChar()

    val b = 'b'.asInstanceOf[CChar]

    assertFalse(
      a.compareAndSwapStrong('b'.asInstanceOf[CChar], 'b'.asInstanceOf[CChar])
        ._1)

    assert(
      a.compareAndSwapStrong('a'.asInstanceOf[CChar], 'b'.asInstanceOf[CChar])
        ._2 == 'b'.asInstanceOf[CChar])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 320)

  @Test def `compare and swap Weak Char`(): Unit = {
    val a = CAtomicChar()

    val b = 'b'.asInstanceOf[CChar]

    assertFalse(
      a.compareAndSwapWeak('b'.asInstanceOf[CChar], 'b'.asInstanceOf[CChar])._1)

    assert(
      a.compareAndSwapWeak('a'.asInstanceOf[CChar], 'b'.asInstanceOf[CChar])
        ._2 == 'b'.asInstanceOf[CChar])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 320)

  @Test def `compare and swap Strong UnsignedChar`(): Unit = {
    val a = CAtomicUnsignedChar()

    val b = 'b'.asInstanceOf[CUnsignedChar]

    assertFalse(
      a.compareAndSwapStrong('b'.asInstanceOf[CUnsignedChar],
                              'b'.asInstanceOf[CUnsignedChar])
        ._1)

    assert(
      a.compareAndSwapStrong('a'.asInstanceOf[CUnsignedChar],
                              'b'.asInstanceOf[CUnsignedChar])
        ._2 == 'b'.asInstanceOf[CUnsignedChar])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 320)

  @Test def `compare and swap Weak UnsignedChar`(): Unit = {
    val a = CAtomicUnsignedChar()

    val b = 'b'.asInstanceOf[CUnsignedChar]

    assertFalse(
      a.compareAndSwapWeak('b'.asInstanceOf[CUnsignedChar],
                            'b'.asInstanceOf[CUnsignedChar])
        ._1)

    assert(
      a.compareAndSwapWeak('a'.asInstanceOf[CUnsignedChar],
                            'b'.asInstanceOf[CUnsignedChar])
        ._2 == 'b'.asInstanceOf[CUnsignedChar])

    a.free()
  }
// ###sourceLocation(file: "/Users/catap/src/scala-native/unit-tests/src/test/scala/scala/scalanative/runtime/AtomicSuite.scala.gyb", line: 334)

  @Test def `multiple compare and swap should yield correct results`(): Unit = {
    val a = CAtomicInt()

    var i = 0

    while (i < 10) {
      assert(a.compareAndSwapStrong(i, i + 1)._2 == i + 1)
      i += 1
    }

    assert(a.load() == 10)

    a.free()
  }

  @Test def `store behaves as expected`(): Unit = {
    val a = CAtomicInt()

    a.store(1)

    assert(a.load() == 1)

    a.free()
  }

  @Test def `constructor with initial value`(): Unit = {
    val a = CAtomicLong(2.toLong)

    assert(a.load() == 2.toLong)

    a.free()
  }

}
