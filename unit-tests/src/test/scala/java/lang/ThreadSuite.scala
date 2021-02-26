package java.lang

import java.io.{OutputStream, PrintStream}

import org.junit.Test
import org.junit.Assert._

import scala.collection.mutable

import scalanative.junit.utils.AssertThrows._
import scalanative.junit.utils.MultiThreadHelper._

class ThreadSuite {
  @Test def `Empty constructor`(): Unit = {
    val thread = new Thread
    //just do not crash
    thread.start()
    thread.join()
  }

  @Test def `ThreadGroup and name constructor`(): Unit = {
    val name = "Dave"
    val threadGroup = new ThreadGroup("MyGroup")
    val thread = new Thread(threadGroup, name)
    assertEquals(thread.getName, name)
    assertEquals(thread.getThreadGroup, threadGroup)
    thread.start()
    assertEquals(thread.getName, name)
    // threadGroup may be null or the value, because it may have already ended
    thread.join()
    assertEquals(thread.getName, name)
    assertEquals(thread.getThreadGroup, null)
  }

  @Test def `Name constructor`(): Unit = {
    val name = "Dave"
    val thread = new Thread(name)
    assertEquals(thread.getName, name)
    thread.start()
    assertEquals(thread.getName, name)
    thread.join()
    assertEquals(thread.getName, name)
  }

  @Test def `Runnable and name constructor`(): Unit = {
    val name = "Dave"
    var didSomething = false
    val runnable = new Runnable {
      def run(): Unit = {
        didSomething = true
      }
    }
    val thread = new Thread(runnable, name)
    assertEquals(thread.getName, name)
    thread.start()
    assertEquals(thread.getName, name)
    thread.join()
    assert(didSomething)
    assertEquals(thread.getName, name)
  }

  @Test def `Threadgroup, Runnable and name constructor`(): Unit = {
    val name = "Dave"
    var didSomething = false
    val threadGroup = new ThreadGroup("MyGroup")
    val runnable = new Runnable {
      def run(): Unit = {
        didSomething = true
      }
    }
    val thread = new Thread(threadGroup, runnable, name)
    assertEquals(thread.getName, name)
    assertEquals(thread.getThreadGroup, threadGroup)
    thread.start()
    assertEquals(thread.getName, name)
    // threadGroup may be null or the value, because it may have already ended
    thread.join()
    assert(didSomething)
    assertEquals(thread.getName, name)
    assertEquals(thread.getThreadGroup, null)
  }

  @Test def `Threadgroup, Runnable, name and stackSize constructor`(): Unit = {
    val name = "Dave"
    var didSomething = false
    val threadGroup = new ThreadGroup("MyGroup")
    val runnable = new Runnable {
      def run(): Unit = {
        didSomething = true
      }
    }
    val stackSize = 6L * 1024L * 1024L
    val thread = new Thread(threadGroup, runnable, name, stackSize)
    // no way to check stack size yet, just do not crash
    assertEquals(thread.getName, name)
    assertEquals(thread.getThreadGroup, threadGroup)
    thread.start()
    assertEquals(thread.getName, name)
    // threadGroup may be null or the value, because it may have already ended
    thread.join()
    assert(didSomething)
    assertEquals(thread.getName, name)
    assertEquals(thread.getThreadGroup, null)
  }

  @Test def `Priority constants are valid`(): Unit = {

    val max  = Thread.MAX_PRIORITY
    val min  = Thread.MIN_PRIORITY
    val norm = Thread.NORM_PRIORITY

    assert(min <= max)
    assert(min <= norm)
    assert(norm <= max)
  }

  @Test def `Get/Set Priority work as it should with currentThread`(): Unit = {
    val currentThread = Thread.currentThread()
    val old           = currentThread.getPriority
    try {
      currentThread.setPriority(3)
      assert(currentThread.getPriority == 3)
    } finally {
      currentThread.setPriority(old)
    }
  }

  @Test def `Get/Set Priority work as it should with other thread`(): Unit = {
    val thread = new Thread()

    thread.setPriority(2)
    assert(thread.getPriority == 2)
  }

  @Test def `Thread.yield does not crash`(): Unit = {
    Thread.`yield`()
    val thread = new Thread {
      override def run() = Thread.`yield`()
    }
    thread.start()
    thread.join()
  }

