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

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.SystemFailure;
import com.gemstone.gemfire.admin.internal.AdminDistributedSystemImpl;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.hdfs.internal.hoplog.HoplogConfig;
import com.gemstone.gemfire.cache.query.QueryTestUtils;
import com.gemstone.gemfire.cache.query.internal.QueryObserverHolder;
import com.gemstone.gemfire.cache30.ClientServerTestCase;
import com.gemstone.gemfire.cache30.GlobalLockingDUnitTest;
import com.gemstone.gemfire.cache30.MultiVMRegionTestCase;
import com.gemstone.gemfire.cache30.RegionTestCase;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.DistributionMessageObserver;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem.CreationStackGenerator;
import com.gemstone.gemfire.internal.SocketCreator;
import com.gemstone.gemfire.internal.admin.ClientStatsManager;
import com.gemstone.gemfire.internal.cache.DiskStoreObserver;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.internal.cache.HARegion;
import com.gemstone.gemfire.internal.cache.InitialImageOperation;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.tier.InternalClientMembership;
import com.gemstone.gemfire.internal.cache.tier.sockets.CacheServerTestUtil;
import com.gemstone.gemfire.internal.cache.tier.sockets.ClientProxyMembershipID;
import com.gemstone.gemfire.internal.cache.xmlcache.CacheCreation;
import com.gemstone.gemfire.management.internal.cli.LogWrapper;
import com.gemstone.gemfire.test.dunit.standalone.DUnitLauncher;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

import junit.framework.TestCase;

/**
 * This class is the superclass of all distributed unit tests.
 * 
 * tests/hydra/JUnitTestTask is the main DUnit driver. It supports two 
 * additional public static methods if they are defined in the test case:
 * 
 * public static void caseSetUp() -- comparable to JUnit's BeforeClass annotation
 * 
 * public static void caseTearDown() -- comparable to JUnit's AfterClass annotation
 *
 * @author David Whitlock
 */
@Category(DistributedTest.class)
@SuppressWarnings("serial")
public abstract class DistributedTestCase extends TestCase implements java.io.Serializable {
  
  private static final LinkedHashSet<String> testHistory = new LinkedHashSet<String>();

  private static void setUpCreationStackGenerator() {
    // the following is moved from InternalDistributedSystem to fix #51058
    InternalDistributedSystem.TEST_CREATION_STACK_GENERATOR.set(
    new CreationStackGenerator() {
      @Override
      public Throwable generateCreationStack(final DistributionConfig config) {
        final StringBuilder sb = new StringBuilder();
        final String[] validAttributeNames = config.getAttributeNames();
        for (int i = 0; i < validAttributeNames.length; i++) {
          final String attName = validAttributeNames[i];
          final Object actualAtt = config.getAttributeObject(attName);
          String actualAttStr = actualAtt.toString();
          sb.append("  ");
          sb.append(attName);
          sb.append("=\"");
          if (actualAtt.getClass().isArray()) {
            actualAttStr = InternalDistributedSystem.arrayToString(actualAtt);
          }
          sb.append(actualAttStr);
          sb.append("\"");
          sb.append("\n");
        }
        return new Throwable("Creating distributed system with the following configuration:\n" + sb.toString());
      }
    });
  }
  
  private static void tearDownCreationStackGenerator() {
    InternalDistributedSystem.TEST_CREATION_STACK_GENERATOR.set(InternalDistributedSystem.DEFAULT_CREATION_STACK_GENERATOR);
  }
  
  /** This VM's connection to the distributed system */
  public static InternalDistributedSystem system;
  private static Class lastSystemCreatedInTest;
  private static Properties lastSystemProperties;
  public static volatile String testName;
  
  /** For formatting timing info */
  private static final DecimalFormat format = new DecimalFormat("###.###");

  public static boolean reconnect = false;

  public static final boolean logPerTest = Boolean.getBoolean("dunitLogPerTest");

  /**
   * Creates a new <code>DistributedTestCase</code> test with the
   * given name.
   */
  public DistributedTestCase(final String name) {
    super(name);
    DUnitLauncher.launchIfNeeded();
  }

  protected Class getTestClass() {
    Class clazz = getClass();
    while (clazz.getDeclaringClass() != null) {
      clazz = clazz.getDeclaringClass();
    }
    return clazz;
  }
  
