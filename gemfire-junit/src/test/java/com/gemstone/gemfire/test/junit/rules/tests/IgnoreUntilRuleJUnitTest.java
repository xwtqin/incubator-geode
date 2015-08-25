package com.gemstone.gemfire.test.junit.rules.tests;

import static com.gemstone.gemfire.test.junit.rules.tests.RunTest.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.gemstone.gemfire.test.junit.IgnoreUntil;
import com.gemstone.gemfire.test.junit.categories.UnitTest;
import com.gemstone.gemfire.test.junit.rules.IgnoreUntilRule;

/**
 * Unit tests for IgnoreUntil JUnit Rule
 * 
 * @author Kirk Lund
 */
@Category(UnitTest.class)
public class IgnoreUntilRuleJUnitTest {

  private static final String ASSERTION_ERROR_MESSAGE = "failing test";
  
  @Test
  public void shouldIgnoreWhenUntilIsInFuture() {
    Result result = runTest(ShouldIgnoreWhenUntilIsInFuture.class);
    
    assertTrue(result.wasSuccessful());
    assertThat(ShouldIgnoreWhenUntilIsInFuture.count, is(0));
  }
  
  @Test
  public void shouldExecuteWhenUntilIsInPast() {
    Result result = runTest(ShouldExecuteWhenUntilIsInPast.class);
    
    assertFalse(result.wasSuccessful());
    
    List<Failure> failures = result.getFailures();
    assertEquals("Failures: " + failures, 1, failures.size());

    Failure failure = failures.get(0);
    assertThat(failure.getException(), is(instanceOf(AssertionError.class)));
    assertThat(failure.getException().getMessage(), containsString(ASSERTION_ERROR_MESSAGE));
    assertThat(ShouldExecuteWhenUntilIsInPast.count, is(1));
  }
  
  @Test
  public void shouldExecuteWhenUntilIsDefault() {
    Result result = runTest(ShouldExecuteWhenUntilIsDefault.class);
    
    assertFalse(result.wasSuccessful());
    
    List<Failure> failures = result.getFailures();
    assertEquals("Failures: " + failures, 1, failures.size());

    Failure failure = failures.get(0);
    assertThat(failure.getException(), is(instanceOf(AssertionError.class)));
    assertThat(failure.getException().getMessage(), containsString(ASSERTION_ERROR_MESSAGE));
    assertThat(ShouldExecuteWhenUntilIsDefault.count, is(1));
  }
  
  public static class ShouldIgnoreWhenUntilIsInFuture {
    private static int count;
    
    @Rule
    public final IgnoreUntilRule ignoreUntilRule = new IgnoreUntilRule();
    
    @Test
    @IgnoreUntil(value = "description", until = "3000-01-01")
    public void doTest() throws Exception {
      count++;
      fail(ASSERTION_ERROR_MESSAGE);
    }
  }

  public static class ShouldExecuteWhenUntilIsInPast {
    private static int count;
    
    @Rule
    public final IgnoreUntilRule ignoreUntilRule = new IgnoreUntilRule();
    
    @Test
    @IgnoreUntil(value = "description", until = "1980-01-01")
    public void doTest() throws Exception {
      count++;
      fail(ASSERTION_ERROR_MESSAGE);
    }
  }

  public static class ShouldExecuteWhenUntilIsDefault {
    private static int count;
    
    @Rule
    public final IgnoreUntilRule ignoreUntilRule = new IgnoreUntilRule();
    
    @Test
    @IgnoreUntil(value = "description")
    public void doTest() throws Exception {
      count++;
      fail(ASSERTION_ERROR_MESSAGE);
    }
  }
}
