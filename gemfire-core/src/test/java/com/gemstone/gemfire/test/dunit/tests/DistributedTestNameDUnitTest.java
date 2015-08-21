package com.gemstone.gemfire.test.dunit.tests;

import static com.gemstone.gemfire.test.dunit.Invoke.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.gemstone.gemfire.internal.lang.reflect.ReflectionUtils;
import com.gemstone.gemfire.test.dunit.DistributedTestCase;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

/**
 * Verifies that test name is available and consistent in the controller JVM 
 * and all 4 dunit JVMs.
 * 
 * @author Kirk Lund
 */
@Category(DistributedTest.class)
public class DistributedTestNameDUnitTest extends DistributedTestCase {
  private static final long serialVersionUID = 1L;

  // TODO: remove transient and fix bug so test FAILs fast
  
  @Rule
  public transient TestWatcher watchman = new TestWatcher() {
    protected void starting(final Description description) {
      testClassName = description.getClassName();
      testMethodName = description.getMethodName();
    }
  };
  
  private String testClassName;
  private String testMethodName;
  
  @Test
  public void testNameShouldBeConsistentInAllJVMs() throws Exception {
    final String methodName = this.testMethodName;
    
    // JUnit Rule provides getMethodName in Controller JVM
    assertThat(getMethodName(), is(methodName));
    
    // Controller JVM sets testName = getMethodName in itself and all 4 other JVMs
    assertThat(getTestName(), is(methodName));
    
    invokeInEveryVM(new SerializableRunnable(getMethodName()) {
      private static final long serialVersionUID = 1L;
      @Override
      public void run() {
        assertThat(getTestName(), is(methodName));
      }
    });
  }

  @Test
  public void uniqueNameShouldBeConsistentInAllJVMs() throws Exception {
    //final String uniqueName = testClassName + "_" + testMethodName;
    final String uniqueName = getClass().getSimpleName() + "_" + testMethodName;
    
    assertThat(getUniqueName(), is(uniqueName));
    
    invokeInEveryVM(new SerializableRunnable(getMethodName()) {
      private static final long serialVersionUID = 1L;
      @Override
      public void run() {
        assertThat(getUniqueName(), is(uniqueName));
      }
    });
  }
}
