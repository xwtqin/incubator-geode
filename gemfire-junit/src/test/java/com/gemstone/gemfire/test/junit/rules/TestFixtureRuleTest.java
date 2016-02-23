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
package com.gemstone.gemfire.test.junit.rules;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Result;

/**
 * Unit tests for {@link TestFixtureRule}.
 */
public class TestFixtureRuleTest {

  @Test
  public void methodRuleAndClassRuleShouldInvokeCallbacksInOrder() {
    Result result = TestRunner.runTest(MethodRuleAndClassRuleInvocations.class);

    assertThat(result.wasSuccessful()).isTrue();
    assertThat(MethodRuleAndClassRuleInvocations.invocations().beforeClassInvocation).isEqualTo(1);
    assertThat(MethodRuleAndClassRuleInvocations.invocations().beforeInvocation).isEqualTo(2);
    assertThat(MethodRuleAndClassRuleInvocations.invocations().testInvocation).isEqualTo(3);
    assertThat(MethodRuleAndClassRuleInvocations.invocations().afterInvocation).isEqualTo(4);
    assertThat(MethodRuleAndClassRuleInvocations.invocations().afterClassInvocation).isEqualTo(5);
  }

  @Test
  public void methodRuleShouldInvokeCallbacksInOrder() {
    Result result = TestRunner.runTest(MethodRuleInvocations.class);

    assertThat(result.wasSuccessful()).isTrue();
    assertThat(MethodRuleInvocations.invocations().beforeClassInvocation).isEqualTo(0);
    assertThat(MethodRuleInvocations.invocations().beforeInvocation).isEqualTo(1);
    assertThat(MethodRuleInvocations.invocations().testInvocation).isEqualTo(2);
    assertThat(MethodRuleInvocations.invocations().afterInvocation).isEqualTo(3);
    assertThat(MethodRuleInvocations.invocations().afterClassInvocation).isEqualTo(0);
  }

  @Test
  public void classRuleShouldInvokeCallbacksInOrder() {
    Result result = TestRunner.runTest(ClassRuleInvocations.class);

    assertThat(result.wasSuccessful()).isTrue();
    assertThat(ClassRuleInvocations.invocations().beforeClassInvocation).isEqualTo(1);
    assertThat(ClassRuleInvocations.invocations().beforeInvocation).isEqualTo(0);
    assertThat(ClassRuleInvocations.invocations().testInvocation).isEqualTo(2);
    assertThat(ClassRuleInvocations.invocations().afterInvocation).isEqualTo(0);
    assertThat(ClassRuleInvocations.invocations().afterClassInvocation).isEqualTo(3);
  }

  @Test
  public void beforeClassThrowsExceptionShouldSkipBeforeTestAndAfterClass() {
    Result result = TestRunner.runTest(BeforeClassThrowsException.class);

    assertThat(result.wasSuccessful()).isFalse();
    assertThat(BeforeClassThrowsException.invocations().beforeClassInvocation).isEqualTo(1);
    assertThat(BeforeClassThrowsException.invocations().beforeInvocation).isEqualTo(0);
    assertThat(BeforeClassThrowsException.invocations().testInvocation).isEqualTo(0);
    assertThat(BeforeClassThrowsException.invocations().afterInvocation).isEqualTo(0);
    assertThat(BeforeClassThrowsException.invocations().afterClassInvocation).isEqualTo(0);
  }

  @Test
  public void beforeClassThrowsErrorShouldSkipBeforeTestAndAfterClass() {
    Result result = TestRunner.runTest(BeforeClassThrowsError.class);

    assertThat(result.wasSuccessful()).isFalse();
    assertThat(BeforeClassThrowsError.invocations().beforeClassInvocation).isEqualTo(1);
    assertThat(BeforeClassThrowsError.invocations().beforeInvocation).isEqualTo(0);
    assertThat(BeforeClassThrowsError.invocations().testInvocation).isEqualTo(0);
    assertThat(BeforeClassThrowsError.invocations().afterInvocation).isEqualTo(0);
    assertThat(BeforeClassThrowsError.invocations().afterClassInvocation).isEqualTo(0);
  }

