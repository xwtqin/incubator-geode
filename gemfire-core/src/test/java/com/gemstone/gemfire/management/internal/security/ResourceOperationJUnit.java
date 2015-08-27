package com.gemstone.gemfire.management.internal.security;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;



import org.junit.experimental.categories.Category;


import junit.framework.TestCase;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.operations.OperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.internal.AvailablePort;
import com.gemstone.gemfire.internal.AvailablePortHelper;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.management.DistributedSystemMXBean;
import com.gemstone.gemfire.management.MemberMXBean;
import com.gemstone.gemfire.management.internal.MBeanJMXAdapter;
import com.gemstone.gemfire.management.internal.cli.CommandManager;
import com.gemstone.gemfire.management.internal.cli.parser.CommandTarget;
import com.gemstone.gemfire.management.internal.cli.parser.GfshMethodTarget;
import com.gemstone.gemfire.management.internal.security.ResourceOperationContext.ResourceOperationCode;
import com.gemstone.gemfire.security.AccessControl;
import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.gemstone.gemfire.security.Authenticator;
import com.gemstone.gemfire.security.NotAuthorizedException;
import com.gemstone.gemfire.test.junit.categories.UnitTest;

@Category(UnitTest.class)
public class ResourceOperationJUnit  extends TestCase {
  
  public static class TestUsernamePrincipal implements Principal,
      Serializable {

    private final String userName;

    public TestUsernamePrincipal(String userName) {
      this.userName = userName;
    }

    public String getName() {
      return this.userName;
    }

    @Override
    public String toString() {
      return this.userName;
    }

  }

  public static class TestAuthenticator implements Authenticator {

    @Override
    public void close() {

    }

    @Override
    public void init(Properties securityProps, LogWriter systemLogger,
        LogWriter securityLogger) throws AuthenticationFailedException {

    }

    @Override
    public Principal authenticate(Properties props, DistributedMember member)
 throws AuthenticationFailedException {
      String mysecret = props.getProperty("GoTSecret");
      props.list(System.out);
      if (mysecret == null) {
        String user = props.getProperty(ResourceConstants.USER_NAME);
        String pwd = props.getProperty(ResourceConstants.PASSWORD);
        if (user != null && !user.equals(pwd) && !"".equals(user))
          throw new AuthenticationFailedException("Wrong username/password");
        System.out.println("Authentication successful!! for " + user);
        return new TestUsernamePrincipal(user);
      } else {
        if (mysecret.equals("JohnSnowIsIceAndFire")) {
          System.out.println("Authentication successful!! for IronThrone");
          return new TestUsernamePrincipal("IronThrone");
        } else
          throw new AuthenticationFailedException("Wrong username/password");
      }
    }
    
    public static Authenticator create() {
      return new TestAuthenticator();
    }

  }
  
  public static class TestAccessControl implements AccessControl {

    private Principal principal=null;   
    public static AtomicInteger preCallsTL = new AtomicInteger(0);
    public static AtomicInteger postCallsTL = new AtomicInteger(0);
    
    public static boolean failPostOpIntentionally = false;
    
    public static AccessControl create(){
      return new TestAccessControl();
    }
    
    static {
      resetCallsCounter();
    }

    public static void resetCallsCounter() {      
      preCallsTL.set(0);
      postCallsTL.set(0);
    }
    
    @Override
    public void close() {
      
    }

    @Override
    public void init(Principal principal, DistributedMember remoteMember,
        Cache cache) throws NotAuthorizedException {
      this.principal = principal;
    }
    
    private void increaseCount(OperationContext context) {
      if (!(context instanceof AccessControlContext)) {
        if (context.isPostOperation()) {
          postCallsTL.incrementAndGet();
          System.out.println("Context Received " + context + " isPostOp=" + context.isPostOperation() + " calls="
              + postCallsTL.get());
        } else {
          preCallsTL.incrementAndGet();
          System.out.println("Context Received " + context + " isPreOp=" + context.isPostOperation() + " calls="
              + preCallsTL.get());
        }
      }     
    }

