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

import static com.gemstone.gemfire.test.dunit.Invoke.invokeInEveryVM;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.Description;

import com.gemstone.gemfire.internal.lang.reflect.ReflectionUtils;
import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;
import com.gemstone.gemfire.test.junit.rules.SerializableTestWatcher;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class SerializableTestWatcherDUnitTest implements Serializable {

  @Rule
  public final DistributedTestRule dunitTestRule = new DistributedTestRule();
  
  @Rule
  public final SerializableTestWatcher watchman = new SerializableTestWatcher() {
    @Override
    protected void starting(final Description description) {
      testClassName = description.getClassName();
      testMethodName = description.getMethodName();
    }
  };

  private String testClassName;
  private String testMethodName;

  @Before
  public void preconditions() {
    assertThat(Host.getHostCount()).isEqualTo(1);
    assertThat(Host.getHost(0).getVMCount()).isEqualTo(4);
  }
  
  @Test
  public void testWatcherShouldBeSerializable() {
    final String methodName = ReflectionUtils.getMethodName();
    final String className = getClass().getName();
    
    invokeInEveryVM(new SerializableRunnable(this.testMethodName) {
      @Override
      public void run() {
        assertThat(getTestMethodName()).isEqualTo(methodName);
        assertThat(getTestClassName()).isEqualTo(className);
      }
    });
  }

  private String getTestMethodName() {
    return this.testMethodName;
  }
  
  private String getTestClassName() {
    return this.testClassName;
  }
}