  public void setSystem(final Properties props, final DistributedSystem ds) {
    system = (InternalDistributedSystem)ds;
    lastSystemProperties = props;
    lastSystemCreatedInTest = getTestClass();
  }
  
  /**
   * Returns this VM's connection to the distributed system.  If
   * necessary, the connection will be lazily created using the given
   * <code>Properties</code>.  Note that this method uses hydra's
   * configuration to determine the location of log files, etc.
   * Note: "final" was removed so that WANTestBase can override this method.
   * This was part of the xd offheap merge.
   *
   * see hydra.DistributedConnectionMgr#connect
   * @since 3.0
   */
  public /*final*/ InternalDistributedSystem getSystem(final Properties props) {
    // Setting the default disk store name is now done in setUp
    if (system == null) {
      system = InternalDistributedSystem.getAnyInstance();
    }
    if (system == null || !system.isConnected()) {
      // Figure out our distributed system properties
      Properties p = DistributedTestSupport.getAllDistributedSystemProperties(props);
      lastSystemCreatedInTest = getTestClass();
      if (logPerTest) {
        String testMethod = getTestName();
        String testName = lastSystemCreatedInTest.getName() + '-' + testMethod;
        String oldLogFile = p.getProperty(DistributionConfig.LOG_FILE_NAME);
        p.put(DistributionConfig.LOG_FILE_NAME, 
            oldLogFile.replace("system.log", testName+".log"));
        String oldStatFile = p.getProperty(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME);
        p.put(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME, 
            oldStatFile.replace("statArchive.gfs", testName+".gfs"));
      }
      system = (InternalDistributedSystem)DistributedSystem.connect(p);
      lastSystemProperties = p;
    } else {
      boolean needNewSystem = false;
      if(!getTestClass().equals(lastSystemCreatedInTest)) {
        Properties newProps = DistributedTestSupport.getAllDistributedSystemProperties(props);
        needNewSystem = !newProps.equals(lastSystemProperties);
        if(needNewSystem) {
          LogWriterSupport.getLogWriter().info(
              "Test class has changed and the new DS properties are not an exact match. "
                  + "Forcing DS disconnect. Old props = "
                  + lastSystemProperties + "new props=" + newProps);
        }
      } else {
        Properties activeProps = system.getProperties();
        for (Iterator iter = props.entrySet().iterator();
        iter.hasNext(); ) {
          Map.Entry entry = (Map.Entry) iter.next();
          String key = (String) entry.getKey();
          String value = (String) entry.getValue();
          if (!value.equals(activeProps.getProperty(key))) {
            needNewSystem = true;
            LogWriterSupport.getLogWriter().info("Forcing DS disconnect. For property " + key
                                + " old value = " + activeProps.getProperty(key)
                                + " new value = " + value);
            break;
          }
        }
      }
      if(needNewSystem) {
        // the current system does not meet our needs to disconnect and
        // call recursively to get a new system.
        LogWriterSupport.getLogWriter().info("Disconnecting from current DS in order to make a new one");
        disconnectFromDS();
        getSystem(props);
      }
    }
    return system;
  }

  private String getDefaultDiskStoreName() {
    String vmid = System.getProperty("vmid");
    return "DiskStore-"  + vmid + "-"+ getTestClass().getCanonicalName() + "." + getTestName();
  }

  /**
   * Returns this VM's connection to the distributed system.  If
   * necessary, the connection will be lazily created using the
   * <code>Properties</code> returned by {@link
   * #getDistributedSystemProperties}.
   *
   * @see #getSystem(Properties)
   *
   * @since 3.0
   */
  public final InternalDistributedSystem getSystem() {
    return getSystem(this.getDistributedSystemProperties());
  }

  /**
   * Returns a loner distributed system that isn't connected to other
   * vms
   * 
   * @since 6.5
   */
  public final InternalDistributedSystem getLonerSystem() {
    Properties props = this.getDistributedSystemProperties();
    props.put(DistributionConfig.MCAST_PORT_NAME, "0");
    props.put(DistributionConfig.LOCATORS_NAME, "");
    return getSystem(props);
  }
  
