package com.gemstone.gemfire.test.dunit.tests;

import static com.gemstone.gemfire.test.junit.rules.tests.RunTest.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.Result;

import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;
import com.gemstone.gemfire.test.junit.rules.SerializableExternalResource;

/**
 * Distributed tests for chaining of rules to DUnitTestRule
 * 
 * @author Kirk Lund
 */
@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class DUnitTestRuleChainDUnitTest implements Serializable {

  private static enum Expected { 
    BEFORE_ONE_BEFORE, BEFORE_TWO_BEFORE, AFTER_ONE_BEFORE, AFTER_TWO_BEFORE, 
    TEST,
    AFTER_TWO_AFTER, AFTER_ONE_AFTER, BEFORE_TWO_AFTER, BEFORE_ONE_AFTER };
  
  private static List<Expected> invocations = Collections.synchronizedList(new ArrayList<Expected>());
  
  @Test
  public void chainedRulesShouldBeInvokedInCorrectOrder() {
    Result result = runTest(DUnitTestWithChainedRules.class);
    
    assertThat(result.wasSuccessful()).isTrue();
    assertThat(invocations).as("Wrong order: " + invocations).containsExactly(Expected.values());
  }
  
  public static class DUnitTestWithChainedRules implements Serializable {
    
    @Rule
    public final DistributedTestRule dunitTestRule = DistributedTestRule.builder()
        .outerRule(new BeforeOne())
        .outerRule(new BeforeTwo())
        .innerRule(new AfterOne())
        .innerRule(new AfterTwo())
        .build();
    
    @Test
    public void doTest() {
      invocations.add(Expected.TEST);
    }
  }
  
  public static class BeforeOne extends SerializableExternalResource {
    @Override
    protected void before() throws Throwable {
      invocations.add(Expected.BEFORE_ONE_BEFORE);
    }
    @Override
    protected void after() throws Throwable {
      invocations.add(Expected.BEFORE_ONE_AFTER);
    }
  }

  public static class BeforeTwo extends SerializableExternalResource {
    @Override
    protected void before() throws Throwable {
      invocations.add(Expected.BEFORE_TWO_BEFORE);
    }
    @Override
    protected void after() throws Throwable {
      invocations.add(Expected.BEFORE_TWO_AFTER);
    }
  }

  public static class AfterOne extends SerializableExternalResource {
    @Override
    protected void before() throws Throwable {
      invocations.add(Expected.AFTER_ONE_BEFORE);
    }
    @Override
    protected void after() throws Throwable {
      invocations.add(Expected.AFTER_ONE_AFTER);
    }
  }

  public static class AfterTwo extends SerializableExternalResource {
    @Override
    protected void before() throws Throwable {
      invocations.add(Expected.AFTER_TWO_BEFORE);
    }
    @Override
    protected void after() throws Throwable {
      invocations.add(Expected.AFTER_TWO_AFTER);
    }
  }
}
