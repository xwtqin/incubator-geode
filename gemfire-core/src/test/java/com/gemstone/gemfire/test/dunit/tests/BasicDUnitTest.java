/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
package com.gemstone.gemfire.test.dunit.tests;

import static com.gemstone.gemfire.test.dunit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.test.dunit.AsyncInvocation;
import com.gemstone.gemfire.test.dunit.DistributedTestCase;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.RMIException;
import com.gemstone.gemfire.test.dunit.VM;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

/**
 * This class tests the basic functionality of the distributed unit
 * test framework.
 */
@Category(DistributedTest.class)
public class BasicDUnitTest extends DistributedTestCase {
  private static final long serialVersionUID = 1L;

  private static final String REMOTE_THROW_EXCEPTION_MESSAGE = "Throwing remoteThrowException";
  
  private static Properties bindings = new Properties();

  /**
   * Tests how the DUnit framework handles an error
   */
  @Test(expected = RMIException.class)
  public void testDontCatchRemoteException() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    vm.invoke(getClass(), "remoteThrowException");
  }

  @Test
  public void testRemoteInvocationWithException() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    try {
      vm.invoke(getClass(), "remoteThrowException");
      fail("Should have thrown a BasicTestException");

    } catch (RMIException expected) {
      Throwable cause = expected.getCause();
      assertThat(cause, is(instanceOf(BasicTestException.class)));
      assertThat(cause.getMessage(), is("Throwing remoteThrowException"));
    }
  }
  
  @Test
  public void testRemoteInvokeAsync() throws Exception {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    String name = this.getUniqueName();
    String value = "Hello";

    AsyncInvocation ai = vm.invokeAsync(this.getClass(), "remoteBind", new Object[] { name, value });
    ai.join();
    if (ai.exceptionOccurred()) {
      fail("remoteBind failed", ai.getException());
    }

    ai = vm.invokeAsync(this.getClass(), "remoteValidateBind", new Object[] {name, value });
    ai.join();
    if (ai.exceptionOccurred()) {
      fail("remoteValidateBind failed", ai.getException());
    }
  }

  @Test
  public void testRemoteInvokeAsyncWithException() throws Exception {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);

    AsyncInvocation ai = vm.invokeAsync(this.getClass(), "remoteThrowException");
    ai.join();
    assertTrue(ai.exceptionOccurred());
    Throwable ex = ai.getException();
    assertTrue(ex instanceof BasicTestException);
  }

  @Test
  @Ignore("not implemented")
  public void testRemoteInvocationBoolean() {
  }
  
  /**
   * Accessed via reflection.  DO NOT REMOVE
   */
  protected static void remoteThrowException() {
    throw new BasicTestException(REMOTE_THROW_EXCEPTION_MESSAGE);
  }
  
  protected static void remoteBind(String name, String s) {
    new BasicDUnitTest().getSystem(); // forces connection
    bindings.setProperty(name, s);
  }

  protected static void remoteValidateBind(String name, String expected) {
    assertEquals(expected, bindings.getProperty(name));
  }

  protected static class BasicTestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BasicTestException() {
      super();
    }
    
    public BasicTestException(String message) {
      super(message);
    }
  }
}
