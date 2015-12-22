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
package com.gemstone.gemfire.distributed;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.Callable;

import com.gemstone.gemfire.distributed.AbstractLauncher.Status;
import com.gemstone.gemfire.distributed.LocatorLauncher.Builder;
import com.gemstone.gemfire.distributed.LocatorLauncher.LocatorState;
import com.gemstone.gemfire.internal.util.StopWatch;

/**
 * Test utility to asynchronously await the startup of a Locator process.
 */
public class LocatorWaiter {

  private final int timeout;
  private final int interval;
  private final Process process;
  private final List<String> outLines;
  private final List<String> errLines;
  
  public LocatorWaiter(final Process process, final List<String> outLines, final List<String> errLines, final int timeout, final int interval) {
    this.process = process;
    this.outLines = outLines;
    this.errLines = errLines;
    this.timeout = timeout;
    this.interval = interval;
  }
  
  public void waitForLocatorToStart(final LocatorLauncher launcher, boolean throwOnTimeout) throws Exception {
    assertEventuallyTrue("waiting for process to start: " + launcher.status(), new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        try {
          checkProcess();
          final LocatorState LocatorState = launcher.status();
          return (LocatorState != null && Status.ONLINE.equals(LocatorState.getStatus()));
        }
        catch (RuntimeException e) {
          return false;
        }
      }
    }, this.timeout, this.interval);
  }
  
  public void waitForLocatorToStart(final int port, final boolean throwOnTimeout) throws Exception {
    final LocatorLauncher locatorLauncher = new Builder().setPort(port).build();
    assertEventuallyTrue("Waiting for Locator in other process to start.", new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        try {
          checkProcess();
          final LocatorState locatorState = locatorLauncher.status();
          return (locatorState != null && Status.ONLINE.equals(locatorState.getStatus()));
        }
        catch (RuntimeException e) {
          return false;
        }
      }
    }, this.timeout, this.interval);
  }

  private void checkProcess() {
    try {
      // TODO: wait until stdout and stderr are no longer changing
      int exitValue = this.process.exitValue();
      // prefer stderr over stdout if possible
      boolean stderr = this.errLines.isEmpty() ? false : true;
      String type = stderr ? "stderr" : "stdout";
      List<String> lines = stderr ? this.errLines : this.outLines;
      StringBuilder output = new StringBuilder();
      for (String line : lines) {
        output.append(line);
      }
      fail("Process failed with exitValue " + exitValue + ". " + type + ": " + output);
    } catch (IllegalThreadStateException stillRunning) {
      // process is still running... continue until timeout
    }
  }
  
  private void assertEventuallyTrue(final String message, final Callable<Boolean> callable, final int timeout, final int interval) throws Exception {
    boolean done = false;
    for (StopWatch time = new StopWatch(true); !done && time.elapsedTimeMillis() < timeout; done = (callable.call())) {
      Thread.sleep(interval);
    }
    assertTrue(message, done);
  }
}
