package com.gemstone.gemfire.test.dunit;

import static com.gemstone.gemfire.test.dunit.Jitter.jitterInterval;
import static org.junit.Assert.fail;

import com.gemstone.gemfire.internal.cache.LocalRegion;

/**
 * Extracted from DistributedTestCase
 */
public class Wait {

  protected Wait() {
  }
  
  /**
   * Wait until given criterion is met
   * 
   * @param ev criterion to wait on
   * @param ms total time to wait, in milliseconds
   * @param interval pause interval between waits
   * @param throwOnTimeout if false, don't generate an error
   */
  public static void waitForCriterion(final WaitCriterion ev, final long ms, final long interval, final boolean throwOnTimeout) {
    long waitThisTime = jitterInterval(interval);
    final long tilt = System.currentTimeMillis() + ms;
    for (;;) {
      if (ev.done()) {
        return; // success
      }
      
      if (ev instanceof StoppableWaitCriterion) {
        StoppableWaitCriterion ev2 = (StoppableWaitCriterion)ev;
        if (ev2.stopWaiting()) {
          if (throwOnTimeout) {
            fail("stopWaiting returned true: " + ev.description());
          }
          return;
        }
      }

      // Calculate time left
      long timeLeft = tilt - System.currentTimeMillis();
      if (timeLeft <= 0) {
        if (!throwOnTimeout) {
          return; // not an error, but we're done
        }
        fail("Event never occurred after " + ms + " ms: " + ev.description());
      }
      
      if (waitThisTime > timeLeft) {
        waitThisTime = timeLeft;
      }
      
      // Wait a little bit
      Thread.yield();
      try {
        Thread.sleep(waitThisTime);
      } catch (InterruptedException e) {
        fail("interrupted");
      }
    }
  }

  /**
   * Wait on a mutex.  This is done in a loop in order to address the
   * "spurious wakeup" "feature" in Java.
   * 
   * @param ev condition to test
   * @param mutex object to lock and wait on
   * @param ms total amount of time to wait
   * @param interval interval to pause for the wait
   * @param throwOnTimeout if false, no error is thrown.
   */
  public static void waitOnMutex(final WaitCriterion ev, final Object mutex, final long ms, final long interval, final boolean throwOnTimeout) {
    final long tilt = System.currentTimeMillis() + ms;
    long waitThisTime = jitterInterval(interval);
    synchronized (mutex) {
      for (;;) {
        if (ev.done()) {
          break;
        }
        
        long timeLeft = tilt - System.currentTimeMillis();
        if (timeLeft <= 0) {
          if (!throwOnTimeout) {
            return; // not an error, but we're done
          }
          fail("Event never occurred after " + ms + " ms: " + ev.description());
        }
        
        if (waitThisTime > timeLeft) {
          waitThisTime = timeLeft;
        }
        
        try {
          mutex.wait(waitThisTime);
        } catch (InterruptedException e) {
          fail("interrupted");
        }
      } // for
    } // synchronized
  }

  /** pause for a default interval */
  public static void pause() {
    pause(250);
  }

  /**
   * Use of this function indicates a place in the tests tree where t
   * he use of Thread.sleep() is
   * highly questionable.
   * <p>
   * Some places in the system, especially those that test expirations and other
   * timeouts, have a very good reason to call {@link Thread#sleep(long)}.  The
   * <em>other</em> places are marked by the use of this method.
   * 
   * @param ms
   */
  public static final void staticPause(int ms) {
    final long target = System.currentTimeMillis() + ms;
    try {
      for (;;) {
        long msLeft = target - System.currentTimeMillis();
        if (msLeft <= 0) {
          break;
        }
        Thread.sleep(msLeft);
      }
    }
    catch (InterruptedException e) {
      throw new AssertionError("interrupted", e);
    }
    
  }
  
  /**
   * Blocks until the clock used for expiration on the given region changes.
   */
  public static final void waitForExpiryClockToChange(LocalRegion lr) {
    long startTime = lr.cacheTimeMillis();
    do {
      Thread.yield();
    } while (startTime == lr.cacheTimeMillis());
  }
  
  /** pause for specified ms interval
   * Make sure system clock has advanced by the specified number of millis before
   * returning.
   */
  public static final void pause(int ms) {
    if (ms > 50) {
      //getLogWriter().info("Pausing for " + ms + " ms..."/*, new Exception()*/);
    }
    final long target = System.currentTimeMillis() + ms;
    try {
      for (;;) {
        long msLeft = target - System.currentTimeMillis();
        if (msLeft <= 0) {
          break;
        }
        Thread.sleep(msLeft);
      }
    }
    catch (InterruptedException e) {
      throw new AssertionError("interrupted", e);
    }
  }
}
