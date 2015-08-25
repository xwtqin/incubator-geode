package com.gemstone.gemfire.test.dunit.rules;

import static com.gemstone.gemfire.test.dunit.Invoke.*;
import static java.lang.System.getProperties;
import static java.lang.System.setProperties;

import java.io.Serializable;
import java.util.Properties;

import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import com.gemstone.gemfire.test.dunit.SerializableRunnable;

/**
 * Distributed version of RestoreSystemProperties which affects all DUnit 
 * JVMs including the Locator JVM.
 * 
 * @author Kirk Lund
 */
@SuppressWarnings("serial")
public class DistributedRestoreSystemProperties extends RestoreSystemProperties implements Serializable {
  
  private static volatile Properties originalProperties;

  private final RemoteInvoker invoker;
  
  public DistributedRestoreSystemProperties() {
   this(new RemoteInvoker());
  }
  
  public DistributedRestoreSystemProperties(final RemoteInvoker invoker) {
    super();
    this.invoker = invoker;
  }
  
  
  @Override
  protected void before() throws Throwable {
    super.before();
    this.invoker.remoteInvokeInEveryVMAndLocator(new SerializableRunnable() {
      @Override
      public void run() { 
        originalProperties = getProperties();
        setProperties(new Properties(originalProperties));
      }
    });
  }

  @Override
  protected void after() {
    super.after();
    this.invoker.remoteInvokeInEveryVMAndLocator(new SerializableRunnable() {
      @Override
      public void run() { 
        setProperties(originalProperties);
        originalProperties = null;
      }
    });
  }
  
  public static class RemoteInvoker implements Serializable {
    public void remoteInvokeInEveryVMAndLocator(final SerializableRunnable runnable) {
      invokeInEveryVM(runnable);
      invokeInLocator(runnable);
    }
  }
}
