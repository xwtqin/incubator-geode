package com.gemstone.gemfire.management.internal.security;



import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMX;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

import junit.framework.TestCase;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEvent;
import com.gemstone.gemfire.cache.asyncqueue.AsyncEventListener;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.cache.server.CacheServer;
import com.gemstone.gemfire.distributed.DistributedLockService;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.internal.AvailablePortHelper;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.management.AsyncEventQueueMXBean;
import com.gemstone.gemfire.management.CacheServerMXBean;
import com.gemstone.gemfire.management.DiskStoreMXBean;
import com.gemstone.gemfire.management.DistributedLockServiceMXBean;
import com.gemstone.gemfire.management.DistributedRegionMXBean;
import com.gemstone.gemfire.management.DistributedSystemMXBean;
import com.gemstone.gemfire.management.LockServiceMXBean;
import com.gemstone.gemfire.management.ManagerMXBean;
import com.gemstone.gemfire.management.MemberMXBean;
import com.gemstone.gemfire.management.RegionMXBean;
import com.gemstone.gemfire.management.internal.MBeanJMXAdapter;
import com.gemstone.gemfire.management.internal.security.ResourceOperationContext.ResourceOperationCode;
import com.gemstone.gemfire.test.junit.categories.IntegrationTest;
import com.gemstone.gemfire.test.junit.categories.UnitTest;

/**
 * Test all mbean operations by granting and revoking the access levels required
 * for performing that operation. Does not test wan mbeans 
 * 
 * @author tushark
 * 
 */
@Category(UnitTest.class)
public class MBeanSecurityJUnitTest extends TestCase {

  private static Logger logger = LogService.getLogger();
  private static final String USER = "custom";
  private static final String PASSWORD = "password123";
  private JMXConnector connector = null;
  
  
  public static class MyAsyncEventListener implements AsyncEventListener {
    @Override
    public void close() {
     
      
    }

    @Override
    public boolean processEvents(List<AsyncEvent> events) {
      return false;
    }
    
  }

  public void testGemfireMBeans() throws IOException, InstanceNotFoundException, MBeanException, ReflectionException,
      AttributeNotFoundException, InvalidAttributeValueException, MalformedObjectNameException {    
    GemFireCacheImpl cache = null;
    DistributedSystem ds = null;
    Properties pr = new Properties();
    pr.put("name", "testGemfireMBeans");
    pr.put(DistributionConfig.JMX_MANAGER_NAME, "true");
    pr.put(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    int port = AvailablePortHelper.getRandomAvailableTCPPort();
    pr.put(DistributionConfig.JMX_MANAGER_PORT_NAME, String.valueOf(port));
    pr.put(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");

    pr.put(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME,
        "com.gemstone.gemfire.management.internal.security.TestAuthenticator.create");
    pr.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME,
        "com.gemstone.gemfire.management.internal.security.TestAccessControl.create");
    pr.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_PP_NAME,
        "com.gemstone.gemfire.management.internal.security.TestAccessControl.create");
    ds = DistributedSystem.connect(pr);
    cache = (GemFireCacheImpl) CacheFactory.create(ds);
    
    DistributedLockService.create("mydsLock", ds);

    TestAuthenticator.addUser(USER, PASSWORD);
    TestAccessControl.grantResourceOp("custom", ResourceOperationCode.DATA_READ);
    TestAccessControl.grantResourceOp("custom", ResourceOperationCode.CREATE_REGION);
    TestAccessControl.grantCacheOp("custom", OperationCode.REGION_CREATE);
    TestAccessControl.grantResourceOp("custom", ResourceOperationCode.CREATE_DISKSTORE);
    TestAccessControl.grantResourceOp("custom", ResourceOperationCode.CREATE_AEQ);

    int cacheServerPort = AvailablePortHelper.getRandomAvailableTCPPort();
    CacheServer cacheServer = cache.addCacheServer();
    cacheServer.setPort(cacheServerPort);
    cacheServer.start();
    
    connector = getGemfireMBeanServer(port, USER, PASSWORD);
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    ObjectName memberON = (ObjectName) conn.invoke(MBeanJMXAdapter.getDistributedSystemName(), "fetchMemberObjectName",
        new Object[] { "testGemfireMBeans" }, new String[] { String.class.getCanonicalName() });

    MemberMXBean member = (MemberMXBean) JMX.newMBeanProxy(conn, memberON, MemberMXBean.class);

    File diskDir = new File("mydiskstore");
    diskDir.mkdir();
    assertTrue(diskDir.exists());
    String result = member.processCommand("create disk-store --name=mydiskstore --dir=" + diskDir.getAbsolutePath());
    assertNotNull(result);
    result = member.processCommand("create region --name=region1 --type=REPLICATE");
    assertNotNull(result);    
    result = member.processCommand(
        "create async-event-queue --id=aeq1 --listener=\"com.gemstone.gemfire.management.internal.security.MBeanSecurityJUnitTest$MyAsyncEventListener\"");
    assertNotNull(result);

    TestAccessControl.revokeResourceOp("custom", ResourceOperationCode.CREATE_REGION);
    TestAccessControl.revokeCacheOp("custom", OperationCode.REGION_CREATE);
    TestAccessControl.revokeResourceOp("custom", ResourceOperationCode.CREATE_DISKSTORE);
    TestAccessControl.revokeResourceOp("custom", ResourceOperationCode.CREATE_AEQ);

    doTestDistributedSystemMXBean(port);
    doTestMemberMXBean(port);

    doTestDiskStoreMXBean(port);
    doTestCacheServerMXBean(port, cacheServerPort);
    doTestLockServiceMXBean(port);
    doTestManagerMXBean(port);
    doTestRegionMXBean(port);    
    doTestDistributedLockServiceMXBean(port);
    doTestDistributedRegionMXBean(port);
    doTestAsyncEventQueueMXBean(port);
    doTestAccessControlMXBean(port);

    cache.close();
    ds.disconnect();
  }

