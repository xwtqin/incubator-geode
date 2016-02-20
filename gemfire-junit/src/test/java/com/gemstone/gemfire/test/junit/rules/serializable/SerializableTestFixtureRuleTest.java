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
package com.gemstone.gemfire.test.junit.rules.serializable;

import static org.assertj.core.api.Assertions.*;

import com.gemstone.gemfire.test.junit.categories.UnitTest;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.Serializable;

/**
 * Unit tests for {@link SerializableTestFixtureRule}.
 */
@Category(UnitTest.class)
public class SerializableTestFixtureRuleTest {

  @Test
  public void isSerializable() throws Exception {
    assertThat(SerializableTestFixtureRule.class).isInstanceOf(Serializable.class);
  }

  @Test
  public void canBeSerialized() throws Throwable {
    FakeSerializableTestFixtureRule instance = new FakeSerializableTestFixtureRule().value(1);

    FakeSerializableTestFixtureRule cloned = (FakeSerializableTestFixtureRule) SerializationUtils.clone(instance);

    assertThat(instance.value()).isEqualTo(1);
    assertThat(cloned.value()).isEqualTo(1);

    instance.value(2);

    assertThat(instance.value()).isEqualTo(2);
    assertThat(cloned.value()).isEqualTo(1);
  }

  /**
   * Fake SerializableExternalResource with a simple int field.
   */
  private static class FakeSerializableTestFixtureRule extends SerializableTestFixtureRule {

    private int value = 0;

    public FakeSerializableTestFixtureRule value(final int value) {
      this.value = value;
      return this;
    }

    public int value() {
      return this.value;
    }
  }
}
