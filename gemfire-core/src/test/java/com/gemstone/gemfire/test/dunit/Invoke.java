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
package com.gemstone.gemfire.test.dunit;

import java.util.HashMap;
import java.util.Map;

import com.gemstone.gemfire.internal.util.Callable;

/**
 * A set of remote invocation methods useful for writing tests. 
 * <pr/>
 * These methods can be used directly:
 * <code>Invoke.invokeInEveryVM(...)</code>, however, they read better if they
 * are referenced through static import:
 * 
 * <pre>
 * import static com.gemstone.gemfire.test.dunit.Invoke.*;
 *    ...
 *    invokeInEveryVM(...);
 * </pre>
 * <pr/>
 * Extracted from DistributedTestCase
 * 
 * @see VM
 * @see SerializableCallable
 * @see SerializableRunnable
 * @see RepeatableRunnable
 */
public class Invoke {

  protected Invoke() {
  }

  /**
   * Invokes a method in every remote VM that DUnit knows about.
   *
   * @see VM#invoke(Class, String)
   */
  public static void invokeInEveryVM(Class c, String method) {
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
  
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        vm.invoke(c, method);
      }
    }
  }

  /**
   * Invokes a method in every remote VM that DUnit knows about.
   *
   * @see VM#invoke(Class, String)
   */
  public static void invokeInEveryVM(Class c, String method, Object[] methodArgs) {
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
  
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        vm.invoke(c, method, methodArgs);
      }
    }
  }

  /**
   * Invokes a <code>SerializableCallable</code> in every VM that
   * DUnit knows about.
   *
   * @return a Map of results, where the key is the VM and the value is the result
   * @see VM#invoke(Callable)
   */
  public static Map invokeInEveryVM(SerializableCallable work) {
    HashMap ret = new HashMap();
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        ret.put(vm, vm.invoke(work));
      }
    }
    return ret;
  }

  /**
   * Invokes a <code>SerializableRunnable</code> in every VM that
   * DUnit knows about.
   *
   * @see VM#invoke(Runnable)
   */
  public static void invokeInEveryVM(SerializableRunnable work) {
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
  
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        vm.invoke(work);
      }
    }
  }

  public static void invokeInLocator(SerializableRunnable work) {
    Host.getLocator().invoke(work);
  }

  public static void invokeRepeatingIfNecessary(VM vm, RepeatableRunnable task) {
    vm.invokeRepeatingIfNecessary(task, 0);
  }

  public static void invokeRepeatingIfNecessary(VM vm, RepeatableRunnable task, final long repeatTimeoutMs) {
    vm.invokeRepeatingIfNecessary(task, repeatTimeoutMs);
  }

  /**
   * Invokes a <code>SerializableRunnable</code> in every VM that
   * DUnit knows about.  If work.run() throws an assertion failure, 
   * its execution is repeated, until no assertion failure occurs or
   * repeatTimeout milliseconds have passed.
   *
   * @see VM#invoke(Runnable)
   */
  public static void invokeInEveryVMRepeatingIfNecessary(final RepeatableRunnable work) {
    Invoke.invokeInEveryVMRepeatingIfNecessary(work, 0);
  }

  public static void invokeInEveryVMRepeatingIfNecessary(final RepeatableRunnable work, final long repeatTimeoutMs) {
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
  
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        vm.invokeRepeatingIfNecessary(work, repeatTimeoutMs);
      }
    }
  }
  
}
