/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gemstone.gemfire.test.dunit;

import static org.junit.Assert.fail;

import org.apache.logging.log4j.Logger;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.internal.cache.LocalRegion;
import com.gemstone.gemfire.internal.logging.LogService;
import com.jayway.awaitility.Awaitility;

public class Wait {
  private static final Logger logger = LogService.getLogger();

  /**
     * Wait until given criterion is met
     * @param ev criterion to wait on
     * @param ms total time to wait, in milliseconds
     * @param interval pause interval between waits
     * @param throwOnTimeout if false, don't generate an error
     * @deprecated Use {@link Awaitility} instead.
     */
    @Deprecated
    static public void waitForCriterion(WaitCriterion ev, long ms, 
        long interval, boolean throwOnTimeout) {
      long waitThisTime = Jitter.jitterInterval(interval);
      final long tilt = System.currentTimeMillis() + ms;
      for (;;) {
  //      getLogWriter().info("Testing to see if event has occurred: " + ev.description());
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
  //        getLogWriter().info("waiting " + waitThisTime + "ms for " + ev.description());
          Thread.sleep(waitThisTime);
        } catch (InterruptedException e) {
          fail("interrupted");
        }
      }
    }

  /**
   * Blocks until the clock used for expiration moves forward.
   * @param baseTime the timestamp that the clock must exceed
   * @return the last time stamp observed
   */
  public static final long waitForExpiryClockToChange(LocalRegion lr, final long baseTime) {
    long nowTime;
    do {
      Thread.yield();
      nowTime = lr.cacheTimeMillis();
    } while ((nowTime - baseTime) <= 0L);
    return nowTime;
  }

  /**
   * Blocks until the clock used for expiration moves forward.
   * @return the last time stamp observed
   */
  public static final long waitForExpiryClockToChange(LocalRegion lr) {
    return waitForExpiryClockToChange(lr, lr.cacheTimeMillis());
  }

  /** pause for specified ms interval
   * Make sure system clock has advanced by the specified number of millis before
   * returning.
   */
  public static final void pause(int ms) {
    LogWriter log = com.gemstone.gemfire.test.dunit.LogWriterSupport.getLogWriter();
    if (ms >= 1000 || log.fineEnabled()) { // check for fine but log at info
      com.gemstone.gemfire.test.dunit.LogWriterSupport.getLogWriter().info("Pausing for " + ms + " ms..."/*, new Exception()*/);
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
      Assert.fail("interrupted", e);
    }
  }

  /**
   * Wait on a mutex.  This is done in a loop in order to address the
   * "spurious wakeup" "feature" in Java.
   * @param ev condition to test
   * @param mutex object to lock and wait on
   * @param ms total amount of time to wait
   * @param interval interval to pause for the wait
   * @param throwOnTimeout if false, no error is thrown.
   */
  static public void waitMutex(WaitCriterion ev, Object mutex, long ms, 
      long interval, boolean throwOnTimeout) {
    final long tilt = System.currentTimeMillis() + ms;
    long waitThisTime = Jitter.jitterInterval(interval);
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
    static public final void staticPause(int ms) {
  //    getLogWriter().info("FIXME: Pausing for " + ms + " ms..."/*, new Exception()*/);
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
        Assert.fail("interrupted", e);
      }
      
    }
}
