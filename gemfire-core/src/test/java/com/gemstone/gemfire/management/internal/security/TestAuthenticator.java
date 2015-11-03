package com.gemstone.gemfire.management.internal.security;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.management.internal.security.ResourceOperationJUnit.TestUsernamePrincipal;
import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.gemstone.gemfire.security.Authenticator;

public class TestAuthenticator implements Authenticator {
  
  private static Logger logger = LogService.getLogger();
  public static Map<String,String> userRepo = new HashMap<String,String>();
  
  public static void addUser(String user, String password) {
    userRepo.put(user, password);
  }
  
  public static Authenticator create() {
    return new TestAuthenticator();
  }

  @Override
  public void close() {
    

  }

  @Override
  public void init(Properties securityProps, LogWriter systemLogger, LogWriter securityLogger)
      throws AuthenticationFailedException {
  }

  @Override
  public Principal authenticate(Properties props, DistributedMember member) throws AuthenticationFailedException {
    String user = props.getProperty(ResourceConstants.USER_NAME);
    String pwd = props.getProperty(ResourceConstants.PASSWORD);
    if (user != null && userRepo.containsKey(user) && pwd != null && pwd.equals(userRepo.get(user))) {
      logger.info("Authentication successful!! for " + user);
      return new TestUsernamePrincipal(user);
    } else
      throw new AuthenticationFailedException("Wrong username/password");
  }

}
