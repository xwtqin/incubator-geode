package com.gemstone.gemfire.management.internal.security;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

/**
 * AccessControlMBean Implementation. This retrieves JMXPrincipal from AccessController
 * and performs authorization for given role using gemfire AccessControl Plugin
 *  
 * @author tushark
 * @since 9.0
 */
public class AccessControl implements AccessControlMXBean {

  private ManagementInterceptor interceptor;

  public AccessControl(ManagementInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  @Override
  public boolean authorize(String role) {
    AccessControlContext acc = AccessController.getContext();
    Subject subject = Subject.getSubject(acc);
    Set<JMXPrincipal> principals = subject.getPrincipals(JMXPrincipal.class);    
    if (principals == null || principals.isEmpty()) {
      throw new SecurityException("Access denied");
    }
    Principal principal = principals.iterator().next();
    com.gemstone.gemfire.security.AccessControl gemAccControl = interceptor.getAccessControl(principal, false);
    boolean authorized = gemAccControl.authorizeOperation(null,
        new com.gemstone.gemfire.management.internal.security.AccessControlContext(role));
    return authorized;
  }

}
