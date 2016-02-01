package com.gemstone.gemfire.test.dunit;

public class Invoke {

  /**
   * Invokes a <code>SerializableRunnable</code> in every VM that
   * DUnit knows about.
   *
   * @see VM#invoke(Runnable)
   */
  public static void invokeInEveryVM(SerializableRunnable work) {
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
  
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        vm.invoke(work);
      }
    }
  }

}
