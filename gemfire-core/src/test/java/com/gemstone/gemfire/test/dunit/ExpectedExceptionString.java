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

import static com.gemstone.gemfire.test.dunit.Invoke.invokeInEveryVM;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Logger;

import com.gemstone.gemfire.internal.logging.LogService;

/**
 * An expected exception string which is added or removed in the log to 
 * suppress it during greplogs (grep for error strings).
 * 
 * Extracted from DistributedTestCase. Renamed from ExpectedException to 
 * prevent conflict with the JUnit rule. Note that the implementation
 * is still writing <code><ExpectedException ...></code> statements to the 
 * log for the <code>batterytest.greplogs.LogConsumer</code>.
 * 
 * @author Mitch Thomas
 * @since 5.7bugfix
 * @see batterytest.greplogs.LogConsumer
 */
public class ExpectedExceptionString implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger logger = LogService.getLogger();
  
  // Note: if you change these prefixes and suffix then you must also change batterytest.greplogs.LogConsumer to match
  private static final String LOG_MESSAGE_PREFIX_ADD = "<ExpectedException action=add>";
  private static final String LOG_MESSAGE_PREFIX_REMOVE = "<ExpectedException action=remove>";
  private static final String LOG_MESSAGE_SUFFIX = "</ExpectedException>";

  private final String exceptionString;

  private final transient VM vm;

  // TODO: prevent memory leak here
  private static final Queue<ExpectedExceptionString> expectedExceptionStrings = new ConcurrentLinkedQueue<ExpectedExceptionString>();

  public ExpectedExceptionString(final String exceptionString) {
    this(exceptionString, null);
  }
  
  public ExpectedExceptionString(final String exceptionString, final VM vm) {
    this.exceptionString = exceptionString;
    this.vm = vm;
  }

  public String getAddMessage() {
    return LOG_MESSAGE_PREFIX_ADD + this.exceptionString + LOG_MESSAGE_SUFFIX;
  }

  public String getRemoveMessage() {
    return LOG_MESSAGE_PREFIX_REMOVE + this.exceptionString + LOG_MESSAGE_SUFFIX;
  }

  public void remove() {
    final SerializableRunnable removeRunnable = newRemoveSerializableRunnable(getRemoveMessage());
    
    if (this.vm != null) {
      this.vm.invoke(removeRunnable);
    } else {
      invokeInEveryVM(removeRunnable);
    }
    
    logger.info(getRemoveMessage());
  }

  /**
   * Log in all VMs the expected exception string to prevent grep logs from
   * complaining. The expected string is used by the GrepLogs utility and so 
   * can contain regular expression characters.
   * 
   * If you do not remove the expected exception, it will be removed at the
   * end of your test case automatically.
   * 
   * @since 5.7bugfix
   * @param exception the exception string to expect
   * @return an instance that a test can use for removal
   */
  public static ExpectedExceptionString addExpectedExceptionString(final String exception) {
    return addExpectedExceptionString(exception, null);
  }

  /**
   * Log in all VMs, the
   * expected exception string to prevent grep logs from complaining. The
   * expected string is used by the GrepLogs utility and so can contain
   * regular expression characters.
   * 
   * @since 5.7bugfix
   * @param exceptionString the exception string to expect
   * @param vm the VM on which to log the expected exception or null for all VMs
   * @return an instance that a test can use for removal
   */
  public static ExpectedExceptionString addExpectedExceptionString(final String exceptionString, final VM vm) {
    final ExpectedExceptionString expectedOutput;
    if (vm != null) {
      expectedOutput = new ExpectedExceptionString(exceptionString, vm);
    } else {
      expectedOutput = new ExpectedExceptionString(exceptionString);
    }
    
    final String addMessage = expectedOutput.getAddMessage();
    
    final SerializableRunnable addRunnable = newAddSerializableRunnable(addMessage);
    
    if (vm != null) {
      vm.invoke(addRunnable);
    } else {
      invokeInEveryVM(addRunnable);
    }
    
    logger.info(addMessage);
    expectedExceptionStrings.add(expectedOutput);
    return expectedOutput;
  }
  
  static ExpectedExceptionString poll() {
    return expectedExceptionStrings.poll();
  }
  
  @SuppressWarnings("serial")
  private static SerializableRunnable newAddSerializableRunnable(final String addMessage) {
    return new SerializableRunnable("addExpectedExceptionString") {
      public void run() {
        logger.info(addMessage);
      }
    };
  }
  
  @SuppressWarnings("serial")
  private static SerializableRunnable newRemoveSerializableRunnable(final String removeMessage) {
    return new SerializableRunnable("removeExpectedExceptionString") {
      public void run() {
        logger.info(removeMessage);
      }
    };
  }
}
