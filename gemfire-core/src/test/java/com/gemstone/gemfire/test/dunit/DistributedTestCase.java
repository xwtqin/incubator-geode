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
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
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
import com.gemstone.gemfire.distributed.internal.membership.gms.MembershipManagerHelper;
import com.gemstone.gemfire.internal.InternalDataSerializer;
import com.gemstone.gemfire.internal.InternalInstantiator;
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
import com.gemstone.gemfire.internal.cache.tier.sockets.DataSerializerPropogationDUnitTest;
import com.gemstone.gemfire.internal.cache.xmlcache.CacheCreation;
import com.gemstone.gemfire.internal.logging.InternalLogWriter;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.internal.logging.log4j.LogWriterLogger;
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
public abstract class DistributedTestCase extends TestCase implements Serializable {
  private static final Logger logger = LogService.getLogger();
  private static final LogWriterLogger oldLogger = LogWriterLogger.create(logger);
  private static final LinkedHashSet<String> testHistory = new LinkedHashSet<String>();

  private static volatile String previousTestName;
  private static volatile String testName;
  
  /** This VM's connection to the distributed system */
  public static InternalDistributedSystem system;
  private static Class lastSystemCreatedInTest;
  private static Properties lastSystemProperties;

  /** For formatting timing info */
  private static final DecimalFormat format = new DecimalFormat("###.###");

  public static boolean reconnect = false;

  public static final boolean logPerTest = Boolean.getBoolean("dunitLogPerTest");

  /**
   * Creates a new <code>DistributedTestCase</code> test with the given name.
   */
  public DistributedTestCase(String name) {
    super(name);
    DUnitLauncher.launchIfNeeded();
  }

  //---------------------------------------------------------------------------
  // ??? methods
  //---------------------------------------------------------------------------
  
  //---------------------------------------------------------------------------
  // setUp methods
  //---------------------------------------------------------------------------
  
