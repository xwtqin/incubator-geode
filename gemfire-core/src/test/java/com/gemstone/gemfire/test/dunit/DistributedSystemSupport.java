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

import java.io.File;

import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.distributed.internal.membership.gms.MembershipManagerHelper;

public class DistributedSystemSupport {

  /**
   * Crash the cache in the given VM in such a way that it immediately stops communicating with
   * peers.  This forces the VM's membership manager to throw a ForcedDisconnectException by
   * forcibly terminating the JGroups protocol stack with a fake EXIT event.<p>
   * 
   * NOTE: if you use this method be sure that you clean up the VM before the end of your
   * test with disconnectFromDS() or disconnectAllFromDS().
   */
  public static void crashDistributedSystem(final DistributedSystem msys) {
    MembershipManagerHelper.crashDistributedSystem(msys);
    MembershipManagerHelper.inhibitForcedDisconnectLogging(false);
    WaitCriterion wc = new WaitCriterion() {
      public boolean done() {
        return !msys.isConnected();
      }
      public String description() {
        return "waiting for distributed system to finish disconnecting: " + msys;
      }
    };
    Wait.waitForCriterion(wc, 10000, 1000, true);
  }

  /**
   * Crash the cache in the given VM in such a way that it immediately stops communicating with
   * peers.  This forces the VM's membership manager to throw a ForcedDisconnectException by
   * forcibly terminating the JGroups protocol stack with a fake EXIT event.<p>
   * 
   * NOTE: if you use this method be sure that you clean up the VM before the end of your
   * test with disconnectFromDS() or disconnectAllFromDS().
   */
  public static boolean crashDistributedSystem(VM vm) {
    return (Boolean)vm.invoke(new SerializableCallable("crash distributed system") {
      public Object call() throws Exception {
        DistributedSystem msys = InternalDistributedSystem.getAnyInstance();
        crashDistributedSystem(msys);
        return true;
      }
    });
  }

  /** 
   * delete locator state files.  Use this after getting a random port
   * to ensure that an old locator state file isn't picked up by the
   * new locator you're starting.
   * @param ports
   */
  public static void deleteLocatorStateFile(int... ports) {
    for (int i=0; i<ports.length; i++) {
      File stateFile = new File("locator"+ports[i]+"view.dat");
      if (stateFile.exists()) {
        stateFile.delete();
      }
    }
  }

}
