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

import static com.gemstone.gemfire.test.dunit.DistributedTestRule.disconnectAllFromDS;
import static com.gemstone.gemfire.test.dunit.DistributedTestRule.getSystem;
import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.jayway.awaitility.Awaitility.with;
import static com.jayway.awaitility.Duration.TWO_HUNDRED_MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.IncompatibleSystemException;
import com.gemstone.gemfire.distributed.internal.DM;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.RMIException;
import com.gemstone.gemfire.test.dunit.SerializableCallable;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.dunit.VM;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;
import com.gemstone.gemfire.test.junit.categories.MembershipTest;

/**
 * Tests the functionality of the {@link DistributedMember} class.
 *
 * @author Kirk Lund
 * @since 5.0
 */
@Category({ DistributedTest.class, MembershipTest.class })
@SuppressWarnings("serial")
public class DistributedMemberDUnitTest implements Serializable {

  @Rule
  public final DistributedTestRule dunitTestRule = DistributedTestRule.build();
  
  private Properties config;
  
  @BeforeClass
  public static void beforeClass() {
    disconnectAllFromDS();
  }
  
  @Before
  public void setUp() {
    this.config = createConfig();
  }
  
  @After
  public void after() {
    disconnectAllFromDS();
  }
  
  /**
   * Tests default configuration values.
   */
  @Test
  public void defaultConfigShouldHaveEmptyValues() {
    // arrange
    this.config.setProperty(DistributionConfig.LOCATORS_NAME, ""); 
    this.config.setProperty(DistributionConfig.ROLES_NAME, "");
    this.config.setProperty(DistributionConfig.GROUPS_NAME, "");
    this.config.setProperty(DistributionConfig.NAME_NAME, "");

    // act
    final InternalDistributedSystem system = getSystem(this.config);
    final InternalDistributedMember member = system.getDistributedMember();

    // assert
    assertThat(system.getConfig().getRoles(), equalTo(DistributionConfig.DEFAULT_ROLES));
    assertThat(system.getConfig().getGroups(), equalTo(DistributionConfig.DEFAULT_ROLES));
    assertThat(system.getConfig().getName(), equalTo(DistributionConfig.DEFAULT_NAME));
    
    assertThat(member.getRoles(), is(empty()));
    assertThat(member.getName(), isEmptyString());
    assertThat(member.getGroups(), is(empty()));
  }

  @Test
  public void nameShouldBeUsed() {
    // arrange
    this.config.setProperty(DistributionConfig.LOCATORS_NAME, ""); 
    this.config.setProperty(DistributionConfig.NAME_NAME, "nondefault");

    // act
    final InternalDistributedSystem system = getSystem(this.config);
    final InternalDistributedMember member = system.getDistributedMember();

    // assert
    assertThat(system.getConfig().getName(), equalTo("nondefault"));
    assertThat(member.getName(), equalTo("nondefault"));
  }

  /**
   * Tests the configuration of many Roles and groups in one vm.
   * Confirms no runtime distinction between roles and groups.
   */
  @Test
  public void multipleRolesAndGroupsAreUsed() {
    // arrange
    final String roles = "A,B,C";
    final String groups = "D,E,F,G";
    final List<String> rolesAndGroups = Arrays.asList((roles+","+groups).split(","));
    
    this.config.setProperty(DistributionConfig.LOCATORS_NAME, ""); 
    this.config.setProperty(DistributionConfig.ROLES_NAME, roles);
    this.config.setProperty(DistributionConfig.GROUPS_NAME, groups);

    // act
    final InternalDistributedSystem system = getSystem(this.config);
    final InternalDistributedMember member = system.getDistributedMember();

    // assert
    assertThat(system.getConfig().getRoles(), equalTo(roles));
    assertThat(system.getConfig().getGroups(), equalTo(groups));
    assertThat(member.getRoles().size(), equalTo(rolesAndGroups.size()));
    for (Role role : member.getRoles()) {
      assertThat(role.getName(), isIn(rolesAndGroups));
    }
    assertThat(member.getGroups(), equalTo(rolesAndGroups));
  }

  @Test
  public void secondMemberUsingSameNameShouldFail() {
    // arrange
    Host.getHost(0).getVM(0).invoke(getSystemWithName("name0"));
    Host.getHost(0).getVM(1).invoke(getSystemWithName("name1"));
    
    // act
    catchException(Host.getHost(0).getVM(2)).invoke(getSystemWithName("name0"));
    
    // assert
    assertThat((Exception)caughtException())
        .isInstanceOf(RMIException.class);
    
    assertThat(caughtException().getCause())
        .isInstanceOf(IncompatibleSystemException.class)
        .hasMessageContaining("used the same name");
  }

