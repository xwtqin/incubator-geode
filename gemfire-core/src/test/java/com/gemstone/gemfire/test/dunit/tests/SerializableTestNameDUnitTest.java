package com.gemstone.gemfire.test.dunit.tests;

import static com.gemstone.gemfire.test.dunit.Invoke.invokeInEveryVM;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.internal.lang.reflect.ReflectionUtils;
import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;
import com.gemstone.gemfire.test.junit.rules.SerializableTestName;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class SerializableTestNameDUnitTest implements Serializable {

  @Rule
  public final DistributedTestRule dunitTestRule = new DistributedTestRule();
  
  @Rule
  public final SerializableTestName testName = new SerializableTestName();

  @Before
  public void preconditions() {
    assertThat(Host.getHostCount()).isEqualTo(1);
    assertThat(Host.getHost(0).getVMCount()).isEqualTo(4);
  }
  
  @Test
  public void testNameShouldBeSerializable() {
    final String methodName = ReflectionUtils.getMethodName();
    
    assertThat(this.testName.getMethodName()).isEqualTo(methodName);
    
    invokeInEveryVM(new SerializableRunnable(methodName) {
      @Override
      public void run() {
        assertThat(testName.getMethodName()).isEqualTo(methodName);
      }
    });
  }
}
