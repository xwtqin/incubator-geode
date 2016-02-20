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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * An abstract base class for test rules that combine {@code ClassRule} and
 * method {@code Rule} test fixture lifecycle callbacks. Subclasses may
 * override any or all of these methods:
 * <p><ul>
 * <li></li>{@link #beforeClass()}
 * <li></li>{@link #afterClass()}
 * <li></li>{@link #before()}
 * <li></li>{@link #after()}
 * </ul>
 *
 * <p>The rule variable does not have to be static in order to implement
 * {@link #beforeClass()} and {@link #afterClass()}.
 *
 * <p>Example:
 *
 * <pre>
 * public class SomeTest {
 *
 *   \@Rule
 *   public TestFixtureRule testFixtureRule = new TestFixtureRule() {
 *     \@Override
 *     protected void beforeClass() throws Throwable {
 *       // setup executed once before all tests in SomeTest
 *     }
 *     \@Override
 *     protected void afterClass() {
 *       // teardown executed once after all tests in SomeTest
 *     }
 *     \@Override
 *     protected void before() throws Throwable {
 *       // setup executed before each test in SomeTest
 *     }
 *     \@Override
 *     protected void after() {
 *       // teardown executed after each test in SomeTest
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Kirk Lund
 */
public class TestFixtureRule implements TestRule {

  @Override
  public Statement apply(final Statement base, final Description description) {
    if (description.isSuite()) {
      return createClassStatement(base);
    } else if (description.isTest()) {
      return createMethodStatement(base);
    }
    return base;
  }

  /**
   * Returns new <code>Statement</code> for invoking <code>beforeClass</code>
   * and <code>afterClass</code>.
   */
  protected Statement createClassStatement(final Statement base) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        beforeClass();
        try {
          base.evaluate();
        } finally {
          afterClass();
        }
      }
    };
  }

  /**
   * Returns new <code>Statement</code> for invoking <code>before</code>
   * and <code>after</code>.
   */
  protected Statement createMethodStatement(final Statement base) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        before();
        try {
          base.evaluate();
        } finally {
          after();
        }
      }
    };
  }

  /**
   * Override to perform custom setup during <code>beforeClass</code> which
   * is invoked prior to {@link #before()} and all test methods.
   *
   * If any <code>Throwable</code> is thrown, then <code>afterClass</code> will
   * be disabled.
   *
   * @throws Throwable if setup fails
   */
  protected void beforeClass() throws Throwable {
  }

  /**
   * Override to perform custom tearDown during <code>afterClass</code> which
   * is invoked following {@link #after()} and all test methods.
   */
  protected void afterClass() {
  }

  /**
   * Override to perform custom setup before each test method.
   *
   * If any <code>Throwable</code> is thrown, then <code>after</code> will
   * be disabled.
   *
   * @throws Throwable if setup fails
   */
  protected void before() throws Throwable {
    // do nothing
  }

  /**
   * Override to perform custom tearDown during after each test method.
   */
  protected void after() {
    // do nothing
  }
}
