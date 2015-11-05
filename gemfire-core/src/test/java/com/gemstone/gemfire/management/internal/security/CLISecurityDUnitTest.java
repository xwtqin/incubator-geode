package com.gemstone.gemfire.management.internal.security;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import com.gemstone.gemfire.distributed.internal.DistributionConfig;

public class CLISecurityDUnitTest extends CommandTestBase {

  private static final long serialVersionUID = 1L;

  public static final String ACCESS_DENIED = "Access Denied";

  protected File tempSecFile;

  protected String tempFilePath;

  public CLISecurityDUnitTest(String name) {
    super(name);

  }

  protected void writeToLog(String text, String resultAsString) {
    getLogWriter().info(testName + "\n");
    getLogWriter().info(resultAsString);
  }

  public void setUp() throws Exception {
    super.setUp();
    createTempFile();
  }

  @Override
  public void tearDown2() throws Exception {
    deleteTempFile();
    super.tearDown2();
  }

  private void createTempFile() {

    try {
      File current = new java.io.File(".");
      tempSecFile = File.createTempFile("gemfire", "sec", current);
      tempSecFile.deleteOnExit();
      tempFilePath = tempSecFile.getCanonicalPath();
    } catch (IOException e) {
      fail("could not create temp file " + e);
    }
  }

  private void deleteTempFile() {

    try {
      tempSecFile.delete();
    } catch (Exception e) {
      fail("could not delete temp file " + e);
    }
  }

  protected void writeToFile(Properties props) {

    try {

      FileWriter fw = new FileWriter(tempSecFile, true);
      Enumeration en = props.keys();
      while (en.hasMoreElements()) {
        String key = (String) en.nextElement();
        String val = props.getProperty(key);
        String line = key + "=" + val;
        fw.append(line);
        fw.append("\n");
      }
      fw.flush();

    } catch (IOException x) {
      fail("could not write to temp file " + x);
    }

  }

  public class Assertor {

    private String errString;

    public Assertor() {
      this.errString = null;
    }

    public Assertor(String errString) {
      this.errString = errString;
    }

    public void assertTest() {
      boolean hasErr = getDefaultShell().hasError();
      // getLogWriter().info(testName + "hasErr = " +hasErr);
      if (hasErr) {
        String error = getDefaultShell().getError();
        if (errString != null) {
          assertTrue(error.contains(errString));
        } else {
          fail("Command should have passed but failed with error = " + error);
        }

      } else {
        if (errString != null) {
          fail("Command should have failed with error " + errString + " but it passed");
        }
      }

    }
  }

  protected void createDefaultSetup(Properties props, String propertyFile) {
    this.securityFile = propertyFile;
    createDefaultSetup(props);
  }