  @Override
  public void setUp() throws Exception {
    logTestHistory();
    setUpCreationStackGenerator();
    setTestName(getName());
    
    System.setProperty(HoplogConfig.ALLOW_LOCAL_HDFS_PROP, "true");
    GemFireCacheImpl.setDefaultDiskStoreName(getDefaultDiskStoreName()); // TODO: not thread safe
      
    for (int h = 0; h < Host.getHostCount(); h++) {
      Host host = Host.getHost(h);
      for (int v = 0; v < host.getVMCount(); v++) {
        VM vm = host.getVM(v);
        final String vmDefaultDiskStoreName = "DiskStore-" + h + "-" + v + "-" + getClass().getSimpleName() + "." + getTestName();
        setUpInVM(vm, getTestName(), vmDefaultDiskStoreName);
      }
    }
    //System.out.println("\n\n[setup] START TEST " + getClass().getSimpleName()+"."+testName+"\n\n");
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

  private static void setUpInVM(final VM vm, final String testNameToUse, final String diskStoreNameToUse) {
    vm.invoke(new SerializableRunnable() {
      private static final long serialVersionUID = 1L;

      @Override
      public void run() {
        setUpCreationStackGenerator();
        setTestName(testNameToUse);
        System.setProperty(HoplogConfig.ALLOW_LOCAL_HDFS_PROP, "true");    
        GemFireCacheImpl.setDefaultDiskStoreName(diskStoreNameToUse); // TODO: not thread safe
      }
    });
  }
  
  //---------------------------------------------------------------------------
  // tearDown methods
  //---------------------------------------------------------------------------
  
  /**
   * For logPerTest to work, we have to disconnect from the DS, but all
   * subclasses do not call super.tearDown(). To prevent this scenario
   * this method has been declared final. Subclasses must now override
   * {@link #tearDownBeforeDisconnect()} instead.
   * @throws Exception
   */
  @Override
  public final void tearDown() throws Exception {
    tearDownBefore();
    tearDownBeforeDisconnect();
    realTearDown();
    tearDownAfter();

    tearDownCreationStackGenerator();
    setTestName(null);

    tearDownInEveryVM();
  }

  /**
   * <code>tearDownBeforeDisconnect()</code> is invoked before {@link #realTearDown()}.<p/>
   * 
   * Override this in CacheTest to closeCache and destroyRegions<p/>
   * 
   * @see #tearDown
   * @see #tearDownAfter()
   */
  public void tearDownBeforeDisconnect() throws Exception {
  }

  protected void realTearDown() throws Exception {
    if (logPerTest) {
      disconnectFromDS();
      Invoke.invokeInEveryVM(DistributedTestCase.class, "disconnectFromDS");
    }
    cleanupAllVms();
  }
  
  /**
   * <code>tearDownBefore()</code> is invoked before {@link #realTearDown()}.<p/>
   * 
   * @see #tearDown
   * @see #tearDownAfter()
   */
  protected void tearDownBefore() throws Exception {
  }

  /**
   * <code>tearDownAfter()</code> is invoked after {@link #realTearDown()}.<p/>
   * @see #tearDown()
   * @see #tearDownBefore()
   */
  protected void tearDownAfter() throws Exception {
  }

  private static void tearDownInEveryVM() {
    Invoke.invokeInEveryVM(new SerializableRunnable() {
      private static final long serialVersionUID = 1L;

      @Override
      public void run() {    
        tearDownCreationStackGenerator();
        setTestName(null);
      }
    });
  }
  
  //---------------------------------------------------------------------------
  // test name methods
  //---------------------------------------------------------------------------

  public final String getMethodName() {
    return getName();
  }
  
  /**
   * Returns a unique name for this test method.  It is based on the
   * name of the class as well as the name of the method.
   */
  public final String getUniqueName() { // TODO: consider using FQCN
    return getClass().getSimpleName() + "_" + getTestName();
  }

  public static String getTestName() {
    return testName;
  }

  public static void setTestName(String name) {
    previousTestName = testName;
    testName = name;
  }
  
  //---------------------------------------------------------------------------
  // public final methods
  //---------------------------------------------------------------------------
  
  /**
   * Returns this VM's connection to the distributed system.  If
   * necessary, the connection will be lazily created using the given
   * <code>Properties</code>.  Note that this method uses hydra's
   * configuration to determine the location of log files, etc.
   * Note: "final" was removed so that WANTestBase can override this method.
   * This was part of the xd offheap merge.
   *
   * @see hydra.DistributedConnectionMgr#connect
   * @since 3.0
   */
  public /*final*/ InternalDistributedSystem getSystem(Properties props) { // TODO: make final and have subclasses override getProperties
    // Setting the default disk store name is now done in setUp
    if (system == null) {
      system = InternalDistributedSystem.getAnyInstance();
    }
    if (system == null || !system.isConnected()) {
      // Figure out our distributed system properties
      Properties p = DUnitEnv.getAllDistributedSystemProperties(props);
      lastSystemCreatedInTest = getClass();
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
      if(!getClass().equals(lastSystemCreatedInTest)) {
        Properties newProps = DUnitEnv.getAllDistributedSystemProperties(props);
        needNewSystem = !newProps.equals(lastSystemProperties);
        if(needNewSystem) {
          getLogWriter().info(
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
            getLogWriter().info("Forcing DS disconnect. For property " + key
                                + " old value = " + activeProps.getProperty(key)
                                + " new value = " + value);
            break;
          }
        }
      }
      if(needNewSystem) {
        // the current system does not meet our needs to disconnect and
        // call recursively to get a new system.
        getLogWriter().info("Disconnecting from current DS in order to make a new one");
        disconnectFromDS();
        getSystem(props);
      }
    }
    return system;
  }

  /**
   * Crash the cache in the given VM in such a way that it immediately stops communicating with
   * peers.  This forces the VM's membership manager to throw a ForcedDisconnectException by
   * forcibly terminating the JGroups protocol stack with a fake EXIT event.<p>
   * 
   * NOTE: if you use this method be sure that you clean up the VM before the end of your
   * test with disconnectFromDS() or disconnectAllFromDS().
   */
  public boolean crashDistributedSystem(VM vm) {
    return vm.invoke(new SerializableCallable<Boolean>("crash distributed system") {
      public Boolean call() throws Exception {
        DistributedSystem msys = InternalDistributedSystem.getAnyInstance();
        crashDistributedSystem(msys);
        return true;
      }
    });
  }
  
  /**
   * Crash the cache in the given VM in such a way that it immediately stops communicating with
   * peers.  This forces the VM's membership manager to throw a ForcedDisconnectException by
   * forcibly terminating the JGroups protocol stack with a fake EXIT event.<p>
   * 
   * NOTE: if you use this method be sure that you clean up the VM before the end of your
   * test with disconnectFromDS() or disconnectAllFromDS().
   */
  public final void crashDistributedSystem(final DistributedSystem msys) {
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
   * Returns a loner distributed system that isn't connected to other vms
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
  public final InternalDistributedSystem getLonerSystemWithEnforceUniqueHost() { // TODO: delete
    Properties props = this.getDistributedSystemProperties();
    props.put(DistributionConfig.MCAST_PORT_NAME, "0");
    props.put(DistributionConfig.LOCATORS_NAME, "");
    props.put(DistributionConfig.ENFORCE_UNIQUE_HOST_NAME, "true");
    props.put(DistributionConfig.REDUNDANCY_ZONE_NAME, "zone1");
    return getSystem(props);
  }

  /**
   * Returns whether or this VM is connected to a {@link com.gemstone.gemfire.distributed.DistributedSystem}.
   */
  public final boolean isConnectedToDS() {
    return system != null && system.isConnected();
  }

  //---------------------------------------------------------------------------
  // public methods
  //---------------------------------------------------------------------------
  
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

  //---------------------------------------------------------------------------
  // move elsewhere
  //---------------------------------------------------------------------------

  /** 
   * delete locator state files.  Use this after getting a random port
   * to ensure that an old locator state file isn't picked up by the
   * new locator you're starting.
   * @param ports
   */
  public void deleteLocatorStateFile(int... ports) { // TODO: tests should define this in custom tearDownAfter or in a locator specific subclass
    for (int i=0; i<ports.length; i++) {
      File stateFile = new File("locator"+ports[i]+"view.dat");
      if (stateFile.exists()) {
        stateFile.delete();
      }
    }
  }

  private String getDefaultDiskStoreName() { // TODO: move to CacheTestCase or somewhere else?
    String vmid = System.getProperty("vmid");
    return "DiskStore-"  + vmid + "-"+ getClass().getCanonicalName() + "." + getTestName();
  }

  //---------------------------------------------------------------------------
  // deprecated static methods
  //---------------------------------------------------------------------------
  
  /**
   * Returns a <code>LogWriter</code> for logging information
   * @deprecated Use a static logger from the log4j2 LogService.getLogger instead.
   */
  @Deprecated
  public static InternalLogWriter getLogWriter() {
    return oldLogger;
  }

  //---------------------------------------------------------------------------
  // private static methods
  //---------------------------------------------------------------------------

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
  
  //---------------------------------------------------------------------------
  // cleanup methods
  //---------------------------------------------------------------------------
  
  public static void cleanupAllVms() {
    cleanupThisVM();
    Invoke.invokeInEveryVM(DistributedTestCase.class, "cleanupThisVM");
    Invoke.invokeInLocator(new SerializableRunnable() {
      public void run() {
        DistributionMessageObserver.setInstance(null);
        unregisterInstantiatorsInThisVM();
      }
    });
    DUnitLauncher.closeAndCheckForSuspects();
  }

  private static void closeCache() { // TODO: move to CacheTestCase
    GemFireCacheImpl cache = GemFireCacheImpl.getInstance();
    if(cache != null && !cache.isClosed()) {
      destroyRegions(cache);
      cache.close();
    }
  }
  
  protected static final void destroyRegions(Cache cache) { // TODO: move to CacheTestCase
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
          getLogWriter().error(t);
        }
      }
    }
  }
  
  public static void unregisterAllDataSerializersFromAllVms() {
    unregisterDataSerializerInThisVM();
    Invoke.invokeInEveryVM(new SerializableRunnable() {
      public void run() {
        unregisterDataSerializerInThisVM();
      }
    });
    Invoke.invokeInLocator(new SerializableRunnable() {
      public void run() {
        unregisterDataSerializerInThisVM();
      }
    });
  }

  public static void unregisterInstantiatorsInThisVM() {
    // unregister all the instantiators
    InternalInstantiator.reinitialize();
    assertEquals(0, InternalInstantiator.getInstantiators().length);
  }
  
  public static void unregisterDataSerializerInThisVM() {
    DataSerializerPropogationDUnitTest.successfullyLoadedTestDataSerializer = false;
    // unregister all the Dataserializers
    InternalDataSerializer.reinitialize();
    // ensure that all are unregistered
    assertEquals(0, InternalDataSerializer.getSerializers().length);
  }

  /**
   * Disconnects this VM from the distributed system
   */
  public static void disconnectFromDS() {
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
      }
      catch (Exception e) {
        // ignore
      }
    }
    
    AdminDistributedSystemImpl ads = AdminDistributedSystemImpl.getConnectedInstance();
    if (ads != null) {// && ads.isConnected()) {
      ads.disconnect();
    }
  }

  /**
   * Disconnects all VMs including the local VM from the distributed system
   */
  public static void disconnectAllFromDS() {
    disconnectFromDS();
    Invoke.invokeInEveryVM(DistributedTestCase.class, "disconnectFromDS");
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
    unregisterInstantiatorsInThisVM();
    DistributionMessageObserver.setInstance(null);
    QueryObserverHolder.reset();
    DiskStoreObserver.setInstance(null);
    System.getProperties().remove("gemfire.log-level");
    System.getProperties().remove("jgroups.resolve_dns");
    
    if (InternalDistributedSystem.systemAttemptingReconnect != null) {
      InternalDistributedSystem.systemAttemptingReconnect.stopReconnecting();
    }
    
    IgnoredException.removeAllIgnoredExceptions();
  }
}
