package com.gemstone.gemfire.management.internal.security;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheCallback;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.management.ManagementService;
import com.gemstone.gemfire.management.internal.AuthManager;
import com.gemstone.gemfire.management.internal.AuthManager.CommandAuthZRequest;
import com.gemstone.gemfire.management.internal.SystemManagementService;
import com.gemstone.gemfire.management.internal.security.CLIOperationContext;
import com.gemstone.gemfire.management.internal.security.ResourceConstants;
import com.gemstone.gemfire.security.AccessControl;
import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.gemstone.gemfire.test.junit.categories.UnitTest;

import junit.framework.TestCase;

@Category(UnitTest.class)
public class AuthManagerJUnitTest extends TestCase {

  InternalDistributedSystem system;
  
  private Cache createCache(Properties props) {
    
    this.system = (InternalDistributedSystem) DistributedSystem.connect(props);
    Cache cache =  new CacheFactory().create();
    return cache;
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    this.system.disconnect();
    this.system = null;
  }

  public void testInvalidAuthenticator() {
    Properties props = new Properties();
    props.setProperty(DistributionConfig.JMX_MANAGER_NAME, "true");
    props.setProperty(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    props.setProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, "com.gemstone.gemfire.management.security.CustomAuthenticatorINVALID.create");
    Cache cache = createCache(props);

    SystemManagementService service = (SystemManagementService) ManagementService.getExistingManagementService(cache);

    AuthManager auth = service.getAuthManager();
    Properties creds = new Properties();
    try {
      auth.verifyCredentials(creds);
      fail("should not have reached here");
    } catch (AuthenticationFailedException ae) {

    }
  }
  
  public void testAuthMapSize() {
    Properties props = new Properties();
      props.setProperty(DistributionConfig.JMX_MANAGER_NAME, "true");
    props.setProperty(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    props.setProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, CustomAuthenticator.class.getName()+".create");
    Cache cache = createCache(props);

    SystemManagementService service = (SystemManagementService) ManagementService.getExistingManagementService(cache);

    AuthManager auth = service.getAuthManager();
    Properties creds = new Properties();
    creds.put(CommandBuilders.SEC_USER_NAME, "AUTHC_1");
    try {
      auth.verifyCredentials(creds);
      assertEquals(1, auth.getAuthMap().size());
      
      auth.verifyCredentials(creds);
      
      assertEquals(1, auth.getAuthMap().size());
      
      creds.put(CommandBuilders.SEC_USER_NAME, "AUTHC_2");
      auth.verifyCredentials(creds);
      assertEquals(2, auth.getAuthMap().size());
    } catch (AuthenticationFailedException ae) {
      ae.printStackTrace();
      fail("should not have reached here "+ ae);
    }catch (Exception ae) {
      ae.printStackTrace();
      fail("should not have reached here "+ ae);
    }
  }
  
  public void testExpiry() {
    Properties props = new Properties();
    props.setProperty(DistributionConfig.JMX_MANAGER_NAME, "true");
    props.setProperty(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    props.setProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, CustomAuthenticator.class.getName()+".create");
    System.setProperty(AuthManager.EXPIRY_TIME_FOR_REST_ADMIN_AUTH, "1");
    Cache cache = createCache(props);

    SystemManagementService service = (SystemManagementService) ManagementService.getExistingManagementService(cache);

    AuthManager auth = service.getAuthManager();
    Properties creds = new Properties();
    creds.put(CommandBuilders.SEC_USER_NAME, "AUTHC_1");
    try {
      auth.verifyCredentials(creds);
      assertEquals(1, auth.getAuthMap().size());
      CommandAuthZRequest authReq1 = auth.getAuthMap().get(creds);
      Thread.sleep(2 * 60 * 1000);
      auth.verifyCredentials(creds);
      CommandAuthZRequest authReq2 = auth.getAuthMap().get(creds);
      assertTrue(!authReq1.equals(authReq2));
     
    } catch (AuthenticationFailedException ae) {
      fail("should not have reached here "+ ae);
    }catch (Exception ae) {
      fail("should not have reached here "+ ae);
    }
  }
  
  public void testAuthorize() {
    Properties props = new Properties();
    props.setProperty(DistributionConfig.JMX_MANAGER_NAME, "true");
    props.setProperty(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    props.setProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, CustomAuthenticator.class.getName()+".create");
    props.setProperty(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME,
        CustomAccessControl.class.getName()+".create");
    Cache cache = createCache(props);

    SystemManagementService service = (SystemManagementService) ManagementService.getExistingManagementService(cache);

    AuthManager auth = service.getAuthManager();
    Properties creds = new Properties();
    creds.put(CommandBuilders.SEC_USER_NAME, "AUTHC_26");
    try {
      auth.verifyCredentials(creds);
      auth.authorize(creds, new CLIOperationContext("gc"));
      assertEquals(1, auth.getAuthMap().size());
    } catch (AuthenticationFailedException ae) {
      fail("should not have reached here "+ ae);
    }catch (Exception ae) {
      fail("should not have reached here "+ ae);
    }
  }
  
  public void testCacheClose() {
    Properties props = new Properties();
    props.setProperty(DistributionConfig.JMX_MANAGER_NAME, "true");
    props.setProperty(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    props.setProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, CustomAuthenticator.class.getName()+".create");
    props.setProperty(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME,
        CustomAccessControl.class.getName()+".create");
    Cache cache = createCache(props);

    SystemManagementService service = (SystemManagementService) ManagementService.getExistingManagementService(cache);

    AuthManager auth = service.getAuthManager();
    Properties creds = new Properties();
    creds.put(CommandBuilders.SEC_USER_NAME, "AUTHC_26");
    try {
      auth.verifyCredentials(creds);
      auth.authorize(creds, new CLIOperationContext("gc"));
      assertEquals(1, auth.getAuthMap().size());
      Map<Properties, CommandAuthZRequest> authMap = auth.getAuthMap();
      Map<Properties, CommandAuthZRequest> tempAuthMap = new HashMap<Properties, CommandAuthZRequest>(authMap);
      Iterator<CommandAuthZRequest> it = tempAuthMap.values().iterator();
    
        CommandAuthZRequest authz = it.next();
        CacheCallback control = authz.getAuthzCallback();
        CustomAccessControl cControl = CustomAccessControl.class.cast(control);
        assertFalse(cControl.isClose());
        
        cache.close();
        
        assertTrue(cControl.isClose());
      
    } catch (AuthenticationFailedException ae) {
      ae.printStackTrace();
      fail("should not have reached here "+ ae);
    }catch (Exception ae) {
      ae.printStackTrace();
      fail("should not have reached here "+ ae);
    }
  }


}