  @Test def `Thread.checkAccess does not crash`(): Unit = {
    Thread.currentThread().checkAccess()
    val thread = new Thread {
      override def run() = checkAccess()
    }
    thread.start()
    thread.join()
  }

  @Test def `GC should not crash with multiple threads`(): Unit = {
    val muncher1 = new MemoryMuncher(10000)
    val muncher2 = new MemoryMuncher(10000)
    muncher1.start()
    muncher2.start()
    muncher1.join()
    muncher2.join()
  }

  @Test def `sleep suspends execution by at least the requested amount`()
      : Unit = {
    val millisecondTests = Seq(0, 1, 5, 100)
    millisecondTests.foreach { ms =>
      takesAtLeast(ms) {
        Thread.sleep(ms)
      }
    }
    millisecondTests.foreach { ms =>
      takesAtLeast(ms) {
        Thread.sleep(ms, 0)
      }
    }

    val tests = Seq(0 -> 0,
                    0   -> 1,
                    0   -> 999999,
                    1   -> 0,
                    1   -> 1,
                    5   -> 0,
                    100 -> 0,
                    100 -> 50)

    tests.foreach {
      case (ms, nanos) =>
        takesAtLeast(ms, nanos) {
          Thread.sleep(ms, nanos)
        }
    }
  }

  @Test def `wait suspends execution by at least the requested amount`()
      : Unit = {
    val mutex            = new Object()
    val millisecondTests = Seq(0, 1, 5, 100, 1000)
    millisecondTests.foreach { ms =>
      mutex.synchronized {
        takesAtLeast(ms) {
          mutex.wait(ms)
        }
      }
    }
    millisecondTests.foreach { ms =>
      mutex.synchronized {
        takesAtLeast(ms) {
          mutex.wait(ms, 0)
        }
      }
    }

    val tests = Seq(0 -> 0,
                    0   -> 1,
                    0   -> 999999,
                    1   -> 0,
                    1   -> 1,
                    5   -> 0,
                    100 -> 0,
                    100 -> 50)

    tests.foreach {
      case (ms, nanos) =>
        mutex.synchronized {
          takesAtLeast(ms, nanos) {
            mutex.wait(ms, nanos)
          }
        }
    }
  }

  @Test def `wait duration should not be reduced by getStackTrace`(): Unit = {
    val mainThread = Thread.currentThread()
    val mutex      = new Object
    val wantsStackTrace = new Thread {
      override def run() = {
        eventuallyEquals()(mainThread.getState, Thread.State.WAITING)
        mainThread.getStackTrace
      }
    }
    wantsStackTrace.start()
    mutex.synchronized {
      takesAtLeast(2000) {
        mutex.wait(2000)
      }
    }
  }

  @Test def `Thread should be able to change a shared var`(): Unit = {
    var shared: Int = 0
    new Thread(new Runnable {
      def run(): Unit = {
        shared = 1
      }
    }).start()
    eventuallyEquals()(shared, 1)
  }

  @Test def `Thread should be able to change its internal state`(): Unit = {
    class StatefulThread extends Thread {
      var internal = 0
      override def run() = {
        internal = 1
      }
    }
    val t = new StatefulThread
    t.start()
    eventuallyEquals()(t.internal, 1)
  }

  @Test def `Thread should be able to change runnable's internal state`()
      : Unit = {
    class StatefulRunnable extends Runnable {
      var internal = 0
      def run(): Unit = {
        internal = 1
      }
    }
    val runnable = new StatefulRunnable
    new Thread(runnable).start()
    eventuallyEquals()(runnable.internal, 1)
  }

  @Test def `Thread should be able to call a method`(): Unit = {
    object hasTwoArgMethod {
      var timesCalled = 0
      def call(arg: String, arg2: Int): Unit = {
        assertEquals("abc", arg)
        assertEquals(123, arg2)
        synchronized {
          timesCalled += 1
        }
      }
    }
    val t = new Thread(new Runnable {
      def run(): Unit = {
        hasTwoArgMethod.call("abc", 123)
        hasTwoArgMethod.call("abc", 123)
      }
    })
    t.start()
    t.join(eternity)
    assertEquals(hasTwoArgMethod.timesCalled, 2)
  }

