package com.gemstone.gemfire.management.internal.security;

import java.util.Properties;

import com.gemstone.gemfire.management.internal.security.CLISecurityDUnitTest.Assertor;

public class RESTAdminAPIUnAuthDUnitTest extends RESTAdminAPISecurityDUnitTest{
  
  public RESTAdminAPIUnAuthDUnitTest(String name) {
    super(name);
    // TODO Auto-generated constructor stub
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected Properties getSecuredProperties(int authCode) {
    Properties props = new Properties();
    props.put(CommandBuilders.SEC_USER_NAME, "UNAUTHC_" + authCode);
    props.put(CommandBuilders.SEC_USER_PWD, "UNAUTHC_" + authCode);
    return props;
  }
  
  protected Assertor getAssertor(){
    return new Assertor(ACCESS_DENIED);
  }

}
