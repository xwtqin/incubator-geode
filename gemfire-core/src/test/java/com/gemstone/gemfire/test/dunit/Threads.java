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
import com.gemstone.gemfire.internal.OSProcess;
import com.gemstone.gemfire.internal.logging.LocalLogWriter;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.internal.logging.LogWriterImpl;

public class Threads {
  private static final Logger logger = LogService.getLogger();

  /**
     * Wait for a thread to join
     * @param t thread to wait on
     * @param ms maximum time to wait
     * @throws AssertionError if the thread does not terminate
     */
    static public void join(Thread t, long ms, LogWriter logger) {
      final long tilt = System.currentTimeMillis() + ms;
      final long incrementalWait = Jitter.jitterInterval(ms);
      final long start = System.currentTimeMillis();
      for (;;) {
        // I really do *not* understand why this check is necessary
        // but it is, at least with JDK 1.6.  According to the source code
        // and the javadocs, one would think that join() would exit immediately
        // if the thread is dead.  However, I can tell you from experimentation
        // that this is not the case. :-(  djp 2008-12-08
        if (!t.isAlive()) {
          break;
        }
        try {
          t.join(incrementalWait);
        } catch (InterruptedException e) {
          fail("interrupted");
        }
        if (System.currentTimeMillis() >= tilt) {
          break;
        }
      } // for
      if (logger == null) {
        logger = new LocalLogWriter(LogWriterImpl.INFO_LEVEL, System.out);
      }
      if (t.isAlive()) {
        logger.info("HUNG THREAD");
        Threads.dumpStackTrace(t, t.getStackTrace(), logger);
        Threads.dumpMyThreads(logger);
        t.interrupt(); // We're in trouble!
        fail("Thread did not terminate after " + ms + " ms: " + t);
  //      getLogWriter().warning("Thread did not terminate" 
  //          /* , new Exception()*/
  //          );
      }
      long elapsedMs = (System.currentTimeMillis() - start);
      if (elapsedMs > 0) {
        String msg = "Thread " + t + " took " 
          + elapsedMs
          + " ms to exit.";
        logger.info(msg);
      }
    }

  public static void dumpStackTrace(Thread t, StackTraceElement[] stack, LogWriter logger) {
    StringBuilder msg = new StringBuilder();
    msg.append("Thread=<")
      .append(t)
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
  public static void dumpMyThreads(LogWriter logger) {
    OSProcess.printStacks(0, false);
  }

  /** print a stack dump for this vm
      @author bruce
      @since 5.0
   */
  public static void dumpStack() {
    com.gemstone.gemfire.internal.OSProcess.printStacks(0, false);
  }

  /** print a stack dump for the given vm
      @author bruce
      @since 5.0
   */
  public static void dumpStack(VM vm) {
    vm.invoke(com.gemstone.gemfire.test.dunit.DistributedTestCase.class, "dumpStack");
  }

  /** print stack dumps for all vms on the given host
      @author bruce
      @since 5.0
   */
  public static void dumpStack(Host host) {
    for (int v=0; v < host.getVMCount(); v++) {
      host.getVM(v).invoke(com.gemstone.gemfire.test.dunit.DistributedTestCase.class, "dumpStack");
    }
  }

  /** print stack dumps for all vms
      @author bruce
      @since 5.0
   */
  public static void dumpAllStacks() {
    for (int h=0; h < Host.getHostCount(); h++) {
      dumpStack(Host.getHost(h));
    }
  }

}