  @Test
  public void beforeThrowsExceptionShouldSkipTestAndAfter() {
    Result result = TestRunner.runTest(BeforeThrowsException.class);

    assertThat(result.wasSuccessful()).isFalse();
    assertThat(BeforeThrowsException.invocations().beforeClassInvocation).isEqualTo(1);
    assertThat(BeforeThrowsException.invocations().beforeInvocation).isEqualTo(2);
    assertThat(BeforeThrowsException.invocations().testInvocation).isEqualTo(0);
    assertThat(BeforeThrowsException.invocations().afterInvocation).isEqualTo(0);
    assertThat(BeforeThrowsException.invocations().afterClassInvocation).isEqualTo(3);
  }

  @Test
  public void beforeThrowsErrorShouldSkipTestAndAfter() {
    Result result = TestRunner.runTest(BeforeThrowsError.class);

    assertThat(result.wasSuccessful()).isFalse();
    assertThat(BeforeThrowsError.invocations().beforeClassInvocation).isEqualTo(1);
    assertThat(BeforeThrowsError.invocations().beforeInvocation).isEqualTo(2);
    assertThat(BeforeThrowsError.invocations().testInvocation).isEqualTo(0);
    assertThat(BeforeThrowsError.invocations().afterInvocation).isEqualTo(0);
    assertThat(BeforeThrowsError.invocations().afterClassInvocation).isEqualTo(3);
  }

  /**
   * Used by test {@link #methodRuleAndClassRuleShouldInvokeCallbacksInOrder()}
   */
  public static class MethodRuleAndClassRuleInvocations {

    @ClassRule
    public static SpyRule staticRule = new SpyRule(new Invocations());

    @Rule
    public SpyRule rule = staticRule;

    static Invocations invocations() {
      return staticRule.invocations;
    }

    @Test
    public void doTest() throws Exception {
      rule.test();
    }
  }

  /**
   * Used by test {@link #classRuleShouldInvokeCallbacksInOrder()}
   */
  public static class ClassRuleInvocations {

    @ClassRule
    public static SpyRule staticRule = new SpyRule(new Invocations());

    static Invocations invocations() {
      return staticRule.invocations;
    }

    @Test
    public void doTest() throws Exception {
      staticRule.test();
    }
  }

  /**
   * Used by test {@link #methodRuleShouldInvokeCallbacksInOrder()}
   */
  public static class MethodRuleInvocations {

    // do NOT use @ClassRule
    static SpyRule staticSpy = new SpyRule(new Invocations());

    @Rule
    public SpyRule rule = staticSpy;

    static Invocations invocations() {
      return staticSpy.invocations;
    }

    @Test
    public void doTest() throws Exception {
      rule.test();
    }
  }

  public static class BeforeClassThrowsException {

    static final Throwable throwable = new Exception("Thrown by BeforeClassThrowsException");

    @ClassRule
    public static SpyRule staticRule = SpyRule.builder()
        .withInvocations(new Invocations())
        .beforeClassThrows(throwable)
        .build();

    @Rule
    public SpyRule rule = staticRule;

    static Invocations invocations() {
      return staticRule.invocations;
    }

    @Test
    public void doTest() throws Exception {
      rule.test();
    }
  }

  public static class BeforeClassThrowsError {

    static final Throwable throwable = new Error("Thrown by BeforeClassThrowsError");

    @ClassRule
    public static SpyRule staticRule = SpyRule.builder()
        .withInvocations(new Invocations())
        .beforeClassThrows(throwable)
        .build();

    @Rule
    public SpyRule rule = staticRule;

    static Invocations invocations() {
      return staticRule.invocations;
    }

