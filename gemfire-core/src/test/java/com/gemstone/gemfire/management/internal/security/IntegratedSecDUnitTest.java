package com.gemstone.gemfire.management.internal.security;

import hydra.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.cache.server.CacheServer;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.internal.AvailablePortHelper;
import com.gemstone.gemfire.management.internal.MBeanJMXAdapter;
import com.gemstone.gemfire.management.internal.cli.domain.DataCommandRequest;
import com.gemstone.gemfire.management.internal.cli.result.CommandResult;
import com.gemstone.gemfire.management.internal.cli.result.CompositeResultData;
import com.gemstone.gemfire.management.internal.cli.result.CompositeResultData.SectionResultData;
import com.gemstone.gemfire.management.internal.cli.result.ErrorResultData;
import com.gemstone.gemfire.management.internal.cli.result.ResultData;
import com.gemstone.gemfire.management.internal.security.ResourceOperationContext.ResourceOperationCode;
import com.gemstone.gemfire.security.AuthInitialize;
import com.gemstone.gemfire.security.AuthenticationFailedException;

import dunit.Host;
import dunit.SerializableRunnable;
import dunit.VM;

/**
 * @author tushark
 * 
 */
public class IntegratedSecDUnitTest extends CommandTestBase {

  public static class AuthInitializer implements AuthInitialize {

    public static AuthInitialize create() {
      return new AuthInitializer();
    }

    public void init(LogWriter systemLogger, LogWriter securityLogger) throws AuthenticationFailedException {
    }

    public Properties getCredentials(Properties p, DistributedMember server, boolean isPeer)
        throws AuthenticationFailedException {
      return p;
    }

    public void close() {
    }
  }

  private static final long serialVersionUID = 1L;
  private static IntegratedSecDUnitTest instance = new IntegratedSecDUnitTest("IntegratedSecDUnitTest");

  private Cache cache;
  private DistributedSystem ds;
  private CacheServer cacheServer;
  private ClientCache clientCache;
  private int cacheServerPort;
  private String hostName;

  public IntegratedSecDUnitTest(String name) {
    super(name);
  }

  public Cache createCache(Properties props) throws Exception {
    ds = getSystem(props);
    cache = CacheFactory.create(ds);
    if (cache == null) {
      throw new Exception("CacheFactory.create() returned null ");
    }
    return cache;
  }

  private void createServer() throws IOException {
    cacheServerPort = AvailablePortHelper.getRandomAvailableTCPPort();
    cacheServer = cache.addCacheServer();
    cacheServer.setPort(cacheServerPort);
    cacheServer.start();
    hostName = cacheServer.getHostnameForClients();
  }

  public int getCacheServerPort() {
    return cacheServerPort;
  }

  public String getCacheServerHost() {
    return hostName;
  }

  public void stopCacheServer() {
    this.cacheServer.stop();
  }

  @SuppressWarnings("rawtypes")
  public void setUpServerVM(Properties gemFireProps) throws Exception {
    Log.getLogWriter().info("Creating server vm cache with props =" + gemFireProps);
    gemFireProps.setProperty(DistributionConfig.NAME_NAME, testName + "Server");
    createCache(gemFireProps);
    RegionFactory factory = cache.createRegionFactory(RegionShortcut.REPLICATE);
    Region r = factory.create("serverRegion");
    assertNotNull(r);
    r.put("serverkey", "servervalue");
    assertEquals(1,r.size());
    Log.getLogWriter().info("Created serverRegion with 1 key=serverKey");
  }

  public void setUpClientVM(Properties gemFireProps, String host, int port, String user, String password) {
    gemFireProps.setProperty(DistributionConfig.NAME_NAME, testName + "Client");
    //gemFireProps.setProperty("security-username", user);
    //gemFireProps.setProperty("security-password", password);
    gemFireProps.setProperty("security-client-auth-init",
        "com.gemstone.gemfire.management.internal.security.IntegratedSecDUnitTest$AuthInitializer.create");
    Log.getLogWriter().info("Creating client cache with props =" + gemFireProps);
    ClientCacheFactory clientCacheFactory = new ClientCacheFactory(gemFireProps);
    clientCacheFactory.addPoolServer(host, port);
    clientCacheFactory.setPoolMultiuserAuthentication(true);
    clientCache = clientCacheFactory.create();
    ClientRegionFactory<String, String> regionFactory = clientCache
        .createClientRegionFactory(ClientRegionShortcut.PROXY);
           
    Region<String, String> region = regionFactory.create("serverRegion");
    assertNotNull(region);    
    
    Properties properties = new Properties();
    properties.setProperty("security-username", user);
    properties.setProperty("security-password", password);
    RegionService regionService = instance.clientCache.createAuthenticatedView(properties);
    Region secRegion = regionService.getRegion("serverRegion");
    assertNotNull(secRegion.get("serverkey"));
  }

