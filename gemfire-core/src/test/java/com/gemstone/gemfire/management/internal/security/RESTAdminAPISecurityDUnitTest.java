package com.gemstone.gemfire.management.internal.security;

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
  public void preTearDownCacheTestCase() throws Exception {
    this.setUseHttpOnConnect(false);
  }

}
