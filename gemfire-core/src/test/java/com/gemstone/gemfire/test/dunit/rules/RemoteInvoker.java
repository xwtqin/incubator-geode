package com.gemstone.gemfire.test.dunit.rules;

import static com.gemstone.gemfire.test.dunit.Invoke.invokeInEveryVM;
import static com.gemstone.gemfire.test.dunit.Invoke.invokeInLocator;

import java.io.Serializable;

import com.gemstone.gemfire.test.dunit.SerializableRunnable;

@SuppressWarnings("serial")
public class RemoteInvoker implements Serializable {

  public void invokeEverywhere(final SerializableRunnable runnable) {
    runnable.run();
    invokeInEveryVM(runnable);
    invokeInLocator(runnable);
  }

  public void remoteInvokeInEveryVMAndLocator(final SerializableRunnable runnable) {
    invokeInEveryVM(runnable);
    invokeInLocator(runnable);
  }
}