  private void doTestAccessControlMXBean(int port) throws MalformedObjectNameException, IOException {    
    ObjectName accessControlON = new ObjectName(ResourceConstants.OBJECT_NAME_ACCESSCONTROL);
    checkMethod(port, AccessControlMXBean.class, accessControlON, "authorize", 
        new Object[] { ResourceOperationCode.DATA_READ.toString() },
        ResourceOperationCode.LIST_DS);
  }

  private void doTestAsyncEventQueueMXBean(int port) throws InstanceNotFoundException, ReflectionException, IOException {
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    ObjectName aeqON = MBeanJMXAdapter.getAsycnEventQueueMBeanName("testGemfireMBeans", "aeq1");
    
    checkAttributes(port, AsyncEventQueueMXBean.class, aeqON, new String[] { "BatchSize", "BatchTimeInterval",
        "BatchConflationEnabled", "Persistent", "Primary", "DispatcherThreads", "OrderPolicy", "DiskSynchronous",
        "Parallel", "AsyncEventListener", "EventQueueSize" });
    
  }

  private void doTestDistributedRegionMXBean(int port) throws IOException, InstanceNotFoundException, MBeanException,
      ReflectionException {
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    ObjectName regionON = (ObjectName) conn.invoke(MBeanJMXAdapter.getDistributedSystemName(),
        "fetchDistributedRegionObjectName", new Object[] { "/region1" },
        new String[] { String.class.getCanonicalName() });

    checkAttributes(port, DistributedRegionMXBean.class, regionON, new String[] { "Name", "RegionType", "FullPath",
        "LastModifiedTime", "PutsRate", "GatewayEnabled", "PersistentEnabled" });

    checkMethod(port, DistributedRegionMXBean.class, regionON, "listSubRegionPaths", new Object[] { true },
        ResourceOperationCode.LIST_DS);

    checkMethod(port, DistributedRegionMXBean.class, regionON, "listRegionAttributes", null,
        ResourceOperationCode.LIST_DS);

    checkMethod(port, DistributedRegionMXBean.class, regionON, "listMembershipAttributes", null,
        ResourceOperationCode.LIST_DS);

  }

