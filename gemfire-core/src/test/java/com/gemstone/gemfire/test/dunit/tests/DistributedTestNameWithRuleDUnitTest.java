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

import static com.gemstone.gemfire.test.dunit.DistributedTestRule.*;
import static com.gemstone.gemfire.test.dunit.Invoke.invokeInEveryVM;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.Serializable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class DistributedTestNameWithRuleDUnitTest implements Serializable {

  @Rule
  public final DistributedTestRule dunitTestRule = new DistributedTestRule();
  
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
    assertThat(getTestMethodName(), is(methodName));
    
    invokeInEveryVM(new SerializableRunnable(getTestMethodName()) {
      @Override
      public void run() {
        assertThat(getTestMethodName(), is(methodName));
      }
    });
  }

  @Test
  public void uniqueNameShouldBeConsistentInAllJVMs() throws Exception {
    final String uniqueName = getClass().getName() + "_" + testMethodName;
    
    assertThat(getUniqueName(), is(uniqueName));
    
    invokeInEveryVM(new SerializableRunnable(getTestMethodName()) {
      @Override
      public void run() {
        assertThat(getUniqueName(), is(uniqueName));
      }
    });
  }
}