  @Test def `Exceptions in Threads should be handled`(): Unit = {
    val exception = new NullPointerException("There must be a null somewhere")
    val thread = new Thread(new Runnable {
      def run(): Unit = {
        throw exception
      }
    })
    val detector = new ExceptionDetector(thread, exception)
    thread.setUncaughtExceptionHandler(detector)
    assertEquals(thread.getUncaughtExceptionHandler, detector)

    thread.start()
    eventually()(detector.wasException)
  }

  @Test def `Exceptions in Threads should be handled 2`(): Unit = {
    val exception = new NullPointerException("There must be a null somewhere")
    val thread = new Thread(new Runnable {
      def run(): Unit = {
        throw exception
      }
    })
    val detector = new ExceptionDetector(thread, exception)
    withExceptionHandler(detector) {
      assertEquals(Thread.getDefaultUncaughtExceptionHandler, detector)
      thread.start()
      thread.join()
    }
    thread.join(eternity)
    assert(detector.wasException)
  }

  @Test def `Thread.join(ms) should wait until timeout`(): Unit = {
    val thread = new Thread {
      override def run(): Unit = {
        Thread.sleep(2000)
      }
    }
    thread.start()
    takesAtLeast(100) {
      thread.join(100)
    }
    assert(thread.isAlive)
  }

  @Test def `Thread.join(ms,ns) should wait until timeout`(): Unit = {
    val thread = new Thread {
      override def run(): Unit = {
        Thread.sleep(2000)
      }
    }
    thread.start()
    takesAtLeast(100, 50) {
      thread.join(100, 50)
    }
    assert(thread.isAlive)
  }

  @Test def `Thread.join should wait for thread finishing`(): Unit = {
    val thread = new Thread {
      override def run(): Unit = {
        Thread.sleep(100)
      }
    }
    thread.start()
    thread.join(1000)
    assertFalse(thread.isAlive)
  }
  @Test def `Thread.join should wait for thread finishing (no timeout)`()
      : Unit = {

    val thread = new Thread {
      override def run(): Unit = {
        Thread.sleep(100)
      }
    }
    thread.start()
    thread.join()
    assertFalse(thread.isAlive)
  }

  @Test def `Thread.getState and Thread.isAlive`(): Unit = {
    var goOn = true
    val thread = new Thread {
      override def run(): Unit = {
        while (goOn) {}
      }
    }
    assertEquals(Thread.State.NEW, thread.getState)
    thread.start()
    assert(thread.isAlive)
    assertEquals(Thread.State.RUNNABLE, thread.getState)
    goOn = false
    thread.join()
    assertEquals(Thread.State.TERMINATED, thread.getState)
    assertFalse(thread.isAlive)
  }

  @Test def `Thread.clone should fail`(): Unit = {
    val thread = new Thread("abc")
    expectThrows(classOf[CloneNotSupportedException], thread.clone())
  }

  @Test def `Synchronized block should be executed by at most 1 thread`()
      : Unit = {
    testWithMinDelay() { delay =>
      // counter example
      var tmp = 0
      withThreads() { _ =>
        tmp *= 2
        tmp += 1
        Thread.sleep(delay)
        tmp -= 1
      }

      // counterexample succeeds if we get a bad value
      tmp != 0
    } { delay =>
      // test
      val mutex = new Object
      var tmp   = 0
      withThreads() { _ =>
        mutex.synchronized {
          tmp *= 2
          tmp += 1
          Thread.sleep(delay)
          tmp -= 1
        }
      }
      // sychronized should preserve the invariant
      tmp == 0
    }

  }

  @Test def `Thread.currentThread`(): Unit = {
    new Thread {
      override def run(): Unit = {
        assertEquals(this, Thread.currentThread())
      }
    }.start()
    assert(Thread.currentThread() != null)
  }