  /**
   * Returns a loner distributed system in combination with enforceUniqueHost
   * and redundancyZone properties.
   * Added specifically to test scenario of defect #47181.
   */
  public final InternalDistributedSystem getLonerSystemWithEnforceUniqueHost() {
    Properties props = this.getDistributedSystemProperties();
    props.put(DistributionConfig.MCAST_PORT_NAME, "0");
    props.put(DistributionConfig.LOCATORS_NAME, "");
    props.put(DistributionConfig.ENFORCE_UNIQUE_HOST_NAME, "true");
    props.put(DistributionConfig.REDUNDANCY_ZONE_NAME, "zone1");
    return getSystem(props);
  }

  /**
   * Returns whether or this VM is connected to a {@link
   * DistributedSystem}.
   */
  public final boolean isConnectedToDS() {
    return system != null && system.isConnected();
  }

  /**
   * Returns a <code>Properties</code> object used to configure a
   * connection to a {@link
   * com.gemstone.gemfire.distributed.DistributedSystem}.
   * Unless overridden, this method will return an empty
   * <code>Properties</code> object.
   *
   * @since 3.0
   */
  public Properties getDistributedSystemProperties() {
    return new Properties();
  }

  /**
   * Sets up the test (noop).
   */
  @Override
  public void setUp() throws Exception {
    logTestHistory();
    setUpCreationStackGenerator();
    testName = getName();
    System.setProperty(HoplogConfig.ALLOW_LOCAL_HDFS_PROP, "true");
    
    if (testName != null) {
      GemFireCacheImpl.setDefaultDiskStoreName(getDefaultDiskStoreName());
      String baseDefaultDiskStoreName = getTestClass().getCanonicalName() + "." + getTestName();
      for (int h = 0; h < Host.getHostCount(); h++) {
        Host host = Host.getHost(h);
        for (int v = 0; v < host.getVMCount(); v++) {
          VM vm = host.getVM(v);
          String vmDefaultDiskStoreName = "DiskStore-" + h + "-" + v + "-" + baseDefaultDiskStoreName;
          vm.invoke(DistributedTestCase.class, "perVMSetUp", new Object[] {testName, vmDefaultDiskStoreName});
        }
      }
    }
    System.out.println("\n\n[setup] START TEST " + getClass().getSimpleName()+"."+testName+"\n\n");
  }

  /**
   * Write a message to the log about what tests have ran previously. This
   * makes it easier to figure out if a previous test may have caused problems
   */
  private void logTestHistory() {
    String classname = getClass().getSimpleName();
    testHistory.add(classname);
    System.out.println("Previously run tests: " + testHistory);
  }

  public static void perVMSetUp(final String name, final String defaultDiskStoreName) {
    setTestName(name);
    GemFireCacheImpl.setDefaultDiskStoreName(defaultDiskStoreName);
    System.setProperty(HoplogConfig.ALLOW_LOCAL_HDFS_PROP, "true");    
  }
  
  private static void setTestName(final String name) {
    testName = name;
  }
  
  public static String getTestName() {
    return testName;
  }

  /**
   * For logPerTest to work, we have to disconnect from the DS, but all
   * subclasses do not call super.tearDown(). To prevent this scenario
   * this method has been declared final. Subclasses must now override
   * {@link #tearDown2()} instead.
   * @throws Exception
   */
  @Override
  public final void tearDown() throws Exception {
    tearDownCreationStackGenerator();
    tearDown2();
    realTearDown();
    tearDownAfter();
  }

  /**
   * Tears down the test. This method is called by the final {@link #tearDown()} method and should be overridden to
   * perform actual test cleanup and release resources used by the test.  The tasks executed by this method are
   * performed before the DUnit test framework using Hydra cleans up the client VMs.
   * <p/>
   * @throws Exception if the tear down process and test cleanup fails.
   * @see #tearDown
   * @see #tearDownAfter()
   */
  // TODO rename this method to tearDownBefore and change the access modifier to protected!
  public void tearDown2() throws Exception {
  }

  protected void realTearDown() throws Exception {
    if (logPerTest) {
      disconnectFromDS();
      Invoke.invokeInEveryVM(DistributedTestCase.class, "disconnectFromDS");
    }
    cleanupAllVms();
  }
  
