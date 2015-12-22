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
import static org.assertj.core.api.Assertions.*;

import java.io.Serializable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class LogPerTestMethodDUnitTest implements Serializable {

  @Rule
  public final DistributedTestRule dunitTestRule = DistributedTestRule.builder().logPerTestMethod(true).build();

  @Test
  public void getTestClassNameShouldReturnThisClass() {
    assertThat(getTestClassName()).isEqualTo(getClass().getName());
  }
  
  @Test
  public void logFileNameShouldEqualThisMethodName() {
    InternalDistributedSystem mySystem = getSystem();
    
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.LOG_FILE_NAME, getUniqueName() + ".log");
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME, getUniqueName() + ".gfs");
  }
  
  @Test
  public void logFileNameShouldChangeToThisMethodName() {
    InternalDistributedSystem mySystem = getSystem();
    
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.LOG_FILE_NAME, getUniqueName() + ".log");
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME, getUniqueName() + ".gfs");
  }
}