  private SerializableRunnable getSystemWithName(final String name) {
    return new SerializableRunnable() {
      public void run() {
        Properties config = createConfig();
        config.setProperty(DistributionConfig.NAME_NAME, name);
        getSystem(config);
      }
    };
  }
  
  /**
   * Tests the configuration of one unique Role in each of four vms. Verifies 
   * that each vm is aware of the other vms' Roles.
   */
  @Test
  public void allMembersShouldSeeRoles() {
    // arrange
    final String[] vmRoles = new String[] {"VM_A","VM_B","VM_C","VM_D"};
    for (int i = 0; i < vmRoles.length; i++) {
      final int vm = i;
      Host.getHost(0).getVM(vm).invoke(new SerializableRunnable() {
        public void run() {
          Properties config = createConfig();
          config.setProperty(DistributionConfig.ROLES_NAME, vmRoles[vm]);
          getSystem(config);
        }
      });
    }
    
    // act (and assert)
    for (int i = 0; i < vmRoles.length; i++) {
      final int vm = i;
      Host.getHost(0).getVM(vm).invoke(new SerializableRunnable() {
        public void run() {
          InternalDistributedSystem sys = getSystem();
          assertNotNull(sys.getConfig().getRoles());
          assertTrue(sys.getConfig().getRoles().equals(vmRoles[vm]));
          
          InternalDistributedMember self = sys.getDistributedMember();
          
          Set<Role> myRoles = self.getRoles();
          assertEquals(1, myRoles.size());
          
          Role myRole = (Role) myRoles.iterator().next();
          assertTrue(vmRoles[vm].equals(myRole.getName()));
          
          with().pollInterval(TWO_HUNDRED_MILLISECONDS).await().atMost(60, SECONDS).until( numberOfOtherMembers(), equalTo(3) );
          // Awaitility: used to have a for-loop here
          
          Set<InternalDistributedMember> members = sys.getDM().getOtherNormalDistributionManagerIds();
          for (Iterator<InternalDistributedMember> iterMembers = members.iterator(); iterMembers.hasNext();) {
            InternalDistributedMember member = iterMembers.next();
            Set<Role> roles = member.getRoles();
            assertEquals(1, roles.size());
            for (Iterator<Role> iterRoles = roles.iterator(); iterRoles.hasNext();) {
              Role role = (Role) iterRoles.next();
              assertTrue(!role.getName().equals(myRole.getName()));
              boolean foundRole = false;
              for (int j = 0; j < vmRoles.length; j++) {
                if (vmRoles[j].equals(role.getName())) {
                  foundRole = true;
                  break;
                }
              }
              assertTrue(foundRole);
            }
          }
        }
      });
    }
  }

  private Callable<Integer> numberOfOtherMembers() {
    return new Callable<Integer>() {
      public Integer call() throws Exception {
        return getSystem().getDM().getOtherNormalDistributionManagerIds().size();
      }
    };
  }
  
  /**
   * Tests the configuration of one unique group in each of four vms. Verifies 
   * that each vm is aware of the other vms' groups.
   */
  @Test
  public void allMembersShouldSeeGroups() {  
    // arrange
    for (int i = 0; i < 4; i++) {
      final int vm = i;
      Host.getHost(0).getVM(vm).invoke(new SerializableRunnable() {
        public void run() {
          final Properties config = createConfig();
          config.setProperty(DistributionConfig.GROUPS_NAME, makeGroupsString(vm));
          getSystem(config);
        }
      });
    }
    
    // act (and assert)
    for (int i = 0; i < 4; i++) {
      final int vm = i;
      Host.getHost(0).getVM(vm).invoke(new SerializableRunnable() {
        public void run() {
          InternalDistributedSystem sys = getSystem();
          final String expectedMyGroup = makeGroupsString(vm);
          assertEquals("vm-"+vm, expectedMyGroup, sys.getConfig().getGroups());
          
          DM dm = sys.getDistributionManager();
          DistributedMember self = sys.getDistributedMember();
          
          List<String> myGroups = self.getGroups();
          
          assertEquals(Arrays.asList(""+vm, makeOddEvenString(vm)), myGroups);
          
          Set<DistributedMember> members = null;
          for (int i = 1; i <= 3; i++) {
            try {
              members = dm.getOtherNormalDistributionManagerIds();
              assertEquals(3, members.size());
              break;
            }
            catch (AssertionError e) {
              if (i < 3) {
                sleep(200);
              } else {
                throw e;
              }
            }
          }
          // Make sure getAllOtherMembers returns a set
          // containing our three peers plus an admin member.
          Set<DistributedMember> others = sys.getAllOtherMembers();
          assertEquals(4, others.size());
          others.removeAll(dm.getOtherNormalDistributionManagerIds());
          assertEquals(1, others.size());
          // test getGroupMembers
          Set<DistributedMember> evens = new HashSet<DistributedMember>();
          Set<DistributedMember> odds = new HashSet<DistributedMember>();
          boolean isEvens = true;
          for (String groupName: Arrays.asList("0", "1", "2", "3")) {
            Set<DistributedMember> gm = sys.getGroupMembers(groupName);
            if (isEvens) {
              evens.addAll(gm);
            } else {
              odds.addAll(gm);
            }
            isEvens = !isEvens;
            if (groupName.equals(""+vm)) {
              assertEquals(Collections.singleton(self), gm);
            } else {
              assertEquals(1, gm.size());
              assertEquals("members=" + members + " gm=" + gm, true, members.removeAll(gm));
            }
          }
          assertEquals(Collections.emptySet(), members);
          assertEquals(evens, sys.getGroupMembers("EVENS"));
          assertEquals(odds, sys.getGroupMembers("ODDS"));
        }
      });
    }
  }
  
