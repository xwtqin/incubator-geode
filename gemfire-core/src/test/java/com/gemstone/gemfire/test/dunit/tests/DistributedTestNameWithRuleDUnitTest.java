package com.gemstone.gemfire.test.dunit.tests;

import static com.gemstone.gemfire.test.dunit.Invoke.invokeInEveryVM;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.Serializable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.gemstone.gemfire.test.dunit.DUnitTestRule;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class DistributedTestNameWithRuleDUnitTest implements Serializable {

  @Rule
  public final DUnitTestRule dunitTestRule = new DUnitTestRule();
  
  @Rule
  public transient TestWatcher watchman = new TestWatcher() {
    protected void starting(final Description description) {
      testMethodName = description.getMethodName();
    }
  };
  
  private transient String testMethodName;
  
  @Test
  public void testNameShouldBeConsistentInAllJVMs() throws Exception {
    final String methodName = this.testMethodName;
    
    // JUnit Rule provides getMethodName in Controller JVM
    assertThat(this.dunitTestRule.getMethodName(), is(methodName));
    
    // Controller JVM sets testName = getMethodName in itself and all 4 other JVMs
    assertThat(DUnitTestRule.getTestMethodName(), is(methodName));
    
    invokeInEveryVM(new SerializableRunnable(this.dunitTestRule.getMethodName()) {
      @Override
      public void run() {
        assertThat(DUnitTestRule.getTestMethodName(), is(methodName));
      }
    });
  }

  @Test
  public void uniqueNameShouldBeConsistentInAllJVMs() throws Exception {
    final String uniqueName = getClass().getName() + "_" + testMethodName;
    
    assertThat(this.dunitTestRule.getUniqueName(), is(uniqueName));
    
    invokeInEveryVM(new SerializableRunnable(this.dunitTestRule.getMethodName()) {
      @Override
      public void run() {
        assertThat(dunitTestRule.getUniqueName(), is(uniqueName));
      }
    });
  }
}