  @Test def `wait-notify`(): Unit = {
    val mutex = new Object
    new Thread {
      override def run() = {
        Thread.sleep(100)
        mutex.synchronized {
          mutex.notify()
        }
      }
    }.start()
    mutex.synchronized {
      mutex.wait(1000)
    }
  }
  @Test def `wait-notify 2`(): Unit = {
    val mutex         = new Object
    val waiter1       = new WaitingThread(mutex, name = "wait-notify 1")
    val waiter2       = new WaitingThread(mutex, name = "wait-notify 2")
    def timesNotified = waiter1.timesNotified + waiter2.timesNotified
    waiter1.start()
    waiter2.start()
    assertEquals(timesNotified, 0)
    eventuallyEquals(label = "waiter1.getState == Thread.State.WAITING")(
      waiter1.getState,
      Thread.State.WAITING)
    eventuallyEquals(label = "waiter2.getState == Thread.State.WAITING")(
      waiter2.getState,
      Thread.State.WAITING)
    mutex.synchronized {
      mutex.notify()
    }
    eventuallyEquals(label = "timesNotified == 1")(timesNotified, 1)
    mutex.synchronized {
      mutex.notify()
    }
    eventuallyEquals(label = "timesNotified == 2")(timesNotified, 2)
  }
  @Test def `wait-notifyAll`(): Unit = {
    val mutex         = new Object
    val waiter1       = new WaitingThread(mutex, name = "wait-notifyAll 1")
    val waiter2       = new WaitingThread(mutex, name = "wait-notifyAll 2")
    def timesNotified = waiter1.timesNotified + waiter2.timesNotified
    waiter1.start()
    waiter2.start()
    assertEquals(timesNotified, 0)
    eventuallyEquals(label = "waiter1.getState == Thread.State.WAITING")(
      waiter1.getState,
      Thread.State.WAITING)
    eventuallyEquals(label = "waiter2.getState == Thread.State.WAITING")(
      waiter2.getState,
      Thread.State.WAITING)
    mutex.synchronized {
      mutex.notifyAll()
    }
    eventuallyEquals(label = "timesNotified == 2")(timesNotified, 2)
  }
  @Test def `Object.wait puts the Thread into TIMED_WAITING state`(): Unit = {
    val mutex = new Object
    var goOn  = true
    val thread = new Thread {
      override def run() = {
        mutex.synchronized {
          takesAtLeast(1000) {
            mutex.wait(1000)
          }
        }
        while (goOn) {}
      }
    }
    thread.start()
    eventuallyEquals(label = "thread.getState == Thread.State.TIMED_WAITING")(
      thread.getState,
      Thread.State.TIMED_WAITING)
    thread.synchronized {
      thread.notify()
    }
    eventuallyEquals(label = "thread.getState == Thread.State.RUNNABLE")(
      thread.getState,
      Thread.State.RUNNABLE)
    goOn = false
    eventuallyEquals(label = "thread.getState == Thread.State.TERMINATED")(
      thread.getState,
      Thread.State.TERMINATED)
  }
  @Test def `Multiple locks should not conflict`(): Unit = {
    val mutex1     = new Object
    val mutex2     = new Object
    var goOn       = true
    var doingStuff = false
    val waiter = new Thread {
      override def run() =
        mutex1.synchronized {
          while (goOn) {
            doingStuff = true
            Thread.sleep(10)
          }
        }
    }
    waiter.start()
    eventually()(doingStuff)

    val stopper = new Thread {
      override def run() = {
        Thread.sleep(100)
        mutex2.synchronized {
          goOn = false
        }
      }
    }
    stopper.start()
    stopper.join(eternity)
    assertFalse(stopper.isAlive)
  }

  @Test def `Thread.interrupt should interrupt sleep`(): Unit = {
    val thread = new Thread {
      override def run() = {
        expectThrows(classOf[InterruptedException], Thread.sleep(10 * eternity))
      }
    }
    thread.start()
    eventuallyEquals()(Thread.State.TIMED_WAITING, thread.getState)
    thread.interrupt()
    eventuallyEquals()(Thread.State.TERMINATED, thread.getState)
  }
  @Test def `Thread.interrupt should interrupt between calculations`(): Unit = {
    var doingStuff = false
    val thread = new Thread {
      override def run() = {
        while (!Thread.interrupted()) {
          doingStuff = true
          //some intense calculation
          scala.collection.immutable.Range(1, 10000, 1).reduce(_ + _)
        }
      }
    }
    thread.start()
    eventually()(doingStuff)
    thread.interrupt()
    eventuallyEquals()(Thread.State.TERMINATED, thread.getState)
  }

  def withErr[T](stream: OutputStream)(f: => T): T = {
    val old = System.err
    try {
      System.setErr(new PrintStream(stream))
      f
    } finally {
      System.setErr(old)
    }
  }