  public static void setUpServerVMTask(Properties props) throws Exception {
    instance.setUpServerVM(props);
  }

  public static void createServerTask() throws Exception {
    instance.createServer();
  }

  public static void setUpClientVMTask(Properties gemFireProps, String host, int port, String user, String password)
      throws Exception {
    instance.setUpClientVM(gemFireProps, host, port, user, password);
  }

  public static Object[] getCacheServerEndPointTask() {
    Object[] array = new Object[2];
    array[0] = instance.getCacheServerHost();
    array[1] = instance.getCacheServerPort();
    return array;
  }

  public static void closeCacheTask() {
    instance.cache.close();
  }

  public static void closeClientCacheTask() {
    instance.clientCache.close();
  }

  /**
   * 
   * VM0 -> Manager
   * VM1 -> Server
   * Vm2 -> CacheClient
   * 
   * @param testName
   * @throws IOException
   */

  @SuppressWarnings("serial")
  void setup(String testName) throws IOException {

    configureIntSecDescriptor();

    Properties props = new Properties();

    props.put(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME,
        "com.gemstone.gemfire.management.internal.security.TestAuthenticator.create");
    props.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME,
        "com.gemstone.gemfire.management.internal.security.TestAccessControl.create");
    props.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_PP_NAME,
        "com.gemstone.gemfire.management.internal.security.TestAccessControl.create");
    props.setProperty(DistributionConfig.NAME_NAME, "Manager");
    HeadlessGfsh gfsh = createDefaultSetup(props);
    assertNotNull(gfsh);
    assertEquals(true, gfsh.isConnectedAndReady());

    props.list(System.out);

    final Host host = Host.getHost(0);
    VM serverVM = host.getVM(1);
    VM clientVM = host.getVM(2);
    serverVM.invoke(IntegratedSecDUnitTest.class, "setUpServerVMTask", new Object[] { props });
    serverVM.invoke(IntegratedSecDUnitTest.class, "createServerTask");

    Object array[] = (Object[]) serverVM.invoke(IntegratedSecDUnitTest.class, "getCacheServerEndPointTask");
    String hostName = (String) array[0];
    int port = (Integer) array[1];
    Object params[] = new Object[] { props, hostName, port, "tushark", "password123" };
    Log.getLogWriter().info("Starting client with server endpoint " + hostName + ":" + port);
    clientVM.invoke(IntegratedSecDUnitTest.class, "setUpClientVMTask", params);
    
    Log.getLogWriter().info("Sleeping for 5 seconds to get all mbeans registered on manager");
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
  }

  @SuppressWarnings("serial")
  private void configureIntSecDescriptor() {
    this.userName = "tushark";
    this.password = "password123";

    final Host host = Host.getHost(0);
    final VM serverVM = host.getVM(1);
    final VM clientVM = host.getVM(2);
    final VM managerVM = host.getVM(0);
    SerializableRunnable grantOpsUser1Runnable = new SerializableRunnable() {
      @Override
      public void run() {
        TestAuthenticator.addUser("tushark", "password123");
        TestAuthenticator.addUser("dataRead", "password123");
        TestAuthenticator.addUser("dataWrite", "password123");
        TestAuthenticator.addUser("monitor", "password123");
        TestAuthenticator.addUser("admin", "password123");
        TestAuthenticator.addUser("custom", "password123");

        TestAccessControl.grantCacheOp("tushark", OperationCode.GET);
        TestAccessControl.grantCacheOp("tushark", OperationCode.PUT);
        TestAccessControl.grantCacheOp("tushark", OperationCode.DESTROY);
        TestAccessControl.grantCacheOp("tushark", OperationCode.REMOVEALL);
        TestAccessControl.grantCacheOp("tushark", OperationCode.EXECUTE_FUNCTION);
        TestAccessControl.grantCacheOp("tushark", OperationCode.QUERY);

        TestAccessControl.grantResourceOp("tushark", ResourceOperationCode.DATA_READ);
        TestAccessControl.grantResourceOp("tushark", ResourceOperationCode.DATA_WRITE);
        TestAccessControl.grantResourceOp("tushark", ResourceOperationCode.MONITOR);
        TestAccessControl.grantResourceOp("tushark", ResourceOperationCode.CHANGE_ALERT_LEVEL);

        TestAccessControl.grantCacheOp("dataRead", OperationCode.GET);
        TestAccessControl.grantResourceOp("dataRead", ResourceOperationCode.DATA_READ);

        TestAccessControl.grantCacheOp("dataWrite", OperationCode.GET);
        TestAccessControl.grantCacheOp("dataWrite", OperationCode.PUT);
        TestAccessControl.grantCacheOp("dataWrite", OperationCode.DESTROY);
        TestAccessControl.grantCacheOp("dataWrite", OperationCode.REGION_CREATE);
        TestAccessControl.grantCacheOp("dataWrite", OperationCode.REGION_DESTROY);
        TestAccessControl.grantResourceOp("dataWrite", ResourceOperationCode.DATA_WRITE);

        TestAccessControl.grantResourceOp("monitor", ResourceOperationCode.DATA_READ);
        TestAccessControl.grantResourceOp("monitor", ResourceOperationCode.MONITOR);

        TestAccessControl.grantResourceOp("admin", ResourceOperationCode.ADMIN);

        TestAccessControl.grantResourceOp("custom", ResourceOperationCode.DATA_READ);
        TestAccessControl.grantResourceOp("custom", ResourceOperationCode.SHOW_DEADLOCKS);        
        TestAccessControl.grantResourceOp("custom", ResourceOperationCode.CREATE_REGION);
        TestAccessControl.grantCacheOp("custom", OperationCode.REGION_CREATE);
      }
    };
    managerVM.invoke(grantOpsUser1Runnable);
    serverVM.invoke(grantOpsUser1Runnable);
  }

  @SuppressWarnings("serial")
  public static void grantCacheOp(final String user, final String code) {
    final Host host = Host.getHost(0);
    final VM serverVM = host.getVM(1);
    final VM managerVM = host.getVM(0);
    SerializableRunnable grantOpsUser1Runnable = new SerializableRunnable() {
      @Override
      public void run() {
        TestAccessControl.grantCacheOp(user, OperationCode.parse(code));
      }
    };
    managerVM.invoke(grantOpsUser1Runnable);
    serverVM.invoke(grantOpsUser1Runnable);
  }

  @SuppressWarnings("serial")
  public static void revokeCacheOp(final String user, final String code) {
    final Host host = Host.getHost(0);
    final VM serverVM = host.getVM(1);
    final VM managerVM = host.getVM(0);
    SerializableRunnable grantOpsUser1Runnable = new SerializableRunnable() {
      @Override
      public void run() {
        TestAccessControl.revokeCacheOp(user, OperationCode.parse(code));
      }
    };
    managerVM.invoke(grantOpsUser1Runnable);
    serverVM.invoke(grantOpsUser1Runnable);
  }

  @SuppressWarnings("serial")
  public static void grantResourceOp(final String user, final String code) {
    final Host host = Host.getHost(0);
    final VM serverVM = host.getVM(1);
    final VM managerVM = host.getVM(0);
    SerializableRunnable grantOpsUser1Runnable = new SerializableRunnable() {
      @Override
      public void run() {
        TestAccessControl.grantResourceOp(user, ResourceOperationCode.parse(code));
      }
    };
    managerVM.invoke(grantOpsUser1Runnable);
    serverVM.invoke(grantOpsUser1Runnable);
  }

  @SuppressWarnings("serial")
  public static void revokeResourceOp(final String user, final String code) {
    final Host host = Host.getHost(0);
    final VM serverVM = host.getVM(1);
    final VM managerVM = host.getVM(0);
    SerializableRunnable grantOpsUser1Runnable = new SerializableRunnable() {
      @Override
      public void run() {
        TestAccessControl.revokeResourceOp(user, ResourceOperationCode.parse(code));
      }
    };
    managerVM.invoke(grantOpsUser1Runnable);
    serverVM.invoke(grantOpsUser1Runnable);
  }

  
  public static void doPutUsingClientCache(final String regionPath, final String key, final String value,
      final boolean expectSuccess, String user, String password) {   
    try {
      Properties properties = new Properties();
      properties.setProperty("security-username", user);
      properties.setProperty("security-password", password);
      RegionService regionService = instance.clientCache.createAuthenticatedView(properties);
      Region region = regionService.getRegion(regionPath);
      assertNotNull(region);
      Object oldValue = region.put(key, value);
      Log.getLogWriter().info("doPutUsingClientCache : Put key=" + key + " for user="+ user+" newValue="+ value + " oldValue="+ oldValue + " expectSuccess="+expectSuccess);
      if (!expectSuccess)
        fail("Region Put was expected to fail");
    } catch (Exception e) {
      if (!expectSuccess) {
        Log.getLogWriter().info("expectSuccess=false => " + e.getMessage());
      } else {
        Log.getLogWriter().error("Unexpected error", e);
        fail("Unknown reason " + e.getMessage());
      }
    }
  }

  public void doPutUsingGfsh(final String regionPath, final String key, final String value,
      final boolean expectSuccess, String user, String password) {
    String command = "put --region=" + regionPath + " --key=" + key + " --value=" + value;
    changeGfshUser(user, password);
    CommandResult result = executeCommand(command);
    Log.getLogWriter().info("CommandResult " + result);
    if (expectSuccess) {
      validateGfshResult(result, expectSuccess);
      printCommandOutput(result);
    }
    else {
      Log.getLogWriter().info("Error line :" + this.commandError);
      assertTrue(this.commandError.contains("Access Denied"));
      this.commandError = null;
      // validateGfshResultError(result);
    }
  }

  private static void validateGfshResultError(CommandResult result) {
    if (result.getType().equals(ResultData.TYPE_ERROR)) {
      ErrorResultData data = (ErrorResultData) result.getResultData();
      Log.getLogWriter().info("Error resultData : " + data.toString());
    } else
      fail("Unexpected result type " + result.getType());
  }

  private static void validateGfshResult(CommandResult cmdResult, boolean expected) {
    if (ResultData.TYPE_COMPOSITE.equals(cmdResult.getType())) {
      CompositeResultData rd = (CompositeResultData) cmdResult.getResultData();
      SectionResultData section = rd.retrieveSectionByIndex(0);
      boolean result = (Boolean) section.retrieveObject("Result");
      assertEquals(expected, result);
    } else
      fail("Expected CompositeResult Returned Result Type " + cmdResult.getType());
  }

  public static void doGetUsingClientCache(final String regionPath, final String key, final boolean expectSuccess,
      String user, String password) {    
    try {
      Properties properties = new Properties();
      properties.setProperty("security-username", user);
      properties.setProperty("security-password", password);
      RegionService regionService = instance.clientCache.createAuthenticatedView(properties);
      Region region = regionService.getRegion(regionPath);
      assertNotNull(region);
      Object value = region.get(key);
      Log.getLogWriter().info("doGetUsingClientCache : Get key=" + key + " for user="+ user+" value="+ value + " expectSuccess="+expectSuccess);
      assertNotNull(value);
      if (!expectSuccess)
        fail("Region Get was expected to fail");
    } catch (Exception e) {
      if (!expectSuccess) {
        Log.getLogWriter().info("expectSuccess=true => " + e.getMessage());
      } else {
        Log.getLogWriter().error("Unexpected error", e);
        fail("Unknown reason " + e.getMessage());
      }
    }
  }
  
  public void doGetUsingGfsh(final String regionPath, final String key, final boolean expectSuccess, String user,
      String password) {
    String command = "get --region=" + regionPath + " --key=" + key;
    changeGfshUser(user, password);
    CommandResult result = executeCommand(command);    
    if (expectSuccess) {
      printCommandOutput(result);
      validateGfshResult(result, expectSuccess);      
    }
    else {
      Log.getLogWriter().info("Error line :" + this.commandError);
      assertTrue(this.commandError.contains("Access Denied"));
      this.commandError = null;
    }
  }

  private void changeGfshUser(String user, String password) {
    if (!this.userName.equals(user)) {
      executeCommand("disconnect");
      this.userName = user;
      this.password = password;
      defaultShellConnect();
    }
  }
  
  public void doCommandUsingGfsh(String command, final boolean expectSuccess, String user, String password) {
    changeGfshUser(user, password);
    CommandResult result = executeCommand(command);
    if (expectSuccess) {
      assertNotNull(result);
      printCommandOutput(result);
      //assertFalse(result.getType().equals(ResultData.TYPE_ERROR));
    }
    else {
      Log.getLogWriter().info("Error line :" + this.commandError);
      assertTrue(this.commandError.contains("Access Denied"));
      this.commandError = null;
    }
  }
  
  private static void printCommandOutput(CommandResult cmdResult) {
    assertNotNull(cmdResult);
    Log.getLogWriter().info("Command Output : ");
    StringBuilder sb = new StringBuilder();
    cmdResult.resetToFirstLine();
    while (cmdResult.hasNextLine()) {
      sb.append(cmdResult.nextLine()).append(DataCommandRequest.NEW_LINE);
    }
    Log.getLogWriter().info(sb.toString());
    Log.getLogWriter().info("");      
  }
  
  private void doShowLogUsingJMX(boolean expectSuccess, String user, String password) {
    Object[] endPoint = getJMXEndPoint();
    String[] creds = new String[] { user, password };
    try {
      JMXConnector connector = _getGemfireMBeanServer((Integer) endPoint[1], creds);
      MBeanServerConnection mbeanServer = connector.getMBeanServerConnection();
      ObjectName memberON = (ObjectName)mbeanServer.invoke(MBeanJMXAdapter.getDistributedSystemName(), "fetchMemberObjectName", 
          new Object[]{"Manager"}, new String[]{String.class.getCanonicalName()});      
      String logs = (String) mbeanServer.invoke(memberON, "showLog", new Object[]{60}, new String[]{int.class.toString()});
      Log.getLogWriter().info("JMX Output :" + logs);
      connector.close();
      if(!expectSuccess)
        fail("Expected Access Denied...");      
    } catch (InstanceNotFoundException e) {
      Log.getLogWriter().error("Unexpected Error", e);
      fail("Unexpected Error " + e.getMessage());
    } catch (MBeanException e) {
      Log.getLogWriter().error("Unexpected Error", e);
      fail("Unexpected Error " + e.getMessage());
    } catch (ReflectionException e) {
      Log.getLogWriter().error("Unexpected Error", e);
      fail("Unexpected Error " + e.getMessage());
    } catch (IOException e) {
      Log.getLogWriter().error("Unexpected Error", e);
      fail("Unexpected Error " + e.getMessage());
    } catch (SecurityException e) {
      if(expectSuccess){
        fail("Expected successful jmx execution");
      } else {
        //expected
      }
    }
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

  public void testDataCommandsFromDifferentClients() throws IOException {
    final Host host = Host.getHost(0);
    final VM clientVM = host.getVM(2);

    setup("testDataCommandsFromDifferentClients");
    executeCommand("list members");
    changeGfshUser("dataRead", "password123");
    executeCommand("list members");

    // check tushark can execute get/put/delete/execute function/query operation from cacheclient and through
    // data-commands
    String region = "serverRegion";
    clientVM.invoke(IntegratedSecDUnitTest.class, "doPutUsingClientCache", new Object[] { region, "myk1", "myv1", true,
        "tushark", "password123" });
    doGetUsingGfsh(region, "myk1", true, "tushark", "password123");
    doPutUsingGfsh(region, "myk2", "myv2", true, "tushark", "password123");
    clientVM.invoke(IntegratedSecDUnitTest.class, "doGetUsingClientCache", new Object[] { region, "myk2", true,
        "tushark", "password123" });
    revokeCacheOp("tushark", "PUT");
    clientVM.invoke(IntegratedSecDUnitTest.class, "doPutUsingClientCache", new Object[] { region, "myk1", "myv1",
        false, "tushark", "password123" });
    doPutUsingGfsh(region, "myk2", "myv2", false, "tushark", "password123");
    grantCacheOp("tushark", "PUT");
    clientVM.invoke(IntegratedSecDUnitTest.class, "doPutUsingClientCache", new Object[] { region, "myk1", "myv1", true,
        "tushark", "password123" });
    doPutUsingGfsh(region, "myk2", "myv2", true, "tushark", "password123");
    
    
    //dataRead Role
    clientVM.invoke(IntegratedSecDUnitTest.class, "doPutUsingClientCache", new Object[] { region, "myk1", "myv1", false,
      "dataRead", "password123" });
    doPutUsingGfsh(region, "myk2", "myv2", false, "dataRead", "password123");
    doGetUsingGfsh(region, "myk1", true, "dataRead", "password123");
    clientVM.invoke(IntegratedSecDUnitTest.class, "doGetUsingClientCache", new Object[] { region, "myk2", true,
      "dataRead", "password123" });    
    
    //dataWrite Role
    clientVM.invoke(IntegratedSecDUnitTest.class, "doPutUsingClientCache", new Object[] { region, "myk1", "myv1", true,
      "dataWrite", "password123" });
    doPutUsingGfsh(region, "myk2", "myv2", true, "dataWrite", "password123");
    doGetUsingGfsh(region, "myk1", true, "dataWrite", "password123");
    clientVM.invoke(IntegratedSecDUnitTest.class, "doGetUsingClientCache", new Object[] { region, "myk2", true,
      "dataWrite", "password123" });
    
    
    
    //admin and monitor and custom roles can not execute get-put commands 
    clientVM.invoke(IntegratedSecDUnitTest.class, "doPutUsingClientCache", new Object[] { region, "myk1", "myv1", false,
      "admin", "password123" });
    doPutUsingGfsh(region, "myk2", "myv2", false, "admin", "password123");
    doGetUsingGfsh(region, "myk1", false, "admin", "password123");
    clientVM.invoke(IntegratedSecDUnitTest.class, "doGetUsingClientCache", new Object[] { region, "myk2", false,
      "admin", "password123" });
    
    clientVM.invoke(IntegratedSecDUnitTest.class, "doPutUsingClientCache", new Object[] { region, "myk1", "myv1", false,
      "monitor", "password123" });
    doPutUsingGfsh(region, "myk2", "myv2", false, "monitor", "password123");
    doGetUsingGfsh(region, "myk1", false, "monitor", "password123");
    clientVM.invoke(IntegratedSecDUnitTest.class, "doGetUsingClientCache", new Object[] { region, "myk2", false,
      "monitor", "password123" });
    
    clientVM.invoke(IntegratedSecDUnitTest.class, "doPutUsingClientCache", new Object[] { region, "myk1", "myv1", false,
      "custom", "password123" });
    doPutUsingGfsh(region, "myk2", "myv2", false, "custom", "password123");
    doGetUsingGfsh(region, "myk1", false, "custom", "password123");
    clientVM.invoke(IntegratedSecDUnitTest.class, "doGetUsingClientCache", new Object[] { region, "myk2", false,
      "custom", "password123" });    
    
    // tushark can execute monitor command but not region create        
    doCommandUsingGfsh("show metrics", true, "monitor", "password123");
    doCommandUsingGfsh("show dead-locks --file=deadlocks_monitor.txt", true, "monitor", "password123");
    
    // dataWrite can execute create region
    doCommandUsingGfsh("create region --type=REPLICATE --name=dataWriteRegion", true, "dataWrite", "password123");
    doCommandUsingGfsh("create region --type=REPLICATE --name=dataReadRegion", false, "dataRead", "password123");
    
    // custom can create region create but not put region
    doCommandUsingGfsh("create region --type=REPLICATE --name=customRegion", true, "custom", "password123");
    doPutUsingGfsh("customRegion", "key", "value", false, "custom", "password123");
    
    // custom can execute show deadlocks - revoke it check again
    doCommandUsingGfsh("show metrics", false, "custom", "password123");
    doCommandUsingGfsh("show dead-locks --file=deadlocks_custom_1.txt", true, "custom", "password123");
    revokeResourceOp("custom", ResourceOperationCode.SHOW_DEADLOCKS.toString());
    grantResourceOp("custom", ResourceOperationCode.SHOW_METRICS.toString());
    doCommandUsingGfsh("show metrics", true, "custom", "password123");
    doCommandUsingGfsh("show dead-locks --file=deadlocks_custom_2.txt", false, "custom", "password123");
    grantResourceOp("custom", ResourceOperationCode.SHOW_DEADLOCKS.toString());    
    doCommandUsingGfsh("show metrics", true, "custom", "password123");
    doCommandUsingGfsh("show dead-locks --file=deadlocks_custom_3.txt", true, "custom", "password123");    
    
    /* Commented due to error with gradle :  TailLogRequest/Response processed in application vm with shared logging
    //check jmx and gfsh
    doCommandUsingGfsh("show log --member=Manager", true, "monitor", "password123");
    doCommandUsingGfsh("show log --member=Manager", false, "dataWrite", "password123");
    doCommandUsingGfsh("show log --member=Manager", false, "custom", "password123");
    
    
    doShowLogUsingJMX(true, "monitor", "password123");
    doShowLogUsingJMX(false, "dataWrite", "password123");
    doShowLogUsingJMX(false, "custom", "password123");

    
    grantResourceOp("custom", ResourceOperationCode.SHOW_LOG.toString());
    doCommandUsingGfsh("show log --member=Manager", true, "custom", "password123");
    doShowLogUsingJMX(true, "custom", "password123");*/
  }

  

  public void tearDown2() throws Exception {
    super.tearDown2();
  }

}
