package com.vmware.gemfire.tools.pulse.tests;

import java.util.Collections;

import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

/**
 * 
 * @author tushark
 *
 */
public class PropsBackedAuthenticator extends JMXBaseBean implements JMXAuthenticator {

  @Override
  public Subject authenticate(Object credentials) {
    String username = null, password = null;
    if (credentials instanceof String[]) {
      final String[] aCredentials = (String[]) credentials;
      username = (String) aCredentials[0];
      password = (String) aCredentials[1];
      System.out.println("#intSec User="+ username + " password="+password);
      String users[] = getStringArray("users");
      for(String u : users) {
        if(username.equals(u)) {          
          String storedpassword = getString("users."+u+".password");
          System.out.println("#intSec PropUser="+ u + " PropPassword="+storedpassword);
          if(storedpassword!=null && storedpassword.equals(password)) {
            return new Subject(true, Collections.singleton(new JMXPrincipal(username)), Collections.EMPTY_SET,
                Collections.EMPTY_SET);
          } else {
            throw new SecurityException("Authentication Failed 1");
          }
        }
      }
    } else {
      throw new SecurityException("Credentials Missing");
    }
    throw new SecurityException("Authentication Failed 2");
  }

  @Override
  protected String getKey(String propName) {    
    return propName;
  }

}