  @Test def `Thread.dumpStack should contain the method name`(): Unit = {
    object Something {
      def aMethodWithoutAnInterestingName = {
        Thread.dumpStack()
      }
    }
    val outputStream = new java.io.ByteArrayOutputStream()
    withErr(outputStream) {
      Something.aMethodWithoutAnInterestingName
    }
    assert(outputStream.toString.contains("aMethodWithoutAnInterestingName"))
  }
  @Test def `currentThread().getStackTrace should contain the running method name`()
      : Unit = {
    object Something {
      def aMethodWithoutAnInterestingName = {
        Thread.currentThread().getStackTrace
      }
    }
    val rawStackTrace = Something.aMethodWithoutAnInterestingName
    assert(
      mutable.WrappedArray
        .make[StackTraceElement](rawStackTrace)
        .exists(_.getMethodName == "aMethodWithoutAnInterestingName"))
  }
  @Test def `Thread.getAllStackTraces should contain our own thread`(): Unit = {
    object Something {
      def aMethodWithoutAnInterestingName = {
        Thread.getAllStackTraces
      }
    }
    val rawStackTraces = Something.aMethodWithoutAnInterestingName
    val currentThread  = Thread.currentThread()
    assert(rawStackTraces.containsKey(currentThread))
    val currentThreadStackTrace = rawStackTraces.get(currentThread)
    assert(
      mutable.WrappedArray
        .make[StackTraceElement](currentThreadStackTrace)
        .exists(_.getMethodName == "aMethodWithoutAnInterestingName"))
  }
  @Test def `thread.getStackTrace should be a stacktrace for that thread`()
      : Unit = {
    object Something {
      def aMethodWithoutAnInterestingName = {
        Thread.sleep(2000)
      }
    }
    val thread = new Thread {
      override def run() = {
        Something.aMethodWithoutAnInterestingName
      }
    }
    thread.start()
    eventuallyEquals(label = "thread.getState == Thread.State.TIMED_WAITING")(
      thread.getState,
      Thread.State.TIMED_WAITING)
    val rawStackTrace = thread.getStackTrace
    assert(
      mutable.WrappedArray
        .make[StackTraceElement](rawStackTrace)
        .exists(_.getMethodName == "aMethodWithoutAnInterestingName"))
  }
  @Test def `newly created threads should show up in Thread.getAllStackTraces`()
      : Unit = {
    val mutex = new Object
    val thread1 =
      new WaitingThread(mutex, name = "waiter nc Thread.getAllStackTraces1")
    val thread2 =
      new WaitingThread(mutex, name = "waiter nc Thread.getAllStackTraces2")

    {
      val stackTraces = Thread.getAllStackTraces
      assertEquals(stackTraces.containsKey(thread1), false)
      assertEquals(stackTraces.containsKey(thread2), false)
      Console.out.println("none of threads present as expected")
    }

    thread1.start()

    {
      val stackTraces = Thread.getAllStackTraces
      assertEquals(stackTraces.containsKey(thread1), true)
      assertEquals(stackTraces.containsKey(thread2), false)
      Console.out.println("thread1 present as expected")
    }

    thread2.start()

    {
      val stackTraces = Thread.getAllStackTraces
      assertEquals(stackTraces.containsKey(thread1), true)
      assertEquals(stackTraces.containsKey(thread2), true)
      Console.out.println("both threads present as expected")
    }

    eventuallyEquals()(thread1.getState, Thread.State.WAITING)
    eventuallyEquals()(thread2.getState, Thread.State.WAITING)

    mutex.notifyAll()

    eventuallyEquals()(thread1.getState, Thread.State.TERMINATED)
    eventuallyEquals()(thread2.getState, Thread.State.TERMINATED)

    eventually(label = "both threads not present") {
      val stackTraces = Thread.getAllStackTraces
      !stackTraces.containsKey(thread1) && !stackTraces.containsKey(thread2)
    }
  }