  /**
   * The method getId() returns a string that is used as a key identifier in
   * the JMX and Admin APIs. This test asserts that it matches the expected 
   * format. If you change DistributedMember.getId() or DistributedSystem.
   * getMemberId() you will need to look closely at the format, decide if it
   * is appropriate for JMX as the id for SystemMember mbeans and then adjust
   * this test as needed.
   *
   * Changing the id can result in bad keys in JMX and can result in numerous
   * errors in Admin/JMX tests.
   */
  @Test
  public void getIdShouldIdentifyMember() {
    // arrange
    this.config.setProperty(DistributionConfig.LOCATORS_NAME, "");
    this.config.setProperty(DistributionConfig.NAME_NAME, "foobar");

    // act
    final InternalDistributedSystem system = getSystem(this.config);
    final DistributedMember member = system.getDistributedMember();
    
    // assert
    assertThat(system.getMemberId(), equalTo(member.getId()));
    assertThat(member.getId(), containsString("foobar"));
  }
  
  /**
   * TODO: move to DistributedSystem DUnit Test
   */
  @Test
  public void findDistributedMembersByNameShouldReturnOneMember() {
    final VM vm0 = Host.getHost(0).getVM(0);
    final VM vm1 = Host.getHost(0).getVM(1);
    final VM vm2 = Host.getHost(0).getVM(2);
    
    final DistributedMember member0 = createSystemAndGetId(vm0, "name0");
    final DistributedMember member1 = createSystemAndGetId(vm1, "name1");
    final DistributedMember member2 = createSystemAndGetId(vm2, "name2");
    
    vm0.invoke(new SerializableCallable<Object>() { // SerializableRunnable
      @Override
      public Object call() throws Exception { // public void run() 
        DistributedSystem system = getSystem();
        assertEquals(member0, system.findDistributedMember("name0"));
        assertEquals(member1, system.findDistributedMember("name1"));
        assertEquals(member2, system.findDistributedMember("name2"));
        assertNull(system.findDistributedMember("name3"));

        Set<DistributedMember> members = system.findDistributedMembers(InetAddress.getByName(member0.getHost()));
        Set<DistributedMember> expected = new HashSet<DistributedMember>();
        expected.add(member0);
        expected.add(member1);
        expected.add(member2);
        
        // Members will contain the locator as well. Just make sure it has the members we're looking for.
        assertTrue("Expected" + expected + " got " + members, members.containsAll(expected));
        assertEquals(4, members.size());
        return null;
      }
    });
  }
  
  private DistributedMember createSystemAndGetId(final VM vm, final String name) {
    return (DistributedMember) vm.invoke(new SerializableCallable<Object>("create system and get member") {
      @Override
      public Object call() throws Exception {
        final Properties config = createConfig();
        config.setProperty(DistributionConfig.NAME_NAME, name);
        final DistributedSystem ds = getSystem(config);
        return ds.getDistributedMember();
      }
    });
  }

  private static Properties createConfig() {
    final Properties props = new Properties();
    props.setProperty(DistributionConfig.MCAST_PORT_NAME, "0");
    return props;
  }
  
  private void sleep(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      fail("interrupted");
    }
  }
  
  private static String makeOddEvenString(final int vm) {
    return ((vm % 2) == 0) ? "EVENS" : "ODDS";
  }
  
  private static String makeGroupsString(final int vm) {
    return "" + vm + ", " + makeOddEvenString(vm);
  }
}
