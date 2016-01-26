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

import static com.gemstone.gemfire.test.dunit.Jitter.jitterInterval;
import static com.gemstone.gemfire.test.dunit.Assert.fail;

import com.gemstone.gemfire.internal.cache.LocalRegion;

/**
 * Extracted from DistributedTestCase
 * 
 * @deprecated Use {@link com.jayway.awaitility.Awaitility} instead.
 */
@Deprecated
public class Wait {

  protected Wait() {
  }
  
  /** 
   * Pause for a default interval 
   */
  public static void pause() {
    pause(250);
  }

  /** 
   * Pause for specified milliseconds. Make sure system clock has advanced by 
   * the specified number of milliseconds before returning.
   */
  public static final void pause(final int milliseconds) {
    final long target = System.currentTimeMillis() + milliseconds; // TODO: reimplement with System.nanoTime()
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
      fail("interrupted", e);
    }
  }

  /**
   * Blocks until the clock used for expiration moves forward.
   * 
   * @return the last time stamp observed
   */
  public static final long waitForExpiryClockToChange(final LocalRegion lr) {
    return waitForExpiryClockToChange(lr, lr.cacheTimeMillis());
  }

  /**
   * Blocks until the clock used for expiration moves forward.
   * 
   * @param baseTime the timestamp that the clock must exceed
   * @return the last time stamp observed
   */
  public static final long waitForExpiryClockToChange(final LocalRegion lr, final long baseTime) {
    long nowTime;
    do {
      Thread.yield();
      nowTime = lr.cacheTimeMillis();
    } while ((nowTime - baseTime) <= 0L);
    return nowTime;
  }

  /**
     * Wait until given criterion is met
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
}
