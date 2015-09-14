package com.gemstone.gemfire.test.dunit.rules;

import static java.lang.System.getProperties;
import static java.lang.System.setProperties;

import java.util.Properties;

import org.junit.contrib.java.lang.system.RestoreSystemProperties;

import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.junit.rules.SerializableTestRule;

/**
 * Distributed version of RestoreSystemProperties which affects all DUnit 
 * JVMs including the Locator JVM.
 * 
 * @author Kirk Lund
 */
@SuppressWarnings("serial")
public class DistributedRestoreSystemProperties extends RestoreSystemProperties implements SerializableTestRule {
  
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
}
