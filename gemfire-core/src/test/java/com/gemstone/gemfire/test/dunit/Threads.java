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
import static org.junit.Assert.fail;

import org.apache.logging.log4j.Logger;

import com.gemstone.gemfire.internal.OSProcess;
import com.gemstone.gemfire.internal.logging.LogService;

/**
 * A set of thread dump methods useful for writing tests. 
 * 
 * <p>These methods can be used directly:
 * <code>ThreadDump.dumpStack(...)</code>, however, they read better if they
 * are referenced through static import:
 * 
 * <pre>
 * import static com.gemstone.gemfire.test.dunit.ThreadDump.*;
 *    ...
 *    dumpStack(...);
 * </pre>
 * 
 * <p>Extracted from DistributedTestCase
 * 
 * @see VM
 * @see Host
 */
@SuppressWarnings("serial")
public class Threads {
  private static final Logger logger = LogService.getLogger();

  /** 
   * Print a stack dump for this vm
   * 
   * @author bruce
   * @since 5.0
   */
  public static void dumpStack() {
    com.gemstone.gemfire.internal.OSProcess.printStacks(0, false);
  }

  /** 
   * Print a stack dump for the given vm
   * 
   * @author bruce
   * @since 5.0
   */
  public static void dumpStack(final VM vm) {
    vm.invoke(dumpStackSerializableRunnable());
  }

  /** 
   * Print stack dumps for all vms on the given host
   * 
   * @author bruce
   * @since 5.0
   */
  public static void dumpStack(final Host host) {
    for (int v=0; v < host.getVMCount(); v++) {
      host.getVM(v).invoke(dumpStackSerializableRunnable());
    }
  }

  /** 
   * Print stack dumps for all vms on all hosts
   * 
   * @author bruce
   * @since 5.0
  */
  public static void dumpAllStacks() {
    for (int h=0; h < Host.getHostCount(); h++) {
      dumpStack(Host.getHost(h));
    }
  }

  public static void dumpStackTrace(final Thread thread, final StackTraceElement[] stack) {
    StringBuilder msg = new StringBuilder();
    msg.append("Thread=<")
      .append(thread)
      .append("> stackDump:\n");
    for (int i=0; i < stack.length; i++) {
      msg.append("\t")
        .append(stack[i])
        .append("\n");
    }
    logger.info(msg.toString());
  }

  /**
   * Dump all thread stacks
   */
  public static void dumpMyThreads() {
    OSProcess.printStacks(0, false);
  }

  /**
   * Wait for the specified thread to terminate.
   * 
   * @param thread thread to wait on
   * @param timeoutMillis maximum time to wait
   * @throws AssertionError if the thread does not terminate
   */
  public static void join(final Thread thread, final long timeoutMillis) {
    final long tilt = System.currentTimeMillis() + timeoutMillis;
    final long incrementalWait = jitterInterval(timeoutMillis);
    final long start = System.currentTimeMillis();
    for (;;) {
      // I really do *not* understand why this check is necessary
      // but it is, at least with JDK 1.6.  According to the source code
      // and the javadocs, one would think that join() would exit immediately
      // if the thread is dead.  However, I can tell you from experimentation
      // that this is not the case. :-(  djp 2008-12-08
      if (!thread.isAlive()) {
        break;
      }
      try {
        thread.join(incrementalWait);
      } catch (InterruptedException e) {
        fail("interrupted");
      }
      if (System.currentTimeMillis() >= tilt) {
        break;
      }
    } // for
    if (thread.isAlive()) {
      logger.info("HUNG THREAD");
      dumpStackTrace(thread, thread.getStackTrace());
      dumpMyThreads();
      thread.interrupt(); // We're in trouble!
      fail("Thread did not terminate after " + timeoutMillis + " ms: " + thread);
    }
    long elapsedMs = (System.currentTimeMillis() - start);
    if (elapsedMs > 0) {
      String msg = "Thread " + thread + " took " + elapsedMs + " ms to exit.";
      logger.info(msg);
    }
  }

  private static SerializableRunnable dumpStackSerializableRunnable() {
    return new SerializableRunnable() {
      @Override
      public void run() {
        dumpStack();
      }
    };
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
    static public final void staticPause(int ms) { // TODO: DELETE
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