  private void doTestDistributedLockServiceMXBean(int port) throws IOException, InstanceNotFoundException,
      MBeanException, ReflectionException {
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    ObjectName mydsLockON = (ObjectName) conn.invoke(MBeanJMXAdapter.getDistributedSystemName(),
        "fetchDistributedLockServiceObjectName", new Object[] { "mydsLock" },
        new String[] { String.class.getCanonicalName() });

    checkAttributes(port, DistributedLockServiceMXBean.class, mydsLockON, new String[] { "Name", "MemberCount",
        "MemberNames" });

    checkMethod(port, DistributedLockServiceMXBean.class, mydsLockON, "fetchGrantorMember", null,
        ResourceOperationCode.LIST_DS);

    checkMethod(port, DistributedLockServiceMXBean.class, mydsLockON, "listHeldLocks", null,
        ResourceOperationCode.LIST_DS);

    checkMethod(port, DistributedLockServiceMXBean.class, mydsLockON, "listThreadsHoldingLock", null,
        ResourceOperationCode.LIST_DS);

  }

  /* Has issues while starting locator hence commented out
   private void doTestLocatorMXBean(int port) {

    MBeanServerConnection conn = connector.getMBeanServerConnection();
    ObjectName regionON = (ObjectName) conn.invoke(MBeanJMXAdapter.getDistributedSystemName(), "fetchRegionObjectName",
        new Object[] { "testGemfireMBeans", "/region1" },
        new String[] { String.class.getCanonicalName(), String.class.getCanonicalName() });
        
      checkAttributes(port, LocatorMXBean.class, locatorON, new String[] { "Port",
        "BindAddress",
        "HostnameForClients",
        "PeerLocator",
        "ServerLocator"});   
        
      checkMethod(port, LocatorMXBean.class, locatorON, "viewLog", null,
        ResourceOperationCode.LIST_DS);
        
      checkMethod(port, LocatorMXBean.class, locatorON, "listPotentialManagers", null,
        ResourceOperationCode.LIST_DS); 
        
      checkMethod(port, LocatorMXBean.class, locatorON, "listManagers", null,
        ResourceOperationCode.LIST_DS);                            
        
    
  }*/

  private void doTestRegionMXBean(int port) throws IOException, InstanceNotFoundException, MBeanException,
      ReflectionException {
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    ObjectName regionON = (ObjectName) conn.invoke(MBeanJMXAdapter.getDistributedSystemName(), "fetchRegionObjectName",
        new Object[] { "testGemfireMBeans", "/region1" },
        new String[] { String.class.getCanonicalName(), String.class.getCanonicalName() });

    checkAttributes(port, RegionMXBean.class, regionON, new String[] { "Name", "RegionType", "FullPath",
        "LastModifiedTime", "PutsRate", "GatewayEnabled", "PersistentEnabled" });

    checkMethod(port, RegionMXBean.class, regionON, "listSubregionPaths", new Object[] { true },
        ResourceOperationCode.LIST_DS);

    checkMethod(port, RegionMXBean.class, regionON, "listRegionAttributes", null, ResourceOperationCode.LIST_DS);

    checkMethod(port, RegionMXBean.class, regionON, "listMembershipAttributes", null, ResourceOperationCode.LIST_DS);

  }

  private void doTestManagerMXBean(int port) throws IOException, InstanceNotFoundException, MBeanException,
      ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {

    ObjectName managerON = MBeanJMXAdapter.getManagerName();

    checkAttributes(port, ManagerMXBean.class, managerON, new String[] { "Running", "StatusMessage" });

    checkSetAttribute(port, managerON, "StatusMessage", "StatusMessage", ResourceOperationCode.LIST_DS);
    
    checkSetAttribute(port, managerON, "PulseURL", "PulseURL", ResourceOperationCode.LIST_DS);

    checkMethod(port, ManagerMXBean.class, managerON, "start", null, ResourceOperationCode.START_MANAGER);

    /*-
     * checkMethod(port, LockServiceMXBean.class, managerON, "stop", null,
     * ResourceOperationCode.STOP_MANAGER);
     */
  }

