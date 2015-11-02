package com.vmware.gemfire.tools.pulse.tests;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

/**
 * 
 * @author tushark
 *
 */
public class AccessControl extends JMXBaseBean implements AccessControlMBean {

  public static final String OBJECT_NAME_ACCESSCONTROL = "GemFire:service=AccessControl,type=Distributed";

  @Override
  public boolean authorize(String role) {
    AccessControlContext acc = AccessController.getContext();
    Subject subject = Subject.getSubject(acc);
    Set<JMXPrincipal> principals = subject.getPrincipals(JMXPrincipal.class);    
    if (principals == null || principals.isEmpty()) {
      throw new SecurityException("Access denied");
    }
    
    Principal principal = principals.iterator().next();    
    String roleArray[] = getStringArray(principal.getName());
    if(roleArray!=null) {
      for(String roleStr:roleArray) {
        if(roleStr.equals(role))
          return true;
      }
    }    
    return false;
  }

  @Override
  protected String getKey(String propName) {    
    return "users." + propName + "." + "roles";
  }

}
