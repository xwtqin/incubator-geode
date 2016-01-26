package com.gemstone.gemfire.test.dunit;

import static com.gemstone.gemfire.test.dunit.Invoke.invokeInEveryVM;

import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Logger;

import com.gemstone.gemfire.internal.logging.LogService;

/**
 * An error string which is added or removed via log statement to ignore 
 * during GrepLogs. The GrepLogs utility is invoked against all output from 
 * a DUnit test run.</p>
 * 
 * Extracted from DistributedTestCase.</p> 
 * 
 * Renamed from ExpectedException to prevent conflict with the JUnit rule. 
 * Note that the implementation is still writing 
 * <code><ExpectedException ...></code> statements which is read by 
 * <code>batterytest.greplogs.LogConsumer</code>.
 * 
 * @author Mitch Thomas
 * @since 5.7bugfix
 * @see batterytest.greplogs.LogConsumer
 */
@SuppressWarnings("serial")
public class IgnoredException implements Serializable {
  private static final Logger logger = LogService.getLogger();
  
  // Note: if you change these then you must also change batterytest.greplogs.LogConsumer
  private static final String LOG_MESSAGE_PREFIX_ADD = "<ExpectedException action=add>";
  private static final String LOG_MESSAGE_PREFIX_REMOVE = "<ExpectedException action=remove>";
  private static final String LOG_MESSAGE_SUFFIX = "</ExpectedException>";

  // TODO: prevent memory leak here
  private static ConcurrentLinkedQueue<IgnoredException> ignoredExceptions = new ConcurrentLinkedQueue<IgnoredException>();

  private final String errorString;

  private final transient VM vm;

  public IgnoredException(final String errorString) {
    this.errorString = errorString;
    this.vm = null;
  }

  public IgnoredException(final String errorString, final VM vm) {
    this.errorString = errorString;
    this.vm = vm;
  }

  public String getAddMessage() {
    return LOG_MESSAGE_PREFIX_ADD + this.errorString + LOG_MESSAGE_SUFFIX;
  }

  public String getRemoveMessage() {
    return LOG_MESSAGE_PREFIX_REMOVE + this.errorString + LOG_MESSAGE_SUFFIX;
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
   * Log the error string in all VMs to prevent GrepLogs from failing. 
   * The error string is read by the GrepLogs utility which supports regex.
   * 
   * If you do not remove the ignored exception, it will be removed at the
   * end of your test case automatically.
   * 
   * @since 5.7bugfix
   * @param errorString the error string to be ignored
   * @return an instance that a test can use for removal
   */
  public static IgnoredException addIgnoredException(final String errorString) {
    return addIgnoredException(errorString, null);
  }

  /**
   * Log the error string in the specified VM to prevent GrepLogs from failing.
   * The error string is read by the GrepLogs utility which supports regex.
   * 
   * @since 5.7bugfix
   * @param errorString the error string to be ignored
   * @param vm the VM on which to log the ignored exception or null for all VMs
   * @return an instance that a test can use for removal
   */
  public static IgnoredException addIgnoredException(final String errorString, final VM vm) {
    final IgnoredException instance;
    if (vm != null) {
      instance = new IgnoredException(errorString, vm);
    } else {
      instance = new IgnoredException(errorString);
    }
    
    final String addMessage = instance.getAddMessage();
    
    final SerializableRunnable addRunnable = newAddSerializableRunnable(addMessage);
    
    if (vm != null) {
      vm.invoke(addRunnable);
    } else {
      invokeInEveryVM(addRunnable);
    }
    
    logger.info(addMessage);
    ignoredExceptions.add(instance);
    return instance;
  }

  public static void removeAllIgnoredExceptions() {
    IgnoredException ex;
    while((ex = ignoredExceptions.poll()) != null) {
      ex.remove();
    }
  }

  private static SerializableRunnable newAddSerializableRunnable(final String addMessage) {
    return new SerializableRunnable("addExpectedExceptionString") {
      public void run() {
        logger.info(addMessage);
      }
    };
  }
  
  private static SerializableRunnable newRemoveSerializableRunnable(final String removeMessage) {
    return new SerializableRunnable("removeExpectedExceptionString") {
      public void run() {
        logger.info(removeMessage);
      }
    };
  }
}