  /**
   * Tears down the test.  Performs additional tear down tasks after the DUnit tests framework using Hydra cleans up
   * the client VMs.  This method is called by the final {@link #tearDown()} method and should be overridden to perform
   * post tear down activities.
   * <p/>
   * @throws Exception if the test tear down process fails.
   * @see #tearDown()
   * @see #tearDown2()
   */
  protected void tearDownAfter() throws Exception {
  }

  public static void cleanupAllVms() {
    cleanupThisVM();
    Invoke.invokeInEveryVM(()->cleanupThisVM());
    Invoke.invokeInLocator(new SerializableRunnable() {
      public void run() {
        DistributionMessageObserver.setInstance(null);
        DistributedTestSupport.unregisterInstantiatorsInThisVM();
      }
    });
    DUnitLauncher.closeAndCheckForSuspects();
  }

  private static void cleanupThisVM() {
    closeCache();
    
    SocketCreator.resolve_dns = true;
    CacheCreation.clearThreadLocals();
    System.getProperties().remove("gemfire.log-level");
    System.getProperties().remove("jgroups.resolve_dns");
    InitialImageOperation.slowImageProcessing = 0;
    DistributionMessageObserver.setInstance(null);
    QueryTestUtils.setCache(null);
    CacheServerTestUtil.clearCacheReference();
    RegionTestCase.preSnapshotRegion = null;
    GlobalLockingDUnitTest.region_testBug32356 = null;
    LogWrapper.close();
    ClientProxyMembershipID.system = null;
    MultiVMRegionTestCase.CCRegion = null;
    InternalClientMembership.unregisterAllListeners();
    ClientStatsManager.cleanupForTests();
    ClientServerTestCase.AUTO_LOAD_BALANCE = false;
    DistributedTestSupport.unregisterInstantiatorsInThisVM();
    DistributionMessageObserver.setInstance(null);
    QueryObserverHolder.reset();
    DiskStoreObserver.setInstance(null);
    System.getProperties().remove("gemfire.log-level");
    System.getProperties().remove("jgroups.resolve_dns");
    
    if (InternalDistributedSystem.systemAttemptingReconnect != null) {
      InternalDistributedSystem.systemAttemptingReconnect.stopReconnecting();
    }
    IgnoredException.removeAllExpectedExceptions();
  }

  private static void closeCache() {
    GemFireCacheImpl cache = GemFireCacheImpl.getInstance();
    if(cache != null && !cache.isClosed()) {
      destroyRegions(cache);
      cache.close();
    }
  }
  
  protected static final void destroyRegions(final Cache cache) {
    if (cache != null && !cache.isClosed()) {
      //try to destroy the root regions first so that
      //we clean up any persistent files.
      for (Iterator itr = cache.rootRegions().iterator(); itr.hasNext();) {
        Region root = (Region)itr.next();
        //for colocated regions you can't locally destroy a partitioned
        //region.
        if(root.isDestroyed() || root instanceof HARegion || root instanceof PartitionedRegion) {
          continue;
        }
        try {
          root.localDestroyRegion("teardown");
        }
        catch (VirtualMachineError e) {
          SystemFailure.initiateFailure(e);
          throw e;
        }
        catch (Throwable t) {
          LogWriterSupport.getLogWriter().error(t);
        }
      }
    }
  }
  
  public static void disconnectAllFromDS() {
    disconnectFromDS();
    Invoke.invokeInEveryVM(DistributedTestCase.class, "disconnectFromDS");
  }

  /**
   * Disconnects this VM from the distributed system
   */
  public static void disconnectFromDS() {
    testName = null;
    GemFireCacheImpl.testCacheXml = null;
    if (system != null) {
      system.disconnect();
      system = null;
    }
    
    for (;;) {
      DistributedSystem ds = InternalDistributedSystem.getConnectedInstance();
      if (ds == null) {
        break;
      }
      try {
        ds.disconnect();
      } catch (Exception e) {
        // ignore
      }
    }
    
    AdminDistributedSystemImpl ads = AdminDistributedSystemImpl.getConnectedInstance();
    if (ads != null) {// && ads.isConnected()) {
      ads.disconnect();
    }
  }

  /**
   * Returns a unique name for this test method.  It is based on the
   * name of the class as well as the name of the method.
   */
  public String getUniqueName() {
    return getClass().getSimpleName() + "_" + getName();
  }
}