    @Override
    public boolean authorizeOperation(String regionName,
        OperationContext context) {

      if(context.isPostOperation() && failPostOpIntentionally) {
        System.out.println("Failing AuthZ since failPostOpIntentionally=true");
        return false;
      }
      
      if(principal.getName().equals("tushark") || principal.getName().equals("IronThrone")) {       
        ResourceOperationCode authorizedOps[] = {
            ResourceOperationCode.LIST_DS,
            ResourceOperationCode.CHANGE_ALERT_LEVEL,
            ResourceOperationCode.LOCATE_ENTRY,
            ResourceOperationCode.QUERY
        };
        
        LogService.getLogger().info("Context received " + context);        
        ResourceOperationContext ctx = (ResourceOperationContext) context;
        LogService.getLogger().info("Checking for code " + ctx.getResourceOperationCode());
        boolean found = false;
        for (ResourceOperationCode code : authorizedOps) {
          if (ctx.getResourceOperationCode().equals(code)) {
            found = true;
            LogService.getLogger().info("found code " + code.toString());
            break;
          }
        }
        if (found) {
          increaseCount(context);
          return true;
        }
        LogService.getLogger().info("Did not find code " + ctx.getResourceOperationCode());        
        return false;
      }     
      return false;
    }
    
  }
  
