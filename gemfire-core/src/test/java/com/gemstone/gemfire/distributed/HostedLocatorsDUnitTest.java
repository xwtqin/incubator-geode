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
package com.gemstone.gemfire.distributed;

import static com.gemstone.gemfire.test.dunit.DistributedTestRule.*;
import static com.gemstone.gemfire.test.dunit.Invoke.*;
import static com.jayway.awaitility.Awaitility.*;
import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

import com.gemstone.gemfire.distributed.AbstractLauncher.Status;
import com.gemstone.gemfire.distributed.LocatorLauncher.Builder;
import com.gemstone.gemfire.distributed.LocatorLauncher.LocatorState;
import com.gemstone.gemfire.distributed.internal.DistributionManager;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.distributed.internal.InternalLocator;
import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.internal.AvailablePortHelper;
import com.gemstone.gemfire.internal.SocketCreator;
import com.gemstone.gemfire.internal.util.StopWatch;
import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.SerializableCallable;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.dunit.rules.DistributedRestoreSystemProperties;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;
import com.gemstone.gemfire.test.junit.categories.MembershipTest;
import com.gemstone.gemfire.test.junit.rules.SerializableExternalResource;

/**
 * Extracted from LocatorLauncherLocalJUnitTest.
 * 
 * @author Kirk Lund
 * @since 8.0
 */
@Category({ DistributedTest.class, MembershipTest.class })
@SuppressWarnings("serial")
public class HostedLocatorsDUnitTest implements Serializable {

  protected static final int TIMEOUT_MINUTES = 5;

  protected transient volatile int locatorPort;
  protected transient volatile LocatorLauncher launcher;
  
  @Rule
  public final DistributedTestRule dunitTestRule = DistributedTestRule.builder()
      .disconnectBefore(true)
      .disconnectAfter(true)
      .innerRule(new DistributedRestoreSystemProperties())
      .build();

  @Test
  public void getAllHostedLocators() throws Exception {
    final InternalDistributedSystem system = getSystem();
    final String dunitLocator = system.getConfig().getLocators();
    assertNotNull(dunitLocator);
    assertFalse(dunitLocator.isEmpty());

    final int[] ports = AvailablePortHelper.getRandomAvailableTCPPorts(4);
    
    final String uniqueName = getUniqueName();
    for (int i = 0 ; i < 4; i++) {
      final int whichvm = i;
      Host.getHost(0).getVM(whichvm).invoke(new SerializableCallable<Object>() {
        @Override
        public Object call() throws Exception {
          try {
            System.setProperty("gemfire.locators", dunitLocator);
            System.setProperty("gemfire.mcast-port", "0");
            
            final String name = uniqueName + "-" + whichvm;
            final File subdir = new File(name);
            subdir.mkdir();
            assertTrue(subdir.exists() && subdir.isDirectory());
            
            final Builder builder = new Builder()
                .setMemberName(name)
                .setPort(ports[whichvm])
                .setRedirectOutput(true)
                .setWorkingDirectory(name);
    
            launcher = builder.build();
            assertEquals(Status.ONLINE, launcher.start().getStatus());
            with().pollInterval(10, MILLISECONDS).await().atMost(TIMEOUT_MINUTES, MINUTES).until( isLocatorStarted() );
            return null;
          } finally {
//            System.clearProperty("gemfire.locators");
//            System.clearProperty("gemfire.mcast-port");
          }
        }
      });
    }
    
    final String host = SocketCreator.getLocalHost().getHostAddress();
    
    final Set<String> locators = new HashSet<String>();
    locators.add(host + "[" + dunitLocator.substring(dunitLocator.indexOf("[")+1, dunitLocator.indexOf("]")) + "]");
    for (int port : ports) {
      locators.add(host +"[" + port + "]");
    }

    // validation within non-locator
    final DistributionManager dm = (DistributionManager)system.getDistributionManager();
    
    final Set<InternalDistributedMember> locatorIds = dm.getLocatorDistributionManagerIds();
    assertEquals(5, locatorIds.size());
    
    final Map<InternalDistributedMember, Collection<String>> hostedLocators = dm.getAllHostedLocators();
    assertTrue(!hostedLocators.isEmpty());
    assertEquals(5, hostedLocators.size());
    
    for (InternalDistributedMember member : hostedLocators.keySet()) {
      assertEquals(1, hostedLocators.get(member).size());
      final String hostedLocator = hostedLocators.get(member).iterator().next();
      assertTrue(locators + " does not contain " + hostedLocator, locators.contains(hostedLocator));
    }

    // validate fix for #46324
    for (int whichvm = 0 ; whichvm < 4; whichvm++) {
      Host.getHost(0).getVM(whichvm).invoke(new SerializableRunnable() {
        @Override
        public void run() {
          final DistributionManager dm = (DistributionManager)InternalDistributedSystem.getAnyInstance().getDistributionManager();
          final InternalDistributedMember self = dm.getDistributionManagerId();
          
          final Set<InternalDistributedMember> locatorIds = dm.getLocatorDistributionManagerIds();
          assertTrue(locatorIds.contains(self));
          
          final Map<InternalDistributedMember, Collection<String>> hostedLocators = dm.getAllHostedLocators();
          assertTrue("hit bug #46324: " + hostedLocators + " is missing " + InternalLocator.getLocatorStrings() + " for " + self, hostedLocators.containsKey(self));
        }
      });
    }
    
    // validation with locators
    for (int whichvm = 0 ; whichvm < 4; whichvm++) {
      Host.getHost(0).getVM(whichvm).invoke(new SerializableRunnable() {
        @Override
        public void run() {
          final DistributionManager dm = (DistributionManager)InternalDistributedSystem.getAnyInstance().getDistributionManager();
          
          final Set<InternalDistributedMember> locatorIds = dm.getLocatorDistributionManagerIds();
          assertEquals(5, locatorIds.size());
          
          final Map<InternalDistributedMember, Collection<String>> hostedLocators = dm.getAllHostedLocators();
          assertTrue(!hostedLocators.isEmpty());
          assertEquals(5, hostedLocators.size());
          
          for (InternalDistributedMember member : hostedLocators.keySet()) {
            assertEquals(1, hostedLocators.get(member).size());
            final String hostedLocator = hostedLocators.get(member).iterator().next();
            assertTrue(locators + " does not contain " + hostedLocator, locators.contains(hostedLocator));
          }
        }
      });
    }
  }

  private Callable<Boolean> isLocatorStarted() {
    return new Callable<Boolean>() {
      public Boolean call() throws Exception {
        final LocatorState LocatorState = launcher.status();
        return (LocatorState != null && Status.ONLINE.equals(LocatorState.getStatus()));
      }
    };
  }
  
  protected void waitForLocatorToStart(final LocatorLauncher launcher, int timeout, int interval, boolean throwOnTimeout) throws Exception {
    assertEventuallyTrue("waiting for process to start: " + launcher.status(), new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        try {
          final LocatorState LocatorState = launcher.status();
          return (LocatorState != null && Status.ONLINE.equals(LocatorState.getStatus()));
        }
        catch (RuntimeException e) {
          return false;
        }
      }
    }, timeout, interval);
  }

  protected static void assertEventuallyTrue(final String message, final Callable<Boolean> callable, final int timeout, final int interval) throws Exception {
    boolean done = false;
    for (StopWatch time = new StopWatch(true); !done && time.elapsedTimeMillis() < timeout; done = (callable.call())) {
      Thread.sleep(interval);
    }
    assertTrue(message, done);
  }
}