  private void doTestLockServiceMXBean(int port) throws IOException, InstanceNotFoundException, MBeanException,
      ReflectionException {
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    ObjectName mydsLockON = (ObjectName) conn.invoke(MBeanJMXAdapter.getDistributedSystemName(),
        "fetchLockServiceObjectName", new Object[] { "testGemfireMBeans", "mydsLock" },
        new String[] { String.class.getCanonicalName(), String.class.getCanonicalName() });
    checkAttributes(port, LockServiceMXBean.class, mydsLockON, new String[]{"Name", "Distributed",
      "MemberCount", "MemberNames", "LockGrantor"});
    
    checkMethod(port, LockServiceMXBean.class, mydsLockON, "listThreadsHoldingLock", null,
        ResourceOperationCode.LIST_DS);
    
    checkMethod(port, LockServiceMXBean.class, mydsLockON, "listHeldLocks", null,
        ResourceOperationCode.LIST_DS);
    
    checkMethod(port, LockServiceMXBean.class, mydsLockON, "fetchGrantorMember", null,
        ResourceOperationCode.LIST_DS);
    
    checkMethod(port, LockServiceMXBean.class, mydsLockON, "becomeLockGrantor", null,
        ResourceOperationCode.BECOME_LOCK_GRANTOR);    
  }

  private void doTestGatewaySenderMXBean(int port) {

  }

  private void doTestGatewayReceiverMXBean(int port) {

  }

  private void doTestCacheServerMXBean(int port, int cacheServerPort) throws IOException, InstanceNotFoundException,
      MBeanException, ReflectionException {
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    ObjectName cacheServerON = (ObjectName) conn.invoke(MBeanJMXAdapter.getDistributedSystemName(),
        "fetchCacheServerObjectName", new Object[] { "testGemfireMBeans", cacheServerPort }, new String[] {
            String.class.getCanonicalName(), int.class.getName() });
    
    checkAttributes(port, CacheServerMXBean.class, cacheServerON, new String[] { "Port", "BindAddress",
        "MaxConnections", "Running" });
    
    checkMethod(port, CacheServerMXBean.class, cacheServerON, "showAllClientStats", null, ResourceOperationCode.LIST_DS);
    
    checkMethod(port, CacheServerMXBean.class, cacheServerON, "removeIndex", new Object[]{"indexName"},
        ResourceOperationCode.DESTROY_INDEX);
    
    checkMethod(port, CacheServerMXBean.class, cacheServerON, "executeContinuousQuery", new Object[]{"queryName"},
        ResourceOperationCode.QUERY);
    
    checkMethod(port, CacheServerMXBean.class, cacheServerON, "stopContinuousQuery", new Object[]{"queryName"},
        ResourceOperationCode.STOP_CONTINUOUS_QUERY);
  }

  private void doTestDiskStoreMXBean(int port) throws IOException, InstanceNotFoundException, MBeanException,
      ReflectionException, AttributeNotFoundException, InvalidAttributeValueException {
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    ObjectName diskStoreON = (ObjectName) conn.invoke(MBeanJMXAdapter.getDistributedSystemName(),
        "fetchDiskStoreObjectName", new Object[] { "testGemfireMBeans", "mydiskstore" },
        new String[] { String.class.getCanonicalName(), String.class.getCanonicalName() });

    checkMethod(port, DiskStoreMXBean.class, diskStoreON, "flush", null, ResourceOperationCode.FLUSH_DISKSTORE);

    checkMethod(port, DiskStoreMXBean.class, diskStoreON, "forceRoll", null, ResourceOperationCode.FORCE_ROLL);

    checkMethod(port, DiskStoreMXBean.class, diskStoreON, "forceCompaction", null,
        ResourceOperationCode.FORCE_COMPACTION);

    checkMethod(port, DiskStoreMXBean.class, diskStoreON, "isAutoCompact", null, ResourceOperationCode.LIST_DS);

    checkSetAttribute(port, diskStoreON, "DiskUsageWarningPercentage", 0.78f,
        ResourceOperationCode.SET_DISK_USAGE);

    checkAttributes(port, DiskStoreMXBean.class, diskStoreON, new String[] { "MaxOpLogSize", "TimeInterval",
        "WriteBufferSize", "DiskDirectories" , "AutoCompact", "CompactionThreshold" });
  }

