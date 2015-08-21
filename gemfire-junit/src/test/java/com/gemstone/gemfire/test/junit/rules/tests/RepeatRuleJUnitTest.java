package com.gemstone.gemfire.test.junit.rules.tests;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.gemstone.gemfire.test.junit.Repeat;
import com.gemstone.gemfire.test.junit.categories.UnitTest;
import com.gemstone.gemfire.test.junit.rules.RepeatRule;

/**
 * Unit tests for Repeat JUnit Rule.
 * 
 * @author Kirk Lund
 */
@Category(UnitTest.class)
public class RepeatRuleJUnitTest {

  private static final String ASSERTION_ERROR_MESSAGE = "failing test";
  
  @Test
  public void failingTestShouldFailOneTimeWhenRepeatIsUnused() {
    Result result = runTest(FailingTestShouldFailOneTimeWhenRepeatIsUnused.class);
    
    assertFalse(result.wasSuccessful());
    
    List<Failure> failures = result.getFailures();
    assertEquals("Failures: " + failures, 1, failures.size());

    Failure failure = failures.get(0);
    assertThat(failure.getException(), is(instanceOf(AssertionError.class)));
    assertThat(failure.getException().getMessage(), containsString(ASSERTION_ERROR_MESSAGE));
    assertThat(FailingTestShouldFailOneTimeWhenRepeatIsUnused.count, is(1));
  }

  @Test
  public void passingTestShouldPassOneTimeWhenRepeatIsUnused() {
    Result result = runTest(PassingTestShouldPassOneTimeWhenRepeatIsUnused.class);
    
    assertTrue(result.wasSuccessful());
    assertThat(PassingTestShouldPassOneTimeWhenRepeatIsUnused.count, is(1));
  }

  @Test
  public void failingTestShouldBeSkippedWhenRepeatIsZero() {
    Result result = runTest(FailingTestShouldBeSkippedWhenRepeatIsZero.class);
    
    assertTrue(result.wasSuccessful());
    assertThat(FailingTestShouldBeSkippedWhenRepeatIsZero.count, is(0));
  }

  @Test
  public void passingTestShouldBeSkippedWhenRepeatIsZero() {
    Result result = runTest(PassingTestShouldBeSkippedWhenRepeatIsZero.class);
    
    assertTrue(result.wasSuccessful());
    assertThat(PassingTestShouldBeSkippedWhenRepeatIsZero.count, is(0));
  }

  @Test
  public void failingTestShouldFailOneTimeWhenRepeatIsOne() {
    Result result = runTest(FailingTestShouldFailOneTimeWhenRepeatIsOne.class);
    
    assertFalse(result.wasSuccessful());
    
    List<Failure> failures = result.getFailures();
    assertEquals("Failures: " + failures, 1, failures.size());

    Failure failure = failures.get(0);
    assertThat(failure.getException(), is(instanceOf(AssertionError.class)));
    assertThat(failure.getException().getMessage(), containsString(ASSERTION_ERROR_MESSAGE));
    assertThat(FailingTestShouldFailOneTimeWhenRepeatIsOne.count, is(1));
  }

  @Test
  public void passingTestShouldPassOneTimeWhenRepeatIsOne() {
    Result result = runTest(PassingTestShouldPassOneTimeWhenRepeatIsOne.class);
    
    assertTrue(result.wasSuccessful());
    assertThat(PassingTestShouldPassOneTimeWhenRepeatIsOne.count, is(1));
  }

  @Test
  public void failingTestShouldFailOneTimeWhenRepeatIsTwo() {
    Result result = runTest(FailingTestShouldFailOneTimeWhenRepeatIsTwo.class);
    
    assertFalse(result.wasSuccessful());
    
    List<Failure> failures = result.getFailures();
    assertEquals("Failures: " + failures, 1, failures.size());

    Failure failure = failures.get(0);
    assertThat(failure.getException(), is(instanceOf(AssertionError.class)));
    assertThat(failure.getException().getMessage(), containsString(ASSERTION_ERROR_MESSAGE));
    assertThat(FailingTestShouldFailOneTimeWhenRepeatIsTwo.count, is(1));
  }

  @Test
  public void passingTestShouldPassTwoTimesWhenRepeatIsTwo() {
    Result result = runTest(PassingTestShouldPassTwoTimesWhenRepeatIsTwo.class);
    
    assertTrue(result.wasSuccessful());
    assertThat(PassingTestShouldPassTwoTimesWhenRepeatIsTwo.count, is(2));
  }

