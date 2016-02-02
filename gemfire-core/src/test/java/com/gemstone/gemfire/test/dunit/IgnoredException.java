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

import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.internal.logging.LogService;

/**
 * A class that represents an currently logged expected exception, which
 * should be removed
 * 
 * @author Mitch Thomas
 * @since 5.7bugfix
 */
public class IgnoredException implements Serializable {
  private static final Logger logger = LogService.getLogger();
  private static final long serialVersionUID = 1L;

  private final String errorString;

  private final transient VM vm;
  
  private static ConcurrentLinkedQueue<IgnoredException> ignoredExceptions = new ConcurrentLinkedQueue<IgnoredException>();

  public IgnoredException(final String errorString) {
    this.errorString = errorString;
    this.vm = null;
  }

  IgnoredException(final String errorString, final VM vm) {
    this.errorString = errorString;
    this.vm = vm;
  }

  String errorString() {
    return this.errorString;
  }
  
  VM vm() {
    return this.vm;
  }
  
  public String getRemoveMessage() {
    return "<ExpectedException action=remove>" + errorString + "</ExpectedException>";
  }

  public String getAddMessage() {
    return "<ExpectedException action=add>" + errorString + "</ExpectedException>";
  }

  public void remove() {
    SerializableRunnable removeRunnable = new SerializableRunnable(
        "removeExpectedExceptions") {
      public void run() {
        final String remove = getRemoveMessage();
        final InternalDistributedSystem sys = InternalDistributedSystem
            .getConnectedInstance();
        if (sys != null) {
          sys.getLogWriter().info(remove);
        }
        try {
          LogWriterSupport.getLogWriter().info(remove);
        } catch (Exception noHydraLogger) {
        }

        logger.info(remove);
      }
    };

    if (this.vm != null) {
      vm.invoke(removeRunnable);
    }
    else {
      Invoke.invokeInEveryVM(removeRunnable);
    }
    String s = getRemoveMessage();
    LogManager.getLogger(LogService.BASE_LOGGER_NAME).info(s);
    // log it locally
    final InternalDistributedSystem sys = InternalDistributedSystem
        .getConnectedInstance();
    if (sys != null) { // avoid creating a system
      sys.getLogWriter().info(s);
    }
    LogWriterSupport.getLogWriter().info(s);
  }

  public static void removeAllExpectedExceptions() {
    IgnoredException ex;
    while((ex = ignoredExceptions.poll()) != null) {
      ex.remove();
    }
  }

  /**
   * Log in all VMs, in both the test logger and the GemFire logger the
   * expected exception string to prevent grep logs from complaining. The
   * expected string is used by the GrepLogs utility and so can contain
   * regular expression characters.
   * 
   * @since 5.7bugfix
   * @param exception
   *          the exception string to expect
   * @param v
   *          the VM on which to log the expected exception or null for all VMs
   * @return an ExpectedException instance for removal purposes
   */
  public static IgnoredException addIgnoredException(final String exception,
      VM v) {
    final IgnoredException ret;
    if (v != null) {
      ret = new IgnoredException(exception, v);
    }
    else {
      ret = new IgnoredException(exception);
    }
    // define the add and remove expected exceptions
    final String add = ret.getAddMessage();
    SerializableRunnable addRunnable = new SerializableRunnable(
        "addExpectedExceptions") {
      public void run() {
        final InternalDistributedSystem sys = InternalDistributedSystem
            .getConnectedInstance();
        if (sys != null) {
          sys.getLogWriter().info(add);
        }
        try {
          LogWriterSupport.getLogWriter().info(add);
        } catch (Exception noHydraLogger) {
        }
  
        logger.info(add);
      }
    };
    if (v != null) {
      v.invoke(addRunnable);
    }
    else {
      Invoke.invokeInEveryVM(addRunnable);
    }
    
    LogManager.getLogger(LogService.BASE_LOGGER_NAME).info(add);
    // Log it locally too
    final InternalDistributedSystem sys = InternalDistributedSystem
        .getConnectedInstance();
    if (sys != null) { // avoid creating a cache
      sys.getLogWriter().info(add);
    }
    LogWriterSupport.getLogWriter().info(add);
    ignoredExceptions.add(ret);
    return ret;
  }

  /**
   * Log in all VMs, in both the test logger and the GemFire logger the
   * expected exception string to prevent grep logs from complaining. The
   * expected string is used by the GrepLogs utility and so can contain
   * regular expression characters.
   * 
   * If you do not remove the expected exception, it will be removed at the
   * end of your test case automatically.
   * 
   * @since 5.7bugfix
   * @param exception
   *          the exception string to expect
   * @return an ExpectedException instance for removal
   */
  public static IgnoredException addIgnoredException(final String exception) {
    return addIgnoredException(exception, null);
  }
}