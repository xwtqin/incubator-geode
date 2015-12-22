package com.gemstone.gemfire.test.dunit.rules.tests;

import static com.gemstone.gemfire.test.dunit.Invoke.*;
import static java.lang.System.*;
import static org.assertj.core.api.Assertions.*;

import java.io.Serializable;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.dunit.rules.DistributedRestoreSystemProperties;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;
import com.gemstone.gemfire.test.junit.rules.SerializableExternalResource;

/**
 * Distributed tests for DistributedRestoreSystemProperties
 * 
 * @author Kirk Lund
 */
@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class DistributedRestoreSystemPropertiesDUnitTest implements Serializable {

  private static final String PROPERTY_NAME = "PROPERTY_NAME"; 
  private static final String ORIGINAL_VALUE = "ORIGINAL_VALUE"; 
  private static final String NEW_VALUE = "NEW_VALUE"; 

  @Rule
  public final DistributedTestRule dunitTestRule = DistributedTestRule.builder()
      .innerRule(new SetUp())
      .innerRule(new Restore())
      .innerRule(new Verify())
      .innerRule(new DistributedRestoreSystemProperties())
      .build();
  
  @Test
  public void shouldRestoreInAllVMs() {
    invokeInEveryVMAndLocator(new SerializableRunnable("shouldRestoreInAllVMs:setProperty") {
      @Override
      public void run() { 
        setProperty(PROPERTY_NAME, NEW_VALUE);
      }
    });
    
    invokeInEveryVMAndLocator(new SerializableRunnable("shouldRestoreInAllVMs:assertion") {
      @Override
      public void run() { 
        assertThat(getProperty(PROPERTY_NAME)).isEqualTo(NEW_VALUE);
      }
    });
  }
  
  protected static class SetUp extends SerializableExternalResource {
    
    @Override
    protected void before() throws Throwable {
      assertThat(Host.getHostCount()).isEqualTo(1);
      assertThat(Host.getHost(0).getVMCount()).isEqualTo(4);

      invokeInEveryVMAndLocator(new SerializableRunnable("SetUp:before") {
        @Override
        public void run() { 
          setProperty(PROPERTY_NAME, ORIGINAL_VALUE);
        }
      });
    }

    @Override
    protected void after() throws Throwable {
      invokeInEveryVMAndLocator(new SerializableRunnable("SetUp:after") {
        @Override
        public void run() { 
          clearProperty(PROPERTY_NAME);
        }
      });
    }
  }
  
  protected static class Verify extends SerializableExternalResource {
    
    @Override
    protected void before() throws Throwable {
      invokeInEveryVMAndLocator(new SerializableRunnable("Verify:before") {
        @Override
        public void run() { 
          assertThat(getProperty(PROPERTY_NAME)).isEqualTo(ORIGINAL_VALUE);
        }
      });
    }
    
    @Override
    protected void after() {
      invokeInEveryVMAndLocator(new SerializableRunnable("Verify:after") {
        @Override
        public void run() { 
          assertThat(getProperty(PROPERTY_NAME)).isEqualTo(ORIGINAL_VALUE);
        }
      });
    }
  }
  
  private static void invokeInEveryVMAndLocator(final SerializableRunnable runnable) {
    runnable.run();
    invokeInEveryVM(runnable);
    invokeInLocator(runnable);
  }
  
  protected static class Restore extends SerializableExternalResource {
    private Properties originalProperties;
    
    @Override
    protected void before() throws Throwable {
      originalProperties = System.getProperties();
    }
    
    @Override
    protected void after() {
      setProperties(originalProperties);
    }
  }
}
