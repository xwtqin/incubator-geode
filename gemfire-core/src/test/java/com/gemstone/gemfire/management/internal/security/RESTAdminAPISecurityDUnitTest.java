package com.gemstone.gemfire.management.internal.security;

import dunit.Host;
import dunit.SerializableCallable;

public class RESTAdminAPISecurityDUnitTest extends CLISecurityDUnitTest {

  private static final long serialVersionUID = 1L;

  public RESTAdminAPISecurityDUnitTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    super.setUp();
    this.setUseHttpOnConnect(true);
  }


  @Override
  public void tearDown2() throws Exception {
    super.tearDown2();
    this.setUseHttpOnConnect(false);
  }

}
