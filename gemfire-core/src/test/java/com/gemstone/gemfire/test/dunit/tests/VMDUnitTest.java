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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Serializable;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.test.dunit.AsyncInvocation;
import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.RMIException;
import com.gemstone.gemfire.test.dunit.VM;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

/**
 * This class tests the functionality of the {@link VM} class.
 */
@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class VMDUnitTest implements Serializable {
  
  private static final boolean BOOLEAN_VALUE = true;
  private static final byte BYTE_VALUE = (byte) 40;
  private static final long LONG_VALUE = 42L;
  private static final String STRING_VALUE = "BLAH BLAH BLAH";

  private static final AtomicInteger COUNTER = new AtomicInteger();
  
  @Rule
  public final DistributedTestRule dunitTestRule = new DistributedTestRule();

  @Test
  public void testInvokeNonExistentMethod() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    try {
      vm.invoke(VMDUnitTest.class, "nonExistentMethod");
      fail("Should have thrown an RMIException");

    } catch (RMIException ex) {
      String s = "Excepted a NoSuchMethodException, got a " + ex.getCause();;
      assertTrue(s, ex.getCause() instanceof NoSuchMethodException);
    }
  }

  @Test
  public void testInvokeStaticBoolean() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    assertEquals(BOOLEAN_VALUE, vm.invokeBoolean(VMDUnitTest.class, "remoteBooleanMethod")); 
  }

  @Test
  public void testInvokeStaticBooleanNotBoolean() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    try {
      vm.invokeBoolean(VMDUnitTest.class, "remoteByteMethod");
      fail("Should have thrown an IllegalArgumentException");

    } catch (IllegalArgumentException ex) {
      String s = "Method \"remoteByteMethod\" in class \"" + getClass().getName() + "\" returned a \"" + Byte.class.getName() + "\" expected a boolean";
      assertThat(ex.getMessage(), equalTo(s));
    }
  }

  @Test
  public void testInvokeStaticLong() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    assertEquals(LONG_VALUE, vm.invokeLong(VMDUnitTest.class, "remoteLongMethod")); 
  }

  @Test
  public void testInvokeStaticLongNotLong() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    try {
      vm.invokeLong(VMDUnitTest.class, "remoteByteMethod");
      fail("Should have thrown an IllegalArgumentException");

    } catch (IllegalArgumentException ex) {
      String s = "Method \"remoteByteMethod\" in class \"" + getClass().getName() + "\" returned a \"" + Byte.class.getName() + "\" expected a long";
      assertThat(ex.getMessage(), equalTo(s));
    }
  }

  @Test
  public void testInvokeInstanceLong() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    assertEquals(LONG_VALUE, vm.invokeLong(new ClassWithLong(), "getLong"));
  }

  @Test
  public void testInvokeInstanceLongNotLong() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    try {
      vm.invokeLong(new ClassWithByte(), "getByte");
      fail("Should have thrown an IllegalArgumentException");

    } catch (IllegalArgumentException ex) {
      String s = "Method \"getByte\" in class \"" + ClassWithByte.class.getName() + "\" returned a \"" + Byte.class.getName() + "\" expected a long";
      assertThat(ex.getMessage(), equalTo(s));
    }
  }

  @Test
  public void testInvokeInstance() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    assertEquals(STRING_VALUE, vm.invoke(new ClassWithString(), "getString"));
  }

  @Test
  public void testInvokeRunnable() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);
    try {
      vm.invoke(new InvokeRunnable());
      fail("Should have thrown a BasicTestException");

    } catch (RMIException ex) {
      assertTrue(ex.getCause() instanceof BasicDUnitTest.BasicTestException);
    }
  }
  
  @Test
  public void testReturnValue() throws Exception {
    final Host host = Host.getHost(0);
    final VM vm = host.getVM(0);
    // Assert class static invocation works
    AsyncInvocation a1 = vm.invokeAsync(getClass(), "getAndIncStaticCount");
    a1.join();
    assertEquals(new Integer(0), a1.getReturnValue());
    // Assert class static invocation with args works
    a1 = vm.invokeAsync(getClass(), "incrementStaticCount", new Object[] {new Integer(2)});
    a1.join();
    assertEquals(new Integer(3), a1.getReturnValue());
    // Assert that previous values are not returned when invoking method w/ no return val
    a1 = vm.invokeAsync(getClass(), "incStaticCount");
    a1.join();
    assertNull(a1.getReturnValue());
    // Assert that previous null returns are over-written 
    a1 = vm.invokeAsync(getClass(), "getAndIncStaticCount");
    a1.join();
    assertEquals(new Integer(4), a1.getReturnValue());

    // Assert object method invocation works with zero arg method
    final VMTestObject o = new VMTestObject(0);
    a1 = vm.invokeAsync(o, "incrementAndGet", new Object[] {});
    a1.join();
    assertEquals(new Integer(1), a1.getReturnValue());
    // Assert object method invocation works with no return
    a1 = vm.invokeAsync(o, "set", new Object[] {new Integer(3)});
    a1.join();
    assertNull(a1.getReturnValue());
  }

  protected static Integer getAndIncStaticCount() {
    return new Integer(COUNTER.getAndIncrement());
  }
  
  protected static Integer incrementStaticCount(Integer inc) {
    return new Integer(COUNTER.addAndGet(inc.intValue()));
  }
  
  protected static void incStaticCount() {
    COUNTER.incrementAndGet();
  }
  
  /**
   * Accessed via reflection.  DO NOT REMOVE
   */
  protected static byte remoteByteMethod() {
    return BYTE_VALUE;
  }

  /**
   * Accessed via reflection.  DO NOT REMOVE
   */
  protected static boolean remoteBooleanMethod() {
    return BOOLEAN_VALUE;
  }

  /**
   * Accessed via reflection.  DO NOT REMOVE
   */
  protected static long remoteLongMethod() {
    return LONG_VALUE;
  }

  protected static class InvokeRunnable implements Serializable, Runnable {
    private static final long serialVersionUID = 1L;
    @Override
    public void run() {
      throw new BasicDUnitTest.BasicTestException();
    }
  }

  protected static class ClassWithString implements Serializable {
    private static final long serialVersionUID = 1L;
    public String getString() {
      return STRING_VALUE;
    }
  }

  protected static class VMTestObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private final AtomicInteger val;
    public VMTestObject(int init) {
      this.val = new AtomicInteger(init);
    }
    public Integer get() {
      return new Integer(this.val.get());
    }
    public Integer incrementAndGet() {
      return new Integer(this.val.incrementAndGet());
    }
    public void set(Integer newVal) {
      this.val.set(newVal.intValue());
    }
  }
  
  protected static class ClassWithLong implements Serializable {
    private static final long serialVersionUID = 1L;
    public long getLong() {
      return LONG_VALUE;
    }
  }

  protected static class ClassWithByte implements Serializable {
    private static final long serialVersionUID = 1L;
    public byte getByte() {
      return BYTE_VALUE;
    }
  }
}