  private void doTestMemberMXBean(int port) throws IOException, InstanceNotFoundException, MBeanException,
      ReflectionException {
    ObjectName distrSysON = MBeanJMXAdapter.getDistributedSystemName();
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    ObjectName memberON = (ObjectName) conn.invoke(MBeanJMXAdapter.getDistributedSystemName(), "fetchMemberObjectName",
        new Object[] { "testGemfireMBeans" }, new String[] { String.class.getCanonicalName() });

    checkMethod(port, MemberMXBean.class, memberON, "showLog", new Object[] { 10 }, ResourceOperationCode.SHOW_LOG);

    checkMethod(port, MemberMXBean.class, memberON, "viewLicense", null, ResourceOperationCode.LIST_DS);

    checkMethod(port, MemberMXBean.class, memberON, "compactAllDiskStores", null,
        ResourceOperationCode.COMPACT_DISKSTORE);

    checkMethod(port, MemberMXBean.class, memberON, "showJVMMetrics", null, ResourceOperationCode.LIST_DS);

    checkMethod(port, MemberMXBean.class, memberON, "showOSMetrics", null, ResourceOperationCode.LIST_DS);

    checkMethod(port, MemberMXBean.class, memberON, "listDiskStores", new Object[] { true },
        ResourceOperationCode.LIST_DS);

    checkMethod(port, MemberMXBean.class, memberON, "listGemFireProperties", null, ResourceOperationCode.LIST_DS);

    checkMethod(port, MemberMXBean.class, memberON, "status", null, ResourceOperationCode.LIST_DS);
  }

  private void doTestDistributedSystemMXBean(int port) throws IOException {
    ObjectName distrSysON = MBeanJMXAdapter.getDistributedSystemName();
    checkMethod(port, DistributedSystemMXBean.class, distrSysON, "listMembers", null, ResourceOperationCode.LIST_DS);

    checkMethod(port, DistributedSystemMXBean.class, distrSysON, "changeAlertLevel", new Object[] { "error" },
        ResourceOperationCode.CHANGE_ALERT_LEVEL);

    checkMethod(port, DistributedSystemMXBean.class, distrSysON, "backupAllMembers", new Object[] { "error", "error" },
        ResourceOperationCode.BACKUP_MEMBERS);

    checkMethod(port, DistributedSystemMXBean.class, distrSysON, "showJVMMetrics", new Object[] { "memberName" },
        ResourceOperationCode.LIST_DS);

    checkMethod(port, DistributedSystemMXBean.class, distrSysON, "revokeMissingDiskStores",
        new Object[] { "diskStoreId" }, ResourceOperationCode.REVOKE_MISSING_DISKSTORE);

    checkMethod(port, DistributedSystemMXBean.class, distrSysON, "queryData", new Object[] { "queryString", "mmebers",
        10 }, ResourceOperationCode.QUERY);

    checkMethod(port, DistributedSystemMXBean.class, distrSysON, "setQueryResultSetLimit", new Object[] { 10 },
        ResourceOperationCode.QUERY);

    checkMethod(port, DistributedSystemMXBean.class, distrSysON, "setQueryCollectionsDepth", new Object[] { 10 },
        ResourceOperationCode.QUERY);
  }

  private void checkMethod(int port, Class<?> mxBean, ObjectName mbeanObjectName, String methodName, Object[] args,
      ResourceOperationCode opCode) throws IOException {
    if (connector == null)
      connector = getGemfireMBeanServer(port, USER, PASSWORD);
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    Object proxy = JMX.newMXBeanProxy(conn, mbeanObjectName, mxBean);
    if (ResourceOperationCode.LIST_DS.equals(opCode)) {
      testObject(proxy, methodName, args, false);
    } else {
      if (TestAccessControl.hasAccessToResourceOp(USER, opCode)) {
        boolean removed = TestAccessControl.revokeResourceOp(USER, opCode);
        if (!removed)
          fail("Fail to removed opCode " + opCode);
      }
      testObject(proxy, methodName, args, true);
      TestAccessControl.grantResourceOp(USER, opCode);
      logger.info("Grant opCode " + opCode);
      testObject(proxy, methodName, args, false);
      boolean removed = TestAccessControl.revokeResourceOp(USER, opCode);
      if (!removed)
        fail("Fail to removed opCode " + opCode);
      else
        logger.info("Revoke opCode " + opCode);

    }
  }