  private void securityCheckForCommand(String command, String propertyFile, Assertor assertor) {
    Properties props = new Properties();
    props.setProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME,
        "com.gemstone.gemfire.management.internal.security.CustomAuthenticator.create");
    props.setProperty(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME,
        "com.gemstone.gemfire.management.internal.security.CustomAccessControl.create");
    createDefaultSetup(props, propertyFile);
    try {
      executeCommandWithoutClear(command);
      assertor.assertTest();
    } catch (Exception e) {
      fail("Test failed with exception " + e);
    } finally {
      getDefaultShell().clearEvents();
      destroyDefaultSetup();
    }

  }

  protected Properties getSecuredProperties(int authCode) {
    Properties props = new Properties();
    props.put(CommandBuilders.SEC_USER_NAME, "AUTHC_" + authCode);
    props.put(CommandBuilders.SEC_USER_PWD, "AUTHC_" + authCode);
    return props;
  }

  protected Assertor getAssertor() {
    return new Assertor();
  }

  /**
   * The below test is to test the framework for proper error.
   */

  /*
   * public void _testCreateIndexParentOP() { String commandString =
   * CommandBuilders.CREATE_INDEX();
   * writeToFile(CommandBuilders.getSecuredAdminProperties
   * (CommandBuilders.OP_CREATE_INDEX)); securityCheckForCommand(commandString,
   * tempFilePath, getAssertor()); }
   */

  public void test_ALTER_RUNTIME() {
    String commandString = CommandBuilders.ALTER_RUNTIME();
    writeToFile(getSecuredProperties(CommandBuilders.OP_ALTER_RUNTIME));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  
  public void test_CHANGE_LOGLEVEL() {
    String commandString = CommandBuilders.CHANGE_LOGLEVEL();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CHANGE_ALERT_LEVEL));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  //This test is disabled becoz only access level required for this command is LIST_DS
  //which is the lowest access level
  //ant test marked with _test is not really required here
  public void _test_DESCRIBE_CONFIG() {
    String commandString = CommandBuilders.DESCRIBE_CONFIG();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_EXPORT_CONFIG() {
    String commandString = CommandBuilders.EXPORT_CONFIG();
    writeToFile(getSecuredProperties(CommandBuilders.OP_EXPORT_CONFIG));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_EXPORT_SHARED_CONFIG() {
    String commandString = CommandBuilders.EXPORT_SHARED_CONFIG();
    writeToFile(getSecuredProperties(CommandBuilders.OP_EXPORT_CONFIG));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_IMPORT_SHARED_CONFIG() throws IOException {

    String commandString = CommandBuilders.IMPORT_SHARED_CONFIG();
    writeToFile(getSecuredProperties(CommandBuilders.OP_IMPORT_CONFIG));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_STATUS_SHARED_CONFIG() {
    String commandString = CommandBuilders.STATUS_SHARED_CONFIG();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_EXPORT_DATA() {
    String commandString = CommandBuilders.EXPORT_DATA();
    writeToFile(getSecuredProperties(CommandBuilders.OP_EXPORT_DATA));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }
  
  public void test_GET() {
    String commandString = CommandBuilders.GET();
    writeToFile(getSecuredProperties(CommandBuilders.OP_GET));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_IMPORT_DATA() {
    String commandString = CommandBuilders.IMPORT_DATA();
    writeToFile(getSecuredProperties(CommandBuilders.OP_IMPORT_DATA));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }
  
  public void test_PUT() {
    String commandString = CommandBuilders.PUT();
    writeToFile(getSecuredProperties(CommandBuilders.OP_PUT));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }
  
  public void test_QUERY(){
    String commandString = CommandBuilders.QUERY();
    writeToFile(getSecuredProperties(CommandBuilders.OP_QUERY));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_REMOVE(){
    String commandString = CommandBuilders.REMOVE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_REMOVE));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }
  
  public void test_LOCATE_ENTRY() {
    String commandString = CommandBuilders.LOCATE_ENTRY();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LOCATE_ENTRY));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_DEPLOY() throws IOException {
    String commandString = CommandBuilders.DEPLOY();
    writeToFile(getSecuredProperties(CommandBuilders.OP_DEPLOY));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_LIST_DEPLOYED() {
    String commandString = CommandBuilders.LIST_DEPLOYED();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_UNDEPLOY() {
    String commandString = CommandBuilders.UNDEPLOY();
    writeToFile(getSecuredProperties(CommandBuilders.OP_UNDEPLOY));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void ISSUE_NO_OP_CODE_test_ALTER_DISK_STORE() {
    String commandString = CommandBuilders.ALTER_DISK_STORE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_UNDEPLOY));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_BACKUP_DISK_STORE() {
    String commandString = CommandBuilders.BACKUP_DISK_STORE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_BACKUP_DISKSTORE));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_COMPACT_DISKSTORE() {
    String commandString = CommandBuilders.COMPACT_DISK_STORE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_COMPACT_DISKSTORE));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_CREATE_DISK_STORE() {
    String commandString = CommandBuilders.CREATE_DISK_STORE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CREATE_DISKSTORE));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_DESCRIBE_DISK_STORE() {
    String commandString = CommandBuilders.DESCRIBE_DISK_STORE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_DESTROY_DISK_STORE() {
    String commandString = CommandBuilders.DESTROY_DISK_STORE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_DESTROY_DISKSTORE));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_LIST_DISK_STORE() {
    String commandString = CommandBuilders.LIST_DISK_STORE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_REVOKE_MISSING_DISK_STORE() {
    String commandString = CommandBuilders.REVOKE_MISSING_DISK_STORE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_REVOKE_MISSING_DISKSTORE));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void ISSUE_test_SHOW_MISSING_DISK_STORE() {
    String commandString = CommandBuilders.SHOW_MISSING_DISK_STORE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_SHOW_MISSING_DISKSTORES));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_LIST_DURABLE_CQS() {
    String commandString = CommandBuilders.LIST_DURABLE_CQS();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_CLOSE_DURABLE_CQS() {
    String commandString = CommandBuilders.CLOSE_DURABLE_CQS();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CLOSE_DURABLE_CQ));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }
  
  public void ISSUE_test_COUNT_DURABLE_CQ_EVENTS() {
    String commandString = CommandBuilders.COUNT_DURABLE_CQ_EVENTS();
    writeToFile(getSecuredProperties(CommandBuilders.OP_SHOW_SUBSCRIPTION_QUEUE_SIZE));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }
  
  public void test_CLOSE_DURABLE_CLIENTS() {
    String commandString = CommandBuilders.CLOSE_DURABLE_CLIENTS();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CLOSE_DURABLE_CLIENT));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_DESTROY_FUNCTION() {
    String commandString = CommandBuilders.DESTROY_FUNCTION();
    writeToFile(getSecuredProperties(CommandBuilders.OP_DESTROY_FUNCTION));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_EXECUTE_FUNCTION() {
    String commandString = CommandBuilders.EXECUTE_FUNCTION();
    writeToFile(getSecuredProperties(CommandBuilders.OP_EXECUTE_FUNCTION));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_LIST_FUNCTION() {
    String commandString = CommandBuilders.LIST_FUNCTION();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_CREATE_ASYNC_EVENT_QUEUE() {
    String commandString = CommandBuilders.CREATE_ASYNC_EVENT_QUEUE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CREATE_AEQ));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_CREATE_GATEWAYRECEIVER() {
    String commandString = CommandBuilders.CREATE_GATEWAYRECEIVER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CREATE_GW_RECEIVER));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_CREATE_GATEWAYSENDER() {
    String commandString = CommandBuilders.CREATE_GATEWAYSENDER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CREATE_GW_SENDER));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_LIST_ASYNC_EVENT_QUEUES() {
    String commandString = CommandBuilders.LIST_ASYNC_EVENT_QUEUES();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_LIST_GATEWAY() {
    String commandString = CommandBuilders.LIST_GATEWAY();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_LOAD_BALANCE_GW_SENDER() {
    String commandString = CommandBuilders.LOAD_BALANCE_GW_SENDER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LOAD_BALANCE_GW_SENDER));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_PAUSE_GATEWAYSENDER() {
    String commandString = CommandBuilders.PAUSE_GATEWAYSENDER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_PAUSE_GW_SENDER));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_RESUME_GATEWAYSENDER() {
    String commandString = CommandBuilders.RESUME_GATEWAYSENDER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_RESUME_GW_SENDER));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_START_GATEWAYRECEIVER() {
    String commandString = CommandBuilders.START_GATEWAYRECEIVER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_START_GW_RECEIVER));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_START_GATEWAYSENDER() {
    String commandString = CommandBuilders.START_GATEWAYSENDER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_START_GW_SENDER));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_STATUS_GATEWAYSENDER() {
    String commandString = CommandBuilders.STATUS_GATEWAYSENDER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_STATUS_GATEWAYRECEIVER() {
    String commandString = CommandBuilders.STATUS_GATEWAYRECEIVER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_STOP_GATEWAYRECEIVER() {
    String commandString = CommandBuilders.STOP_GATEWAYRECEIVER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_STOP_GW_RECEIVER));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_STOP_GATEWAYSENDER() {
    String commandString = CommandBuilders.STOP_GATEWAYSENDER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_STOP_GW_SENDER));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_DESCRIBE_CLIENT() {
    String commandString = CommandBuilders.DESCRIBE_CLIENT();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_DESCRIBE_MEMBER() {
    String commandString = CommandBuilders.DESCRIBE_MEMBER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_EXPORT_LOGS() {
    String commandString = CommandBuilders.EXPORT_LOGS();
    writeToFile(getSecuredProperties(CommandBuilders.OP_EXPORT_LOGS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_EXPORT_STACKTRACE() {
    String commandString = CommandBuilders.EXPORT_STACKTRACE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_EXPORT_STACKTRACE));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_GC() {
    String commandString = CommandBuilders.GC();
    writeToFile(getSecuredProperties(CommandBuilders.OP_GC));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_LIST_CLIENTS() {
    String commandString = CommandBuilders.LIST_CLIENTS();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_LIST_MEMBER() {
    String commandString = CommandBuilders.LIST_MEMBER();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_NETSTAT() {
    String commandString = CommandBuilders.NETSTAT();
    writeToFile(getSecuredProperties(CommandBuilders.OP_NETSTAT));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_SHOW_DEADLOCK() {
    String commandString = CommandBuilders.SHOW_DEADLOCK();
    writeToFile(getSecuredProperties(CommandBuilders.OP_SHOW_DEADLOCKS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_SHOW_LOG() {
    String commandString = CommandBuilders.SHOW_LOG();
    writeToFile(getSecuredProperties(CommandBuilders.OP_SHOW_LOG));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_SHOW_METRICS() {
    String commandString = CommandBuilders.SHOW_METRICS();
    writeToFile(getSecuredProperties(CommandBuilders.OP_SHOW_METRICS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_CLEAR_DEFINED_INDEXES() {
    String commandString = CommandBuilders.CLEAR_DEFINED_INDEXES();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CREATE_INDEX));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_CREATE_DEFINED_INDEXES() {
    String commandString = CommandBuilders.CREATE_DEFINED_INDEXES();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CREATE_INDEX));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_CREATE_INDEX() {
    String commandString = CommandBuilders.CREATE_INDEX();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CREATE_INDEX));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_DEFINE_INDEX() {
    String commandString = CommandBuilders.DEFINE_INDEX();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CREATE_INDEX));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_DESTROY_INDEX() {
    String commandString = CommandBuilders.DESTROY_INDEX();
    writeToFile(getSecuredProperties(CommandBuilders.OP_DESTROY_INDEX));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_LIST_INDEX() {
    String commandString = CommandBuilders.LIST_INDEX();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_CONFIGURE_PDX() {
    String commandString = CommandBuilders.CONFIGURE_PDX();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CONFIGURE_PDX));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_ALTER_REGION() {
    String commandString = CommandBuilders.ALTER_REGION();
    writeToFile(getSecuredProperties(CommandBuilders.OP_ALTER_REGION));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_CREATE_REGION() {
    String commandString = CommandBuilders.CREATE_REGION();
    writeToFile(getSecuredProperties(CommandBuilders.OP_CREATE_REGION));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_DESCRIBE_REGION() {
    String commandString = CommandBuilders.DESCRIBE_REGION();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_DESTROY_REGION() {
    String commandString = CommandBuilders.DESTROY_REGION();
    writeToFile(getSecuredProperties(CommandBuilders.OP_DESTROY_REGION));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void _test_LIST_REGION() {
    String commandString = CommandBuilders.LIST_REGION();
    writeToFile(getSecuredProperties(CommandBuilders.OP_LIST_DS));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

  public void test_REBALANCE() {
    String commandString = CommandBuilders.REBALANCE();
    writeToFile(getSecuredProperties(CommandBuilders.OP_REBALANCE));
    securityCheckForCommand(commandString, tempFilePath, getAssertor());
  }

}
