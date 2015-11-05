package com.gemstone.gemfire.rest.internal.web.controllers;

/**
 * A dummy implementation of the {@link Authenticator} interface that expects a
 * user name and password allowing authentication depending on the format of the
 * user name.
 * 
 * @author Nilkanth Patel
 * @since 9.0
 */

import java.security.Principal;
import java.util.Properties;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.gemstone.gemfire.security.Authenticator;
import templates.security.UserPasswordAuthInit;
import templates.security.UsernamePrincipal;


public class CustomRestAPIsAuthenticator implements Authenticator { 

  public static Authenticator create() {
    return new CustomRestAPIsAuthenticator();
  }

  public CustomRestAPIsAuthenticator() {
  }

  public void init(Properties systemProps, LogWriter systemLogger,
      LogWriter securityLogger) throws AuthenticationFailedException {
  }

  public static boolean testValidName(String userName) {

    return (userName.startsWith("user") || userName.startsWith("reader")
        || userName.startsWith("writer") || userName.equals("admin")
        || userName.equals("root") || userName.equals("administrator"));
  }

  public Principal authenticate(Properties props, DistributedMember member)
      throws AuthenticationFailedException {

    String userName = props.getProperty(UserPasswordAuthInit.USER_NAME);
    if (userName == null) {
      throw new AuthenticationFailedException(
          "DummyAuthenticator: user name property ["
              + UserPasswordAuthInit.USER_NAME + "] not provided");
    }
    String password = props.getProperty(UserPasswordAuthInit.PASSWORD);
    if (password == null) {
      throw new AuthenticationFailedException(
          "DummyAuthenticator: password property ["
              + UserPasswordAuthInit.PASSWORD + "] not provided");
    }

    if (userName.equals(password) && testValidName(userName)) {
      return new UsernamePrincipal(userName);
    }
    else {
      throw new AuthenticationFailedException(
          "DummyAuthenticator: Invalid user name [" + userName
              + "], password supplied.");
    }
  }

  public void close() {
  }

}