  @Test
  public void failingTestShouldFailOneTimeWhenRepeatIsThree() {
    Result result = runTest(FailingTestShouldFailOneTimeWhenRepeatIsThree.class);
    
    assertFalse(result.wasSuccessful());
    
    List<Failure> failures = result.getFailures();
    assertEquals("Failures: " + failures, 1, failures.size());

    Failure failure = failures.get(0);
    assertThat(failure.getException(), is(instanceOf(AssertionError.class)));
    assertThat(failure.getException().getMessage(), containsString(ASSERTION_ERROR_MESSAGE));
    assertThat(FailingTestShouldFailOneTimeWhenRepeatIsThree.count, is(1));
  }

  @Test
  public void passingTestShouldPassThreeTimesWhenRepeatIsThree() {
    Result result = runTest(PassingTestShouldPassThreeTimesWhenRepeatIsThree.class);
    
    assertTrue(result.wasSuccessful());
    assertThat(PassingTestShouldPassThreeTimesWhenRepeatIsThree.count, is(3));
  }

  private static Result runTest(Class<?> test) {
    JUnitCore junitCore = new JUnitCore();
    return junitCore.run(Request.aClass(test).getRunner());
  }
  
  public static class FailingTestShouldFailOneTimeWhenRepeatIsUnused {
    protected static int count = 0;
    
    @Rule
    public RepeatRule repeat = new RepeatRule();

    @Test
    public void doTest() throws Exception {
      count++;
      fail(ASSERTION_ERROR_MESSAGE);
    }
  }

  public static class PassingTestShouldPassOneTimeWhenRepeatIsUnused {
    protected static int count = 0;
    
    @Rule
    public RepeatRule repeat = new RepeatRule();

    @Test
    public void doTest() throws Exception {
      count++;
    }
  }

  public static class FailingTestShouldBeSkippedWhenRepeatIsZero {
    protected static int count = 0;
    
    @Rule
    public RepeatRule repeat = new RepeatRule();

    @Test
    @Repeat(0)
    public void doTest() throws Exception {
      count++;
      fail(ASSERTION_ERROR_MESSAGE);
    }
  }

  public static class PassingTestShouldBeSkippedWhenRepeatIsZero {
    protected static int count = 0;
    
    @Rule
    public RepeatRule repeat = new RepeatRule();

    @Test
    @Repeat(0)
    public void doTest() throws Exception {
      count++;
    }
  }
  
  public static class FailingTestShouldFailOneTimeWhenRepeatIsOne {
    protected static int count = 0;
    
    @Rule
    public RepeatRule repeat = new RepeatRule();

    @Test
    @Repeat(1)
    public void doTest() throws Exception {
      count++;
      fail(ASSERTION_ERROR_MESSAGE);
    }
  }

  public static class PassingTestShouldPassOneTimeWhenRepeatIsOne {
    protected static int count = 0;
    
    @Rule
    public RepeatRule repeat = new RepeatRule();

    @Test
    @Repeat(1)
    public void doTest() throws Exception {
      count++;
    }
  }

  public static class FailingTestShouldFailOneTimeWhenRepeatIsTwo {
    protected static int count = 0;
    
    @Rule
    public RepeatRule repeat = new RepeatRule();

    @Test
    @Repeat(2)
    public void doTest() throws Exception {
      count++;
      fail(ASSERTION_ERROR_MESSAGE);
    }
  }

  public static class PassingTestShouldPassTwoTimesWhenRepeatIsTwo {
    protected static int count = 0;
    
    @Rule
    public RepeatRule repeat = new RepeatRule();

    @Test
    @Repeat(2)
    public void doTest() throws Exception {
      count++;
    }
  }

  public static class FailingTestShouldFailOneTimeWhenRepeatIsThree {
    protected static int count = 0;
    
    @Rule
    public RepeatRule repeat = new RepeatRule();

    @Test
    @Repeat(3)
    public void doTest() throws Exception {
      count++;
      fail(ASSERTION_ERROR_MESSAGE);
    }
  }

  public static class PassingTestShouldPassThreeTimesWhenRepeatIsThree {
    protected static int count = 0;
    
    @Rule
    public RepeatRule repeat = new RepeatRule();

    @Test
    @Repeat(3)
    public void doTest() throws Exception {
      count++;
    }
  }
}
