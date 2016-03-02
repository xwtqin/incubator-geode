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
package com.gemstone.gemfire.security;

import com.gemstone.gemfire.test.junit.categories.UnitTest;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.Serializable;
import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link NotAuthorizedException}.
 */
@Category(UnitTest.class)
public class NotAuthorizedExceptionTest {

  @Test
  public void isSerializable() throws Exception {
    assertThat(NotAuthorizedException.class).isInstanceOf(Serializable.class);
  }

  @Test
  public void canBeSerialized() throws Exception {
    String message = "my message";
    NotAuthorizedException instance = new NotAuthorizedException(message);

    NotAuthorizedException cloned = (NotAuthorizedException) SerializationUtils.clone(instance);

    assertThat(cloned).hasMessage(message);
  }

  @Test
  public void canBeSerializedWithThrowable() throws Exception {
    String message = "my message";
    Throwable cause = new Exception("the cause");
    NotAuthorizedException instance = new NotAuthorizedException(message, cause);

    NotAuthorizedException cloned = (NotAuthorizedException) SerializationUtils.clone(instance);

    assertThat(cloned).hasMessage(message);
    assertThat(cloned).hasCause(cause);
  }

  @Test
  public void canBeSerializedWithNonSerializablePrincipal() throws Exception {
    String message = "my message";
    Principal mockPrincipal = mock(Principal.class);
    NotAuthorizedException instance = new NotAuthorizedException(message, mockPrincipal);

    NotAuthorizedException cloned = (NotAuthorizedException) SerializationUtils.clone(instance);

    assertThat(cloned).hasMessage(message);
    //assertThat(cloned.getPrincipal()).isEqualTo(mockPrincipal);
    assertThat(cloned.getPrincipal()).isNull();
  }
}