  @Test def `holdsLock`(): Unit = {
    def twoMutexTest = {
      val mutex1 = new Object
      val mutex2 = new Object

      assertEquals(Thread.holdsLock(mutex1), false)
      assertEquals(Thread.holdsLock(mutex2), false)
      mutex1.synchronized {
        assertEquals(Thread.holdsLock(mutex1), true)
        assertEquals(Thread.holdsLock(mutex2), false)
        mutex2.synchronized {
          assertEquals(Thread.holdsLock(mutex1), true)
          assertEquals(Thread.holdsLock(mutex2), true)
        }
        assertEquals(Thread.holdsLock(mutex1), true)
        assertEquals(Thread.holdsLock(mutex2), false)
      }
      assertEquals(Thread.holdsLock(mutex1), false)
      assertEquals(Thread.holdsLock(mutex2), false)
    }

    twoMutexTest

    val thread = new Thread {
      override def run() = twoMutexTest
    }
    thread.start()
    thread.join()
  }
  @Test def `contextClassLoaders not supported`(): Unit = {
    assertThrows(
      classOf[NotImplementedError],
      Thread.currentThread().setContextClassLoader(new ClassLoader() {}))
    assertThrows(classOf[NotImplementedError],
                 Thread.currentThread().getContextClassLoader)
  }

  @Test def `Thread.suspend and Thread.resume`(): Unit = {
    val counter = new Counter
    counter.start()
    eventually()(counter.count > 1)
    counter.suspend()
    val value = eventuallyConstant()(counter.count)
    counter.resume()
    eventually()(counter.count > value.get)
    counter.goOn = false
    counter.join()
  }

  @Test def `second Thread.suspend call should not affect anything`(): Unit = {
    val counter = new Counter
    counter.start()
    eventually()(counter.count > 1)
    counter.suspend()
    counter.suspend()
    val value = eventuallyConstant()(counter.count)
    counter.resume()
    eventually()(counter.count > value.get)
    counter.goOn = false
    counter.join()
  }

  @Test def `Thread.destroy`(): Unit = {
    val mutex  = new Object
    val thread = new WaitingThread(mutex, name = "waiter Thread.destroy")
    thread.start()
    eventuallyEquals(label = "thread WAITING")(thread.getState,
                                               Thread.State.WAITING)
    thread.destroy()
    eventually(label = "thread stopped")(!thread.isAlive)
  }

  @Test def `Thread.stop()`(): Unit = {
    val mutex  = new Object
    val thread = new WaitingThread(mutex, name = "waiter Thread.stop")
    thread.start()
    eventuallyEquals(label = "thread WAITING")(thread.getState,
                                               Thread.State.WAITING)
    thread.stop()
    eventually(label = "thread stopped")(!thread.isAlive)
  }

  @Test def `Thread.stop(throwable)`(): Unit = {
    val mutex  = new Object
    val thread = new WaitingThread(mutex, name = "waiter Thread.stop 2")
    thread.start()
    eventuallyEquals(label = "thread WAITING")(thread.getState,
                                               Thread.State.WAITING)
    thread.stop(new Error("something went wrong"))
    eventually(label = "thread stopped")(!thread.isAlive)
  }

  @Test def `Thread.countStackFrames`(): Unit = {
    val counter = new Counter
    counter.start()
    eventually()(counter.count > 1)
    // thread not suspended, throw exception ... sure why not
    assertThrows(classOf[IllegalThreadStateException],
                 counter.countStackFrames())
    counter.suspend()
    val value = eventuallyConstant()(counter.count)
    assert(counter.countStackFrames() > 0)
    counter.resume()
    eventually()(counter.count > value.get)
    counter.goOn = false
    counter.join()
  }

  @Test def `toString should contain the name and the name of the group`()
      : Unit = {
    val groupName  = "veryNiceGroupName"
    val group      = new ThreadGroup(groupName)
    val threadName = "descriptiveNameGivenToAThread"
    val thread     = new Thread(group, threadName)
    val toString   = thread.toString
    assert(toString.contains(groupName))
    assert(toString.contains(threadName))
  }

  @Test def `can set name for new and running threads`(): Unit = {
    var goOn = true
    val thread = new Thread {
      override def run() = while (goOn) {
        Thread.sleep(20)
      }
    }
    val name1 = "Larry"
    thread.setName(name1)
    assertEquals(thread.getName, name1)

    thread.start()
    val name2 = "Curly"
    thread.setName(name2)
    assertEquals(thread.getName, name2)

    goOn = false
    thread.join()

    val name3 = "Moe"
    thread.setName(name3)
    assertEquals(thread.getName, name3)
  }

  @Test def `threadIds increment`(): Unit = {
    var oldId        = -1L
    var timeToRepeat = 10
    while (timeToRepeat > 0) {
      val id = new Thread().getId
      assert(id > oldId)
      oldId = id
      timeToRepeat -= 1
    }
  }
}