  public void testJMXOperationContext() {   
    TestAccessControl.resetCallsCounter();    
    GemFireCacheImpl cache = null;
    DistributedSystem ds = null;
    Properties pr = new Properties();
    pr.put("name", "testJMXOperationContext");
    pr.put(DistributionConfig.JMX_MANAGER_NAME, "true");
    pr.put(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    int port = AvailablePort.getRandomAvailablePort(AvailablePort.SOCKET);
    pr.put(DistributionConfig.JMX_MANAGER_PORT_NAME, String.valueOf(port));
    pr.put(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");
    
    pr.put(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAuthenticator.create");
    pr.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAccessControl.create");
    pr.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_PP_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAccessControl.create");
    
    ds = getSystem(pr);
    cache = (GemFireCacheImpl) CacheFactory.create(ds);
    ObjectName name = MBeanJMXAdapter.getDistributedSystemName();
    
    String[] methods = {
        "listCacheServerObjectNames",
        "viewRemoteClusterStatus",
        "getTotalHeapSize",
        "setQueryCollectionsDepth",
        "getQueryCollectionsDepth",
        "changeAlertLevel",
        "backupAllMembers",
        "revokeMissingDiskStores",
        "shutDownAllMembers",
        "queryData",
        "queryDataForCompressedResult",
        "setQueryResultSetLimit",       
    };
    
    ResourceOperationCode expectedCodes[] = {
        ResourceOperationCode.LIST_DS,
        ResourceOperationCode.LIST_DS,
        ResourceOperationCode.LIST_DS,
        ResourceOperationCode.QUERY,
        ResourceOperationCode.LIST_DS,
        ResourceOperationCode.CHANGE_ALERT_LEVEL,
        ResourceOperationCode.BACKUP_MEMBERS,
        ResourceOperationCode.REVOKE_MISSING_DISKSTORE,
        ResourceOperationCode.SHUTDOWN,
        ResourceOperationCode.QUERY,
        ResourceOperationCode.QUERY,
        ResourceOperationCode.QUERY
    };
        
    for(int i=0;i<methods.length;i++) {
      String methodName = methods[i];
      JMXOperationContext context = new JMXOperationContext(name, methodName);
      assertEquals(expectedCodes[i],
          context.getResourceOperationCode());
      assertEquals(OperationCode.RESOURCE, context.getOperationCode());
    }
    
    JMXConnector cs = getGemfireMBeanServer(port, "tushark", "tushark");;
    MBeanServerConnection mbeanServer =null;
    int totalCalls=-1;
    try {
      mbeanServer = cs.getMBeanServerConnection();
      mbeanServer.invoke(MBeanJMXAdapter.getDistributedSystemName(), "listCacheServerObjectNames", null, null);
      String oldLevel = (String)mbeanServer.getAttribute(MBeanJMXAdapter.getDistributedSystemName(), "AlertLevel");
      System.out.println("Old Level = " + oldLevel);
      mbeanServer.invoke(MBeanJMXAdapter.getDistributedSystemName(), "changeAlertLevel", new Object[]{"WARNING"},new String[]{
        String.class.getCanonicalName()
      });
      String newLevel = (String)mbeanServer.getAttribute(MBeanJMXAdapter.getDistributedSystemName(), "AlertLevel");
      System.out.println("New Level = " + newLevel);
      
      
      //totalCalls = 1 for listCacheServerObjectNames +  changeAlertLevel + 2 for AlertLevel attr
      totalCalls = 2 + 2 ;
      totalCalls += checkGetAttributesAndSetAttributes(mbeanServer);
      assertEquals(totalCalls, (int)TestAccessControl.preCallsTL.get());
      assertEquals(totalCalls, (int)TestAccessControl.postCallsTL.get());
      checkAcceeControlMXBean(mbeanServer);
      
    } catch (InstanceNotFoundException e1) {
      e1.printStackTrace();
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (MBeanException e1) {
      e1.printStackTrace();
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (ReflectionException e1) {
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (IOException e1) {
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (AttributeNotFoundException e) {
      fail("Error while invoking JMXRMI" + e.getMessage());
    } catch (MalformedObjectNameException e) {
      fail("Error while invoking JMXRMI" + e.getMessage());
    }
    
    try {
      mbeanServer.invoke(MBeanJMXAdapter.getDistributedSystemName(),
          "backupAllMembers", 
          new Object[]{"targetPath","baseLinePath"}, 
          new String[]{String.class.getCanonicalName(), String.class.getCanonicalName()});
      fail("Should not be authorized for backupAllMembers");
    } catch (SecurityException e) {
      //expected
    } catch(Exception e){
      e.printStackTrace();
      fail("Unexpected exception : " + e.getMessage());
    }
    
    try {
      mbeanServer.invoke(MBeanJMXAdapter.getDistributedSystemName(),
          "shutDownAllMembers",null,null);
      fail("Should not be authorized for shutDownAllMembers");
    } catch (SecurityException e) {
      //expected
    } catch(Exception e){
      fail("Unexpected exception : " + e.getMessage());
    }
    
    //2 unsuccessful calls    
    assertEquals(totalCalls, (int)TestAccessControl.preCallsTL.get());
    assertEquals(totalCalls, (int)TestAccessControl.postCallsTL.get());
    
    checkCLIContext(mbeanServer);totalCalls += 2;       
    assertEquals(totalCalls, (int)TestAccessControl.preCallsTL.get());
    assertEquals(totalCalls, (int)TestAccessControl.postCallsTL.get());
    
    //Simulate a condition where accessControl return false during postOpAuthZ
    TestAccessControl.failPostOpIntentionally = true;
    for(int i=1;i<=3;i++) {
      try {
        mbeanServer.invoke(MBeanJMXAdapter.getDistributedSystemName(), "changeAlertLevel", new Object[] { "WARNING" },
            new String[] { String.class.getCanonicalName() });              
      } catch (InstanceNotFoundException e) {
        fail("Unexpected exception : " + e.getMessage());
      } catch (MBeanException e) {
        fail("Unexpected exception : " + e.getMessage());
      } catch (ReflectionException e) {
        fail("Unexpected exception : " + e.getMessage());
      } catch (IOException e) {
        fail("Unexpected exception : " + e.getMessage());
      }  catch (SecurityException e) {
        //expected
        assertEquals(totalCalls+i, (int)TestAccessControl.preCallsTL.get());
        assertEquals(totalCalls, (int)TestAccessControl.postCallsTL.get());
      }  
    }    
    TestAccessControl.failPostOpIntentionally = false;
    try {
      cs.close();
    } catch (IOException e) {
      fail("Unexpected exception : " + e.getMessage());
    }
    
    cache.close();
    ds.disconnect();
  }
  
  private int checkGetAttributesAndSetAttributes(MBeanServerConnection mbeanServer) throws InstanceNotFoundException, ReflectionException, IOException {
    AttributeList list = mbeanServer.getAttributes(MBeanJMXAdapter.getDistributedSystemName(),
        new String[]{"TotalHeapSize","TotalRegionEntryCount","TotalRegionCount","TotalMissCount"});    
    assertNotNull(list);
    assertEquals(4,list.size());    
    list = new AttributeList();
    list.add(new Attribute("QueryResultSetLimit", 1000));    
    list.add(new Attribute("QueryCollectionsDepth",1000));
    list = mbeanServer.setAttributes(MBeanJMXAdapter.getDistributedSystemName(),list);
    assertNotNull(list);
    assertEquals(2,list.size());
    return 3;
  }

  public void testOnlyAuthenticatorNoAuthorization() {    
    TestAccessControl.resetCallsCounter();    
    GemFireCacheImpl cache = null;
    DistributedSystem ds = null;
    Properties pr = new Properties();
    pr.put("name", "testJMXOperationContext");
    pr.put(DistributionConfig.JMX_MANAGER_NAME, "true");
    pr.put(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    int port = AvailablePortHelper.getRandomAvailableTCPPort();
    pr.put(DistributionConfig.JMX_MANAGER_PORT_NAME, String.valueOf(port));
    pr.put(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");    
    pr.put(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAuthenticator.create");    
    ds = getSystem(pr);
    cache = (GemFireCacheImpl) CacheFactory.create(ds);
    JMXConnector cs = getGemfireMBeanServer(port, "tushark", "tushark");;
    MBeanServerConnection mbeanServer =null;
    try {
      mbeanServer = cs.getMBeanServerConnection();
      mbeanServer.invoke(MBeanJMXAdapter.getDistributedSystemName(), "listCacheServerObjectNames", null, null);
      String oldLevel = (String)mbeanServer.getAttribute(MBeanJMXAdapter.getDistributedSystemName(), "AlertLevel");
      System.out.println("Old Level = " + oldLevel);
      mbeanServer.invoke(MBeanJMXAdapter.getDistributedSystemName(), "changeAlertLevel", new Object[]{"WARNING"},new String[]{
        String.class.getCanonicalName()
      });
      String newLevel = (String)mbeanServer.getAttribute(MBeanJMXAdapter.getDistributedSystemName(), "AlertLevel");
      System.out.println("New Level = " + newLevel);      
      //totalCalls = 0 since not AccessControl is invoked     
      assertEquals(0, (int)TestAccessControl.preCallsTL.get());
      assertEquals(0, (int)TestAccessControl.postCallsTL.get());      
    } catch (InstanceNotFoundException e1) {
      e1.printStackTrace();
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (MBeanException e1) {
      e1.printStackTrace();
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (ReflectionException e1) {
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (IOException e1) {
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (AttributeNotFoundException e) {
      fail("Error while invoking JMXRMI" + e.getMessage());
    } 
    cache.close();
    ds.disconnect();
  }
  
  public void testAuthenticationUsingPropertiesBag(){
    TestAccessControl.resetCallsCounter();    
    GemFireCacheImpl cache = null;
    DistributedSystem ds = null;
    Properties pr = new Properties();
    pr.put("name", "testJMXOperationContext");
    pr.put(DistributionConfig.JMX_MANAGER_NAME, "true");
    pr.put(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    int port = AvailablePortHelper.getRandomAvailableTCPPort();
    pr.put(DistributionConfig.JMX_MANAGER_PORT_NAME, String.valueOf(port));
    pr.put(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");    
    pr.put(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAuthenticator.create");
    pr.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAccessControl.create");
    pr.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_PP_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAccessControl.create");
    ds = getSystem(pr);
    cache = (GemFireCacheImpl) CacheFactory.create(ds);
    
    Properties userProperties = new Properties();
    userProperties.put("GoTSecret", "JohnSnowIsIceAndFire");
    JMXConnector cs = getGemfireMBeanServer(port, userProperties);
    MBeanServerConnection mbeanServer =null;
    try {
      mbeanServer = cs.getMBeanServerConnection();      
      String oldLevel = (String)mbeanServer.getAttribute(MBeanJMXAdapter.getDistributedSystemName(), "AlertLevel");
      System.out.println("Old Level = " + oldLevel);      
      cs.close();
    } catch (InstanceNotFoundException e1) {
      e1.printStackTrace();
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (MBeanException e1) {
      e1.printStackTrace();
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (ReflectionException e1) {
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (IOException e1) {
      fail("Error while invoking JMXRMI " + e1.getMessage());
    } catch (AttributeNotFoundException e) {
      fail("Error while invoking JMXRMI" + e.getMessage());
    }
    
    try {
      userProperties = new Properties();
      userProperties.put("GoTSecret", "JoffreyIsRightfulKing");
      cs = getGemfireMBeanServer(port, userProperties);
      fail("Authentication should fail");
    } catch (AuthenticationFailedException e) {
      //expected
    } catch (SecurityException e) {
      //expected
    } catch(Exception e){
      e.printStackTrace();
      fail("Unexpected error " + e.getMessage());      
    }
    cache.close();
    ds.disconnect();
  }
  
  private void checkAcceeControlMXBean(MBeanServerConnection mbeanServer) throws MalformedObjectNameException,
      InstanceNotFoundException, MBeanException, ReflectionException, IOException {
    // Checking accessControlMXBean
    System.out.println("Checking access via AccessControlMbean");
    ResourceOperationCode authorizedOps[] = { ResourceOperationCode.LIST_DS, ResourceOperationCode.LIST_DS,
        ResourceOperationCode.CHANGE_ALERT_LEVEL, ResourceOperationCode.LOCATE_ENTRY };
    ObjectName accControlON = new ObjectName(ResourceConstants.OBJECT_NAME_ACCESSCONTROL);
    for (ResourceOperationCode c : authorizedOps) {
      boolean result = (Boolean) mbeanServer.invoke(accControlON, "authorize",
          new Object[] { c.toString() },
          new String[] { String.class.getCanonicalName() });
      assertTrue(result);
    }
    
    //check accessControlMBean is hidden from generic listing
    Set<ObjectInstance> instanceSet = mbeanServer.queryMBeans(null, null);
    for(ObjectInstance oi : instanceSet) {
      if(oi.getObjectName().equals(accControlON))
        fail("Found AccessControl Mbean in queryMbeans");
    }
    
    Set<ObjectName> onSet = mbeanServer.queryNames(null, null);
    for(ObjectName on : onSet) {
      if(on.equals(accControlON))
        fail("Found AccessControl Mbean in queryNames");
    }

  }

  private void checkCLIContext(MBeanServerConnection mbeanServer) {
    DistributedSystemMXBean proxy = JMX.newMXBeanProxy(mbeanServer, MBeanJMXAdapter.getDistributedSystemName(),
        DistributedSystemMXBean.class);
    ObjectName managerMemberObjectName = proxy.getMemberObjectName();
    MemberMXBean memberMXBeanProxy = JMX.newMXBeanProxy(mbeanServer, managerMemberObjectName, MemberMXBean.class);
    try {
      Map<String,String> map = new HashMap<String,String>();
      map.put("APP","GFSH");
      String result = memberMXBeanProxy.processCommand("locate entry --key=k1 --region=/region1", map);
      System.out.println("Result = " + result);
    } catch (Exception e) {
      System.out.println("Excpetion e " + e.getMessage());
      fail(e.getMessage());
    }
  }
  
  public void testAllCommandsAreAnnotated() {
    GemFireCacheImpl cache = null;
    DistributedSystem ds = null;
    Properties pr = new Properties();
    pr.put("name", "testJMXOperationContext");
    pr.put(DistributionConfig.JMX_MANAGER_NAME, "true");
    pr.put(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    int port = AvailablePortHelper.getRandomAvailableTCPPort();
    pr.put(DistributionConfig.JMX_MANAGER_PORT_NAME, String.valueOf(port));
    pr.put(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");
    pr.put(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAuthenticator.create");
    pr.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAccessControl.create");
    pr.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_PP_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAccessControl.create");
    ds = getSystem(pr);
    cache = (GemFireCacheImpl) CacheFactory.create(ds);
    
    CommandManager manager = CommandManager.getExisting();
    List<String> notFoundList = new ArrayList<String>();
    Map<String,CommandTarget> map = manager.getCommands();
    for(Map.Entry<String,CommandTarget> entry : map.entrySet()) {
      String commandName = entry.getKey();
      CommandTarget target = entry.getValue();
      GfshMethodTarget methodTarget = target.getGfshMethodTarget();
      boolean found=false;
      Annotation ans[] = methodTarget.getMethod().getDeclaredAnnotations();
      for(Annotation an : ans){
        if(an instanceof ResourceOperation) {
          String opcode= ((ResourceOperation) an).operation();
          LogService.getLogger().info("For command " + commandName + " OpCode="+ opcode);
          found = true;
        }
      }
      if(!found)
        notFoundList.add(commandName);
    }
    System.out.println("Command without any annotation " + notFoundList);
    assertEquals(0,notFoundList.size());
    cache.close();
    ds.disconnect();
  }

  public void testCLIOperationContext() { 
    GemFireCacheImpl cache = null;
    DistributedSystem ds = null;
    Properties pr = new Properties();
    pr.put("name", "testJMXOperationContext");
    pr.put(DistributionConfig.JMX_MANAGER_NAME, "true");
    pr.put(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    int port = AvailablePort.getRandomAvailablePort(AvailablePort.SOCKET);
    pr.put(DistributionConfig.JMX_MANAGER_PORT_NAME, String.valueOf(port));
    pr.put(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");
    pr.put(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAuthenticator.create");
    pr.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAccessControl.create");
    pr.put(DistributionConfig.SECURITY_CLIENT_ACCESSOR_PP_NAME, "com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit$TestAccessControl.create");
    ds = getSystem(pr);
    cache = (GemFireCacheImpl) CacheFactory.create(ds);
    
    String[] commands = {       
        "export data --region=value --file=value --member=value",
        "import data --region=value --file=value --member=value",
        "rebalance",
        "get --key=k1 --region=/region1",
        "put --key=k1 --value=v1 --region=/region1",
        "locate entry --key=k1 --region=/region1",
        "query --query=\"select * from /region1\"",       
        "remove --key=k1 --region=/region1",                
        "remove --region=/region1 --all=true", //region clear
        "create region --name=r1 --type=REPLICATE",
        "destroy region --name=/r1", 
        "execute function --id=func1",
        "close durable-cq --durable-client-id=value --durable-cq-name=value"
         //"stop cq"
        //"removeall",
        //"get durable cqs",        
    };
    
    ResourceOperationCode expectedResourceCodes[] = {       
        ResourceOperationCode.EXPORT_DATA,
        ResourceOperationCode.IMPORT_DATA,
        ResourceOperationCode.REBALANCE,
        ResourceOperationCode.GET,
        ResourceOperationCode.PUT,
        ResourceOperationCode.LOCATE_ENTRY,
        ResourceOperationCode.QUERY,
        ResourceOperationCode.REMOVE,
        ResourceOperationCode.REMOVE,
        ResourceOperationCode.CREATE_REGION,
        ResourceOperationCode.DESTROY_REGION,
        ResourceOperationCode.EXECUTE_FUNCTION,
        ResourceOperationCode.CLOSE_DURABLE_CQ,
    };
    
    OperationCode expectedOpCodes[] = {        
        OperationCode.RESOURCE,
        OperationCode.RESOURCE,
        OperationCode.RESOURCE,
        OperationCode.GET,
        OperationCode.PUT,
        OperationCode.GET,
        OperationCode.QUERY,
        OperationCode.DESTROY,
        OperationCode.REMOVEALL,
        OperationCode.REGION_CREATE,
        OperationCode.REGION_DESTROY,
        OperationCode.EXECUTE_FUNCTION,
        OperationCode.CLOSE_CQ,
    };
    
    for(int i=0;i<commands.length;i++){
      CLIOperationContext ctx = new CLIOperationContext(commands[i]);
      System.out.println("Context " + ctx);
      assertEquals(expectedResourceCodes[i],ctx.getResourceOperationCode());
      assertEquals(expectedOpCodes[i],ctx.getOperationCode());
    }
    
    cache.close();
    ds.disconnect();
  }
  
  public void testResourceOpCodeAllowedOp() {
    assertTrue(ResourceOperationCode.ADMIN.allowedOp(ResourceOperationCode.LIST_DS));
    assertTrue(ResourceOperationCode.DATA_READ.allowedOp(ResourceOperationCode.LIST_DS));
    assertTrue(ResourceOperationCode.DATA_WRITE.allowedOp(ResourceOperationCode.LIST_DS));
    assertTrue(ResourceOperationCode.MONITOR.allowedOp(ResourceOperationCode.LIST_DS));

    assertTrue(ResourceOperationCode.MONITOR.allowedOp(ResourceOperationCode.DATA_READ));
    assertTrue(ResourceOperationCode.DATA_WRITE.allowedOp(ResourceOperationCode.DATA_READ));
    assertTrue(ResourceOperationCode.ADMIN.allowedOp(ResourceOperationCode.DATA_READ));

    assertTrue(ResourceOperationCode.MONITOR.allowedOp(ResourceOperationCode.PULSE_DASHBOARD));
    assertTrue(ResourceOperationCode.DATA_WRITE.allowedOp(ResourceOperationCode.PULSE_DASHBOARD));
    assertTrue(ResourceOperationCode.ADMIN.allowedOp(ResourceOperationCode.PULSE_DASHBOARD));

    assertFalse(ResourceOperationCode.PULSE_DASHBOARD.allowedOp(ResourceOperationCode.BECOME_LOCK_GRANTOR));
    assertTrue(ResourceOperationCode.PULSE_DASHBOARD.allowedOp(ResourceOperationCode.PULSE_DASHBOARD));

    assertTrue(ResourceOperationCode.ADMIN.allowedOp(ResourceOperationCode.SHUTDOWN));
    assertFalse(ResourceOperationCode.DATA_READ.allowedOp(ResourceOperationCode.SHUTDOWN));
    assertFalse(ResourceOperationCode.DATA_WRITE.allowedOp(ResourceOperationCode.SHUTDOWN));
    assertFalse(ResourceOperationCode.MONITOR.allowedOp(ResourceOperationCode.SHUTDOWN));

    assertTrue(ResourceOperationCode.ADMIN.allowedOp(ResourceOperationCode.BECOME_LOCK_GRANTOR));
    assertFalse(ResourceOperationCode.DATA_READ.allowedOp(ResourceOperationCode.BECOME_LOCK_GRANTOR));
    assertTrue(ResourceOperationCode.DATA_WRITE.allowedOp(ResourceOperationCode.BECOME_LOCK_GRANTOR));
    assertFalse(ResourceOperationCode.MONITOR.allowedOp(ResourceOperationCode.BECOME_LOCK_GRANTOR));

    assertTrue(ResourceOperationCode.ADMIN.allowedOp(ResourceOperationCode.EXPORT_STACKTRACE));
    assertFalse(ResourceOperationCode.DATA_READ.allowedOp(ResourceOperationCode.EXPORT_STACKTRACE));
    assertFalse(ResourceOperationCode.DATA_WRITE.allowedOp(ResourceOperationCode.EXPORT_STACKTRACE));
    assertTrue(ResourceOperationCode.MONITOR.allowedOp(ResourceOperationCode.EXPORT_STACKTRACE));
  }
  
  private JMXConnector getGemfireMBeanServer(int port, Properties pr) {
    return _getGemfireMBeanServer(port, pr);
  }
  
  private JMXConnector getGemfireMBeanServer(int port, String user, String pwd) {
    String[] creds = null;
    if(user!=null) 
      creds = new String[]{ user, pwd };
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

  private static DistributedSystem getSystem(Properties properties) {
      return DistributedSystem.connect(properties);
    }

}

