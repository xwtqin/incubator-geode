package com.gemstone.gemfire.test.dunit;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of remote invocation methods useful for writing tests. 
 * <pr/>
 * These methods can be used directly:
 * <code>Invoke.invokeInEveryVM(...)</code>, however, they read better if they
 * are referenced through static import:
 * 
 * <pre>
 * import static com.gemstone.gemfire.test.dunit.Invoke.*;
 *    ...
 *    invokeInEveryVM(...);
 * </pre>
 * <pr/>
 * Extracted from DistributedTestCase
 * 
 * @see VM
 * @see SerializableCallable
 * @see SerializableRunnable
 * @see RepeatableRunnable
 */
public class Invoke {

  protected Invoke() {
  }
  
  /**
   * Invokes a <code>SerializableRunnable</code> in every VM that DUnit knows about.
   *
   * @see VM#invoke(Runnable)
   */
  public static void invokeInEveryVM(final SerializableRunnable work) {
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        vm.invoke(work);
      }
    }
  }

  public static void invokeInLocator(final SerializableRunnable work) {
    Host.getLocator().invoke(work);
  }
  
  /**
   * Invokes a <code>SerializableCallable</code> in every VM that DUnit knows about.
   *
   * @return a Map of results, where the key is the VM and the value is the result for that VM
   * @see VM#invoke(java.util.concurrent.Callable)
   */
  public static Map<VM, Object> invokeInEveryVM(final SerializableCallable<?> work) {
    final Map<VM, Object> results = new HashMap<VM, Object>();
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        results.put(vm, vm.invoke(work));
      }
    }
    return results;
  }

  /**
   * Invokes a method in every remote VM that DUnit knows about.
   *
   * @see VM#invoke(Class, String)
   */
  public static void invokeInEveryVM(final Class<?> theClass, final String methodName) {
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        vm.invoke(theClass, methodName);
      }
    }
  }

  /**
   * Invokes a method in every remote VM that DUnit knows about.
   *
   * @see VM#invoke(Class, String)
   */
  public static void invokeInEveryVM(final Class<?> theClass, final String methodName, final Object[] methodArgs) {
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        vm.invoke(theClass, methodName, methodArgs);
      }
    }
  }
  
  public static void invokeRepeatingIfNecessary(final VM vm, final RepeatableRunnable task) {
    vm.invokeRepeatingIfNecessary(task, 0);
  }
  
  /**
   * For ACK scopes, no repeat should be necessary.
   * 
   * @param vm
   * @param task
   * @param repeatTimeoutMs the number of milliseconds to try repeating validation code in the
   * event that AssertionFailedError is thrown.  
   */
  public static void invokeRepeatingIfNecessary(final VM vm, final RepeatableRunnable task, final long repeatTimeoutMs) {
    vm.invokeRepeatingIfNecessary(task, repeatTimeoutMs);
  }
  
  /**
   * Invokes a <code>SerializableRunnable</code> in every VM that
   * DUnit knows about.  If work.run() throws an assertion failure, 
   * its execution is repeated, until no assertion failure occurs or
   * repeatTimeout milliseconds have passed.
   *
   * @see VM#invoke(Runnable)
   */
  public static void invokeInEveryVMRepeatingIfNecessary(final RepeatableRunnable work) {
    invokeInEveryVMRepeatingIfNecessary(work, 0);
  }

  public static void invokeInEveryVMRepeatingIfNecessary(final RepeatableRunnable work, final long repeatTimeoutMs) {
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        vm.invokeRepeatingIfNecessary(work, repeatTimeoutMs);
      }
    }
  }
  
  /** Return the total number of VMs on all hosts */
  public static int getVMCount() {
    int count = 0;
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
      count += host.getVMCount();
    }
    return count;
  }
}
