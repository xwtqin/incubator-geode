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
package com.gemstone.gemfire.test.examples;

import static com.googlecode.catchexception.CatchException.*;
import static org.assertj.core.api.Assertions.*;

import java.io.Serializable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.RMIException;
import com.gemstone.gemfire.test.dunit.SerializableCallable;
import com.gemstone.gemfire.test.dunit.VM;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

/**
 * Using Catch-Exception works well for remote exceptions and asserting details
 * about root cause of RMIExceptions in DUnit tests.
 */
@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class CatchExceptionExampleDUnitTest implements Serializable {

  private static final String REMOTE_THROW_EXCEPTION_MESSAGE = "Throwing remoteThrowException";

  @Rule
  public final DistributedTestRule dunitTestRule = new DistributedTestRule();
  
  @Test
  public void testRemoteInvocationWithException() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);

    catchException(vm).invoke(new ThrowBasicTestException());

    assertThat((Exception)caughtException())
        .isInstanceOf(RMIException.class)
        .hasCause(new BasicTestException(REMOTE_THROW_EXCEPTION_MESSAGE));
  }
  
  protected static class ThrowBasicTestException extends SerializableCallable<Object> {

    @Override
    public Object call() throws Exception {
      throw new BasicTestException(REMOTE_THROW_EXCEPTION_MESSAGE);
    }
  }
  
  protected static class BasicTestException extends RuntimeException {

    public BasicTestException() {
      super();
    }
    
    public BasicTestException(String message) {
      super(message);
    }
  }
}
