package com.gemstone.gemfire.internal.lang.reflect;

import static com.gemstone.gemfire.internal.lang.reflect.ReflectionUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.gemstone.gemfire.test.junit.categories.UnitTest;

/**
 * Unit tests for the ReflectionUtils class.
 * 
 * @author Kirk Lund
 */
@Category(UnitTest.class)
public class ReflectionUtilsJUnitTest {

  @Rule
  public TestWatcher watchman = new TestWatcher() {
    @Override
    protected void starting(final Description description) {
      testClassName = description.getClassName();
      testMethodName = description.getMethodName();
    }
  };
  
  private String testClassName;
  private String testMethodName;
  
  @Test
  public void getClassNameZeroShouldReturnReflectionUtilsClass() {
    assertThat(getClassName(0), is(Thread.class.getName()));
  }
  
  @Test
  public void getClassNameOneShouldReturnReflectionUtilsClass() {
    assertThat(getClassName(1), is(ReflectionUtils.class.getName()));
  }
  
  @Test
  public void getClassNameTwoShouldReturnReflectionUtilsClass() {
    assertThat(getClassName(2), is(getClass().getName()));
    assertThat(getClassName(2), is(this.testClassName));
  }
  
  @Test
  public void getClassNameShouldReturnReflectionUtilsClass() {
    assertThat(getClassName(), is(getClass().getName()));
    assertThat(getClassName(), is(this.testClassName));
  }
  
  @Test
  public void getMethodNameZeroShouldReturnGetStackTrace() {
    assertThat(getMethodName(0), is("getStackTrace"));
  }
  
  @Test
  public void getMethodNameOneShouldReturnGetMethodName() {
    assertThat(getMethodName(1), is("getMethodName"));
  }
  
  @Test
  public void getMethodNameTwoShouldReturnThisMethod() {
    assertThat(getMethodName(2), is("getMethodNameTwoShouldReturnThisMethod"));
    assertThat(getMethodName(2), is(this.testMethodName));
  }
  
  @Test
  public void getMethodNameShouldReturnThisMethod() {
    assertThat(getMethodName(), is("getMethodNameShouldReturnThisMethod"));
    assertThat(getMethodName(), is(this.testMethodName));
  }
}