    @Test
    public void doTest() throws Exception {
      rule.test();
    }
  }

  public static class BeforeThrowsException {

    static final Throwable throwable = new Exception("Thrown by BeforeThrowsException");

    @ClassRule
    public static SpyRule staticRule = SpyRule.builder()
      .withInvocations(new Invocations())
      .beforeThrows(throwable)
      .build();

    @Rule
    public SpyRule rule = staticRule;

    static Invocations invocations() {
      return staticRule.invocations;
    }

    @Test
    public void doTest() throws Exception {
      rule.test();
    }
  }

  public static class BeforeThrowsError {

    static final Throwable throwable = new Error("Thrown by BeforeThrowsError");

    @ClassRule
    public static SpyRule staticRule = SpyRule.builder()
        .withInvocations(new Invocations())
        .beforeThrows(throwable)
        .build();

    @Rule
    public SpyRule rule = staticRule;

    static Invocations invocations() {
      return staticRule.invocations;
    }

    @Test
    public void doTest() throws Exception {
      rule.test();
    }
  }

  /**
   * Structure of rule callback and test invocations
   */
  public static class Invocations {
    int invocation = 0;
    int beforeClassInvocation = 0;
    int afterClassInvocation = 0;
    int beforeInvocation = 0;
    int afterInvocation = 0;
    int testInvocation = 0;

    void invokedTest() {
      testInvocation = ++invocation;
    }
    void invokedBeforeClass() {
      beforeClassInvocation = ++invocation;
    }
    void invokedAfterClass() {
      afterClassInvocation = ++invocation;
    }
    void invokedBefore() {
      beforeInvocation = ++invocation;
    }
    void invokedAfter() {
      afterInvocation = ++invocation;
    }
  }

  /**
   * Implementation of TestRule that records the order of callbacks invoked on
   * it. Used by {@link TestFixtureRuleTest}.
   */
  public static class SpyRule extends TestFixtureRule {

    static SpyRuleBuilder builder() {
      return new SpyRuleBuilder();
    }

    private final Invocations invocations;
    private final Throwable beforeClassThrowable;
    private final Throwable beforeThrowable;

    SpyRule(Invocations invocations) {
      this.invocations = invocations;
      this.beforeClassThrowable = null;
      this.beforeThrowable = null;
    }

    SpyRule(SpyRuleBuilder builder) {
      this.invocations = builder.invocations;
      this.beforeClassThrowable = builder.beforeClassThrowable;
      this.beforeThrowable = builder.beforeThrowable;
    }

    Invocations invocations() {
      return this.invocations;
    }

    void test() {
      this.invocations.invokedTest();
    }

    @Override
    protected void beforeClass() throws Throwable {
      this.invocations.invokedBeforeClass();
      if (this.beforeClassThrowable != null) {
        throw this.beforeClassThrowable;
      }
    }

    @Override
    protected void afterClass() {
      this.invocations.invokedAfterClass();
    }

    @Override
    protected void before() throws Throwable {
      this.invocations.invokedBefore();
      if (this.beforeThrowable != null) {
        throw this.beforeThrowable;
      }
    }

    @Override
    protected void after() {
      this.invocations.invokedAfter();
    }
  }

  /**
   * Builder for more control of constructing an instance of {@link SpyRule}
   */
  public static class SpyRuleBuilder {

    Invocations invocations;
    Throwable beforeClassThrowable;
    Throwable beforeThrowable;

    SpyRuleBuilder withInvocations(Invocations invocations) {
      this.invocations = invocations;
      return this;
    }

    SpyRuleBuilder beforeClassThrows(Throwable throwable) {
      this.beforeClassThrowable = throwable;
      return this;
    }

    SpyRuleBuilder beforeThrows(Throwable throwable) {
      this.beforeThrowable = throwable;
      return this;
    }

    SpyRule build() {
      return new SpyRule(this);
    }
  }
}
