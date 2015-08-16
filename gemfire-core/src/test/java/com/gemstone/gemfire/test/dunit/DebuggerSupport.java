package com.gemstone.gemfire.test.dunit;

/**
 * Extracted from DistributedTestCase. Enters infinite loop to allow debugger to attach.
 */
public class DebuggerSupport {

  protected DebuggerSupport() {
  }
  
  @SuppressWarnings("serial")
  public static void attachDebugger(VM vm, final String msg) {
    vm.invoke(new SerializableRunnable("Attach Debugger") {
      public void run() {
        com.gemstone.gemfire.internal.util.DebuggerSupport.
        waitForJavaDebugger(msg);
      } 
    });
  }
}