  private void checkAttributes(int port, Class<?> mxBean, ObjectName mbeanObjectName, String[] attrs)
      throws IOException, InstanceNotFoundException, ReflectionException {
    if (connector == null)
      connector = getGemfireMBeanServer(port, USER, PASSWORD);
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    try {
      AttributeList list = conn.getAttributes(mbeanObjectName, attrs);
      assertNotNull(list);
      assertEquals(list.size(), attrs.length);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception " + e.getMessage());
    } finally {

    }
    
    //Try individual attribute to test getAttribute hook 
    try {
      for (String attr : attrs) {
        conn.getAttribute(mbeanObjectName, attr);
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception " + e.getMessage());
    } finally {

    }
    
  }

  private void checkSetAttribute(int port, ObjectName mbeanObjectName, String attr, Object value,
      ResourceOperationCode opcode) throws IOException, InstanceNotFoundException, AttributeNotFoundException,
      InvalidAttributeValueException, MBeanException, ReflectionException {
    if (connector == null)
      connector = getGemfireMBeanServer(port, USER, PASSWORD);
    MBeanServerConnection conn = connector.getMBeanServerConnection();
    try {
      Attribute attribute = new Attribute(attr, value);
      conn.setAttribute(mbeanObjectName, attribute);
      if (!opcode.equals(ResourceOperationCode.LIST_DS))
        fail("SetAttribute suceeded without Access to " + opcode);
    } catch (SecurityException e) {
      // expected
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected Exception " + e.getMessage());
    }

    if (!opcode.equals(ResourceOperationCode.LIST_DS)) {
      try {
        TestAccessControl.grantResourceOp(USER, opcode);
        Attribute attribute = new Attribute(attr, value);
        conn.setAttribute(mbeanObjectName, attribute);
      } catch (SecurityException e) {
        e.printStackTrace();
        fail("Unexpected SecurityException " + e.getMessage());
      } catch (Exception e) {
        e.printStackTrace();
        fail("Unexpected Exception " + e.getMessage());
      } finally {
        TestAccessControl.revokeResourceOp(USER, opcode);
      }
    }

  }

  private void testObject(Object proxy, String methodName, Object[] args, boolean securityExceptionExpected) {
    Method mt = null;
    for (Method mts : proxy.getClass().getMethods()) {
      if (mts.getName().equals(methodName)) {
        mt = mts;
      }
    }
    try {
      logger.info("Invoking method " + methodName);
      mt.invoke(proxy, args);
      if (securityExceptionExpected)
        fail("Expected call to method " + methodName + " was expected to fail with security exception");
      logger.info("Successfully Invoked method " + methodName);
    } catch (IllegalAccessException e) {
      error("UnExpected error ", e);
      fail(e.getMessage());
    } catch (IllegalArgumentException e) {
      error("UnExpected error ", e);
      fail(e.getMessage());
    } catch (InvocationTargetException e) {
      Throwable t = e.getCause();
      if (t instanceof SecurityException) {
        if (!securityExceptionExpected) {
          fail("Did not expect call to method " + methodName + " to fail with security exception");
        }
      } else {
        // other errors are expected as wrong parameters are passed
        // error("UnExpected error ", e);
        // fail(e.getMessage());
      }
    } catch (SecurityException e) {
      if (!securityExceptionExpected) {
        fail("Did not expect call to method " + methodName + " to fail with security exception");
      }
    }
  }

  private void error(String string, Exception e) {
    System.out.println(string);
    e.printStackTrace();
  }

  private JMXConnector getGemfireMBeanServer(int port, String user, String pwd) {
    String[] creds = null;
    if (user != null)
      creds = new String[] { user, pwd };
    return _getGemfireMBeanServer(port, creds);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private JMXConnector _getGemfireMBeanServer(int port, Object creds) {
    JMXServiceURL url;
    try {
      url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi");
      if (creds != null) {
        Map env = new HashMap();
        env.put(JMXConnector.CREDENTIALS, creds);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
        return jmxc;
      } else {
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        return jmxc;
      }
    } catch (MalformedURLException e) {
      fail("Error connecting to port=" + port + " " + e.getMessage());
    } catch (IOException e) {
      fail("Error connecting to port=" + port + " " + e.getMessage());
    }
    return null;
  }

}
