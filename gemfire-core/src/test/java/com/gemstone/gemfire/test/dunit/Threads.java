package com.gemstone.gemfire.test.dunit;

import static com.gemstone.gemfire.test.dunit.Jitter.*;
import static org.junit.Assert.fail;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.internal.OSProcess;
import com.gemstone.gemfire.internal.logging.LocalLogWriter;
import com.gemstone.gemfire.internal.logging.LogWriterImpl;

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

  protected Threads() {
  }
  
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
  
  public static void dumpStackTrace(final Thread thread, final StackTraceElement[] stack, final LogWriter logWriter) { // TODO: remove LogWriter
    StringBuilder msg = new StringBuilder();
    msg.append("Thread=<")
      .append(thread)
      .append("> stackDump:\n");
    for (int i=0; i < stack.length; i++) {
      msg.append("\t")
        .append(stack[i])
        .append("\n");
    }
    logWriter.info(msg.toString());
  }

  /**
   * Dump all thread stacks
   */
  public static void dumpMyThreads(final LogWriter logWriter) { // TODO: remove LogWriter
    OSProcess.printStacks(0, false);
  }
  
  /**
   * Wait for a thread to join
   * @param thread thread to wait on
   * @param timeoutMillis maximum time to wait
   * @throws AssertionFailure if the thread does not terminate
   */
  public static void join(final Thread thread, final long timeoutMillis) {
    join(thread, timeoutMillis, new LocalLogWriter(LogWriterImpl.INFO_LEVEL, System.out));
  }

  /**
   * Wait for a thread to join // TODO: remove LogWriter
   * @param thread thread to wait on
   * @param timeoutMillis maximum time to wait
   * @throws AssertionFailure if the thread does not terminate
   */
  public static void join(final Thread thread, final long timeoutMillis, final LogWriter logWriter) {
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
      logWriter.info("HUNG THREAD");
      dumpStackTrace(thread, thread.getStackTrace(), logWriter);
      dumpMyThreads(logWriter);
      thread.interrupt(); // We're in trouble!
      fail("Thread did not terminate after " + timeoutMillis + " ms: " + thread);
    }
    long elapsedMs = (System.currentTimeMillis() - start);
    if (elapsedMs > 0) {
      String msg = "Thread " + thread + " took " 
        + elapsedMs
        + " ms to exit.";
      logWriter.info(msg);
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
}
