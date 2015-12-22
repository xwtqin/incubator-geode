/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * Distributed tests for chaining of rules to DistributedTestRule
 * 
 * @author Kirk Lund
 */
@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class DistributedTestRuleChainDUnitTest implements Serializable {

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
