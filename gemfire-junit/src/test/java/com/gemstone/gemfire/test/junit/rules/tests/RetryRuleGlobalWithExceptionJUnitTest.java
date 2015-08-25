package com.gemstone.gemfire.test.junit.rules.tests;

import static com.gemstone.gemfire.test.junit.rules.tests.RunTest.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.gemstone.gemfire.test.junit.Retry;
import com.gemstone.gemfire.test.junit.categories.UnitTest;
import com.gemstone.gemfire.test.junit.rules.RetryRule;

/**
 * Unit tests for Retry JUnit Rule involving global scope (ie Rule affects all 
 * tests in the test case) with failures due to an Exception.
 * 
 * @author Kirk Lund
 */
@Category(UnitTest.class)
public class RetryRuleGlobalWithExceptionJUnitTest {
  
  @Test
  public void zeroIsIllegal() {
    Result result = runTest(ZeroIsIllegal.class);
    
    assertFalse(result.wasSuccessful());
    
    List<Failure> failures = result.getFailures();
    assertEquals("Failures: " + failures, 1, failures.size());

    Failure failure = failures.get(0);
    assertThat(failure.getException(), is(instanceOf(IllegalArgumentException.class)));
    assertThat(failure.getException().getMessage(), containsString("Retry count must be greater than zero"));
    assertThat(ZeroIsIllegal.count, is(0));
  }
  
  @Test
  public void failsWithOne() {
    Result result = runTest(FailsWithOne.class);
    
    assertFalse(result.wasSuccessful());
    
    List<Failure> failures = result.getFailures();
    assertEquals("Failures: " + failures, 1, failures.size());

    Failure failure = failures.get(0);
    assertThat(failure.getException(), is(instanceOf(CustomException.class)));
    assertThat(failure.getException().getMessage(), containsString(FailsWithOne.message));
    assertThat(FailsWithOne.count, is(1));
  }
  
  @Test
  public void passesWithOne() {
    Result result = runTest(PassesWithOne.class);
    
    assertTrue(result.wasSuccessful());
  }
  
  @Test
  public void passesWithUnused() {
    Result result = runTest(PassesWhenUnused.class);
    
    assertTrue(result.wasSuccessful());
  }
  
  @Test
  public void failsOnSecondAttempt() {
    Result result = runTest(FailsOnSecondAttempt.class);
    
    assertFalse(result.wasSuccessful());
    
    List<Failure> failures = result.getFailures();
    assertEquals(1, failures.size());

    Failure failure = failures.get(0);
    assertThat(failure.getException(), is(instanceOf(CustomException.class)));
    assertThat(failure.getException().getMessage(), containsString(FailsOnSecondAttempt.message));
    assertThat(FailsOnSecondAttempt.count, is(2));
  }

  @Test
  public void passesOnSecondAttempt() {
    Result result = runTest(PassesOnSecondAttempt.class);
    
    assertTrue(result.wasSuccessful());
    assertThat(PassesOnSecondAttempt.count, is(2));
  }
  
  @Test
  public void failsOnThirdAttempt() {
    Result result = runTest(FailsOnThirdAttempt.class);
    
    assertFalse(result.wasSuccessful());
    
    List<Failure> failures = result.getFailures();
    assertEquals(1, failures.size());

    Failure failure = failures.get(0);
    assertThat(failure.getException(), is(instanceOf(CustomException.class)));
    assertThat(failure.getException().getMessage(), containsString(FailsOnThirdAttempt.message));
    assertThat(FailsOnThirdAttempt.count, is(3));
  }

  @Test
  public void passesOnThirdAttempt() {
    Result result = runTest(PassesOnThirdAttempt.class);
    
    assertTrue(result.wasSuccessful());
    assertThat(PassesOnThirdAttempt.count, is(3));
  }
  
  public static class CustomException extends Exception {
    private static final long serialVersionUID = 1L;
    public CustomException(final String message) {
      super(message);
    }
  }
  
  public static class ZeroIsIllegal {
    protected static int count;

    @Rule
    public RetryRule retryRule = new RetryRule(0);

    @Test
    public void zeroIsIllegal() throws Exception {
      count++;
    }
  }
  
  public static class FailsWithOne {
    protected static int count;
    protected static String message;

    @Rule
    public RetryRule retryRule = new RetryRule(1);

    @Test
    public void failsWithOne() throws Exception {
      count++;
      message = "Failing " + count;
      throw new CustomException(message);
    }
  }
  
  public static class PassesWithOne {
    protected static int count;

    @Rule
    public RetryRule retryRule = new RetryRule(1);

    @Test
    public void passesWithOne() throws Exception {
      count++;
    }
  }
  
  public static class PassesWhenUnused {
    protected static int count;

    @Rule
    public RetryRule retryRule = new RetryRule(2);

    @Test
    public void passesWithUnused() throws Exception {
      count++;
    }
  }
  
  public static class FailsOnSecondAttempt {
    protected static int count;
    protected static String message;

    @Rule
    public RetryRule retryRule = new RetryRule(2);

    @Test
    @Retry(2)
    public void failsOnSecondAttempt() throws Exception {
      count++;
      message = "Failing " + count;
      throw new CustomException(message);
    }
  }
  
  public static class PassesOnSecondAttempt {
    protected static int count;
    protected static String message;
    
    @Rule
    public RetryRule retryRule = new RetryRule(2);

    @Test
    @Retry(2)
    public void failsOnSecondAttempt() throws Exception {
      count++;
      if (count < 2) {
        message = "Failing " + count;
        throw new CustomException(message);
      }
    }
  }
  
  public static class FailsOnThirdAttempt {
    protected static int count;
    protected static String message;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    @Retry(3)
    public void failsOnThirdAttempt() throws Exception {
      count++;
      message = "Failing " + count;
      throw new CustomException(message);
    }
  }

  public static class PassesOnThirdAttempt {
    protected static int count;
    protected static String message;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void failsOnThirdAttempt() throws Exception {
      count++;
      if (count < 3) {
        message = "Failing " + count;
        throw new CustomException(message);
      }
    }
  }
}
