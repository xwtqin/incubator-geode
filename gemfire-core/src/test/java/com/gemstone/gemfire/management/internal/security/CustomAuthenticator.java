package com.gemstone.gemfire.management.internal.security;

import java.security.Principal;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.management.remote.JMXPrincipal;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.gemstone.gemfire.security.Authenticator;


public class CustomAuthenticator implements  Authenticator {
  


  public static Authenticator create() {
    return new CustomAuthenticator();
  }

  
  public static final String AUTHENTICATED_USER = "AUTHC_";
  public static final String NON_AUTHORIZED_USER = "UNAUTHC_";
  public static final String ADMIN_USER = "ADMIN_";
  
  
  
  Set<String> users = new HashSet<String>();
  
  private boolean closed = false;

  @Override
  public void close() {
    this.closed = true;    
  }

  @Override
  public void init(Properties securityProps, LogWriter systemLogger, LogWriter securityLogger)
      throws AuthenticationFailedException {
  }

  @Override
  public Principal authenticate(Properties props, DistributedMember member) throws AuthenticationFailedException {
    String user = props.getProperty(CommandBuilders.SEC_USER_NAME);
    String pwd = props.getProperty(CommandBuilders.SEC_USER_PWD);
    if (user.startsWith(AUTHENTICATED_USER) || user.startsWith(NON_AUTHORIZED_USER) || user.startsWith(ADMIN_USER)) {
      return new JMXPrincipal(user);

    }
    throw new AuthenticationFailedException("Not authenticated");

  }

  
}
