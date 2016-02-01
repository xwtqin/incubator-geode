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
          DistributedTestCase.getLogWriter().info(remove);
        } catch (Exception noHydraLogger) {
        }

        logger.info(remove);
      }
    };

    if (this.vm != null) {
      vm.invoke(removeRunnable);
    }
    else {
      DistributedTestCase.invokeInEveryVM(removeRunnable);
    }
    String s = getRemoveMessage();
    LogManager.getLogger(LogService.BASE_LOGGER_NAME).info(s);
    // log it locally
    final InternalDistributedSystem sys = InternalDistributedSystem
        .getConnectedInstance();
    if (sys != null) { // avoid creating a system
      sys.getLogWriter().info(s);
    }
    DistributedTestCase.getLogWriter().info(s);
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
  public static IgnoredException addExpectedException(final String exception,
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
          DistributedTestCase.getLogWriter().info(add);
        } catch (Exception noHydraLogger) {
        }
  
        logger.info(add);
      }
    };
    if (v != null) {
      v.invoke(addRunnable);
    }
    else {
      DistributedTestCase.invokeInEveryVM(addRunnable);
    }
    
    LogManager.getLogger(LogService.BASE_LOGGER_NAME).info(add);
    // Log it locally too
    final InternalDistributedSystem sys = InternalDistributedSystem
        .getConnectedInstance();
    if (sys != null) { // avoid creating a cache
      sys.getLogWriter().info(add);
    }
    DistributedTestCase.getLogWriter().info(add);
    IgnoredException.ignoredExceptions.add(ret);
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
  public static IgnoredException addExpectedException(final String exception) {
    return IgnoredException.addExpectedException(exception, null);
  }
}