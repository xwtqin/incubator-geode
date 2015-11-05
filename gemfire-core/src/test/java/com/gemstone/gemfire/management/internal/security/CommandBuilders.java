package com.gemstone.gemfire.management.internal.security;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.gemstone.gemfire.management.internal.cli.i18n.CliStrings;
import com.gemstone.gemfire.management.internal.cli.util.CommandStringBuilder;

public class CommandBuilders {

  protected static String SEC_USER_NAME = "security-user-name";
  protected static String SEC_USER_PWD = "security-user-pwd";

  protected static final int OP_ALTER_REGION = 1;
  protected static final int OP_ALTER_RUNTIME = 2;
  protected static final int OP_BACKUP_DISKSTORE = 3;
  protected static final int OP_CHANGE_ALERT_LEVEL = 4;
  protected static final int OP_CLOSE_DURABLE_CLIENT = 5;
  protected static final int OP_CLOSE_DURABLE_CQ = 6;
  protected static final int OP_COMPACT_DISKSTORE = 7;
  protected static final int OP_CONFIGURE_PDX = 8;
  protected static final int OP_CREATE_AEQ = 9;
  protected static final int OP_CREATE_DISKSTORE = 10;
  protected static final int OP_CREATE_GW_RECEIVER = 11;
  protected static final int OP_CREATE_GW_SENDER = 12;
  protected static final int OP_CREATE_INDEX = 13;
  protected static final int OP_CREATE_REGION = 14;
  protected static final int OP_DEPLOY = 15;
  protected static final int OP_DESTROY_DISKSTORE = 16;
  protected static final int OP_DESTROY_FUNCTION = 17;
  protected static final int OP_DESTROY_INDEX = 18;
  protected static final int OP_DESTROY_REGION = 19;
  protected static final int OP_EXECUTE_FUNCTION = 20;
  protected static final int OP_EXPORT_CONFIG = 21;
  protected static final int OP_EXPORT_DATA = 22;
  protected static final int OP_EXPORT_LOGS = 23;
  protected static final int OP_EXPORT_OFFLINE_DISKSTORE = 24;
  protected static final int OP_EXPORT_STACKTRACE = 25;
  protected static final int OP_GC = 26;
  protected static final int OP_GET = 27;
  protected static final int OP_IMPORT_CONFIG = 28;
  protected static final int OP_IMPORT_DATA = 29;
  protected static final int OP_LIST_DS = 30;
  protected static final int OP_LOAD_BALANCE_GW_SENDER = 31;
  protected static final int OP_LOCATE_ENTRY = 32;
  protected static final int OP_NETSTAT = 33;
  protected static final int OP_PAUSE_GW_SENDER = 34;
  protected static final int OP_PUT = 35;
  protected static final int OP_QUERY = 36;
  protected static final int OP_REBALANCE = 37;
  protected static final int OP_REMOVE = 38;
  protected static final int OP_RENAME_PDX = 39;
  protected static final int OP_RESUME_GW_SENDER = 40;
  protected static final int OP_REVOKE_MISSING_DISKSTORE = 41;
  protected static final int OP_SHOW_DEADLOCKS = 42;
  protected static final int OP_SHOW_LOG = 43;
  protected static final int OP_SHOW_METRICS = 44;
  protected static final int OP_SHOW_MISSING_DISKSTORES = 45;
  protected static final int OP_SHOW_SUBSCRIPTION_QUEUE_SIZE = 46;
  protected static final int OP_SHUTDOWN = 47;
  protected static final int OP_STOP_GW_RECEIVER = 48;
  protected static final int OP_STOP_GW_SENDER = 49;
  protected static final int OP_UNDEPLOY = 50;
  protected static final int OP_BACKUP_MEMBERS = 51;
  protected static final int OP_ROLL_DISKSTORE = 52;
  protected static final int OP_FORCE_COMPACTION = 53;
  protected static final int OP_FORCE_ROLL = 54;
  protected static final int OP_FLUSH_DISKSTORE = 55;
  protected static final int OP_START_GW_RECEIVER = 56;
  protected static final int OP_START_GW_SENDER = 57;
  protected static final int OP_BECOME_LOCK_GRANTOR = 58;
  protected static final int OP_START_MANAGER = 59;
  protected static final int OP_STOP_MANAGER = 60;
  protected static final int OP_CREATE_MANAGER = 61;
  protected static final int OP_STOP_CONTINUOUS_QUERY = 62;
  protected static final int OP_SET_DISK_USAGE = 63;

  protected static final int OP_PULSE_DASHBOARD = 92;
  protected static final int OP_PULSE_DATABROWSER = 93;
  protected static final int OP_PULSE_WEBGFSH = 94;
  protected static final int OP_PULSE_ADMIN_V1 = 95;

  protected static final int OP_DATA_READ = 96;
  protected static final int OP_DATA_WRITE = 97;
  protected static final int OP_MONITOR = 98;
  protected static final int OP_ADMIN = 99;
  
  private static String JTEST = System.getProperty("JTESTS");

  protected static Properties getInSecuredProperties(int authCode) {
    Properties props = new Properties();
    props.put(SEC_USER_NAME, "AUTHI_" + authCode);
    props.put(SEC_USER_PWD, "AUTHI_" + authCode);
    return props;
  }

  protected static Properties getSecuredAdminProperties(int authCode) {
    Properties props = new Properties();
    props.put(SEC_USER_NAME, "ADMIN_" + authCode);
    props.put(SEC_USER_PWD, "ADMIN_" + authCode);
    return props;
  }

  protected static Properties getUnAuthorizedProperties(int authCode) {
    Properties props = new Properties();
    props.put(SEC_USER_NAME, "UNAUTHC_" + authCode);
    props.put(SEC_USER_PWD, "UNAUTHC_" + authCode);
    return props;
  }

  public static String CREATE_INDEX() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.CREATE_INDEX);
    String regionName = "r1";
    csb.addOption(CliStrings.CREATE_INDEX__EXPRESSION, "key");
    csb.addOption(CliStrings.CREATE_INDEX__NAME, "test");
    csb.addOption(CliStrings.CREATE_INDEX__GROUP, "test");
    csb.addOption(CliStrings.CREATE_INDEX__REGION, "\"/" + regionName + " p\"");
    String commandString = csb.toString();
    return commandString;
  }

  public static String ALTER_REGION() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.CREATE_INDEX);
    String regionName = "r1";
    csb = new CommandStringBuilder(CliStrings.ALTER_REGION);
    csb.addOption(CliStrings.ALTER_REGION__REGION, regionName);
    csb.addOption(CliStrings.ALTER_REGION__GROUP, "g1");
    csb.addOption(CliStrings.ALTER_REGION__ENTRYEXPIRATIONTIMETOLIVE, "45635");
    csb.addOption(CliStrings.ALTER_REGION__ENTRYEXPIRATIONTTLACTION, "DESTROY");
    String commandString = csb.toString();
    return commandString;
  }

  public static String ALTER_RUNTIME() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.ALTER_RUNTIME_CONFIG);
    csb.addOption(CliStrings.ALTER_RUNTIME_CONFIG__MEMBER, "m1");
    csb.addOption(CliStrings.ALTER_RUNTIME_CONFIG__LOG__LEVEL, "info");
    csb.addOption(CliStrings.ALTER_RUNTIME_CONFIG__LOG__FILE__SIZE__LIMIT, "50");
    csb.addOption(CliStrings.ALTER_RUNTIME_CONFIG__ARCHIVE__DISK__SPACE__LIMIT, "32");
    csb.addOption(CliStrings.ALTER_RUNTIME_CONFIG__ARCHIVE__FILE__SIZE__LIMIT, "49");
    csb.addOption(CliStrings.ALTER_RUNTIME_CONFIG__STATISTIC__SAMPLE__RATE, "2000");
    csb.addOption(CliStrings.ALTER_RUNTIME_CONFIG__STATISTIC__ARCHIVE__FILE, "stat.gfs");
    csb.addOption(CliStrings.ALTER_RUNTIME_CONFIG__STATISTIC__SAMPLING__ENABLED, "true");
    csb.addOption(CliStrings.ALTER_RUNTIME_CONFIG__LOG__DISK__SPACE__LIMIT, "10");
    String commandString = csb.getCommandString();
    return commandString;
  }

  public static String CLOSE_DURABLE_CLIENTS() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.CLOSE_DURABLE_CLIENTS);
    csb.addOption(CliStrings.CLOSE_DURABLE_CLIENTS__CLIENT__ID, "c1");
    String commandString = csb.toString();
    return commandString;
  }
  
  public static String COUNT_DURABLE_CQ_EVENTS() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.COUNT_DURABLE_CQ_EVENTS);
    csb.addOption(CliStrings.COUNT_DURABLE_CQ_EVENTS__DURABLE__CLIENT__ID, "c1");
    String commandString = csb.toString();
    return commandString;
  }
  
  public static String CLOSE_DURABLE_CQS() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.CLOSE_DURABLE_CQS);
    csb.addOption(CliStrings.CLOSE_DURABLE_CQS__DURABLE__CLIENT__ID, "c1");
    csb.addOption(CliStrings.CLOSE_DURABLE_CQS__NAME, "cq1");
    String commandString = csb.toString();
    return commandString;
  }

  public static String CONFIGURE_PDX() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.CONFIGURE_PDX);
    csb.addOptionWithValueCheck(CliStrings.CONFIGURE_PDX__AUTO__SERIALIZER__CLASSES, "com.class");
    csb.addOptionWithValueCheck(CliStrings.CONFIGURE_PDX__IGNORE__UNREAD_FIELDS, "true");
    csb.addOptionWithValueCheck(CliStrings.CONFIGURE_PDX__PORTABLE__AUTO__SERIALIZER__CLASSES, "com.class");
    csb.addOptionWithValueCheck(CliStrings.CONFIGURE_PDX__READ__SERIALIZED, "true");
    String commandString = csb.toString();
    return commandString;
  }

  public static String CREATE_ASYNC_EVENT_QUEUE() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.CREATE_ASYNC_EVENT_QUEUE);
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__ID, "q1");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__GROUP, "Group1");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__BATCH_SIZE, "514");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__PERSISTENT, "true");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__DISK_STORE, "d1");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__MAXIMUM_QUEUE_MEMORY, "213");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__BATCHTIMEINTERVAL, "946");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__PARALLEL, "true");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__ENABLEBATCHCONFLATION, "true");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__DISPATCHERTHREADS, "2");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__ORDERPOLICY, "PARTITION");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__GATEWAYEVENTFILTER, "com.qcdunit.QueueCommandsDUnitTestHelper");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__SUBSTITUTION_FILTER,
        "com.qcdunit.QueueCommandsDUnitTestHelper");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__DISKSYNCHRONOUS, "false");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__LISTENER, "com.qcdunit.QueueCommandsDUnitTestHelper");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__LISTENER_PARAM_AND_VALUE, "param1");
    csb.addOption(CliStrings.CREATE_ASYNC_EVENT_QUEUE__LISTENER_PARAM_AND_VALUE, "param2#value2");
    String commandString = csb.toString();
    return commandString;
  }

  public static String CREATE_DISK_STORE() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.CREATE_DISK_STORE);
    csb.addOption(CliStrings.CREATE_DISK_STORE__NAME, "d1");
    csb.addOption(CliStrings.CREATE_DISK_STORE__GROUP, "g1");
    csb.addOption(CliStrings.CREATE_DISK_STORE__DIRECTORY_AND_SIZE, "/temp/temp");
    String commandString = csb.toString();
    return commandString;
  }

  public static String ALTER_DISK_STORE() {

    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.ALTER_DISK_STORE);
    csb.addOption(CliStrings.ALTER_DISK_STORE__DISKSTORENAME, "d1");
    csb.addOption(CliStrings.ALTER_DISK_STORE__REGIONNAME, "r1");
    csb.addOption(CliStrings.ALTER_DISK_STORE__DISKDIRS, "/temp/temp");
    csb.addOption(CliStrings.ALTER_DISK_STORE__CONCURRENCY__LEVEL, "5");
    csb.addOption(CliStrings.ALTER_DISK_STORE__INITIAL__CAPACITY, "6");
    csb.addOption(CliStrings.ALTER_DISK_STORE__LRU__EVICTION__ACTION, "local-destroy");
    csb.addOption(CliStrings.ALTER_DISK_STORE__COMPRESSOR, "com.gemstone.gemfire.compression.SnappyCompressor");
    csb.addOption(CliStrings.ALTER_DISK_STORE__STATISTICS__ENABLED, "true");

    return csb.getCommandString();
  }

  public static String BACKUP_DISK_STORE() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.BACKUP_DISK_STORE);
    csb.addOption(CliStrings.BACKUP_DISK_STORE__DISKDIRS, "/temp/temp");
    String commandString = csb.toString();
    return commandString;
  }

  public static String COMPACT_DISK_STORE() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.COMPACT_DISK_STORE);
    csb.addOption(CliStrings.COMPACT_DISK_STORE__NAME, "d1");
    csb.addOption(CliStrings.COMPACT_DISK_STORE__GROUP, "g1");
    String commandString = csb.toString();
    return commandString;
  }

  public static String DESCRIBE_DISK_STORE() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.DESCRIBE_DISK_STORE);
    csb.addOption(CliStrings.DESCRIBE_DISK_STORE__MEMBER, "m1");
    csb.addOption(CliStrings.DESCRIBE_DISK_STORE__NAME, "d1");
    String commandString = csb.toString();
    return commandString;
  }

  public static String DESTROY_DISK_STORE() {
    CommandStringBuilder commandStringBuilder = new CommandStringBuilder(CliStrings.DESTROY_DISK_STORE);
    commandStringBuilder.addOption(CliStrings.DESTROY_DISK_STORE__NAME, "d1");
    commandStringBuilder.addOption(CliStrings.DESTROY_DISK_STORE__GROUP, "g1");
    return commandStringBuilder.toString();
  }

  public static String LIST_DISK_STORE() {
    CommandStringBuilder commandStringBuilder = new CommandStringBuilder(CliStrings.LIST_DISK_STORE);
    return commandStringBuilder.toString();
  }

  public static String REVOKE_MISSING_DISK_STORE() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.REVOKE_MISSING_DISK_STORE);
    csb.addOption(CliStrings.REVOKE_MISSING_DISK_STORE__ID, "d1");
    return csb.toString();
  }

  public static String SHOW_MISSING_DISK_STORE() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.SHOW_MISSING_DISK_STORE);
    return csb.toString();
  }

  public static String CREATE_GATEWAYRECEIVER() {
    String command = CliStrings.CREATE_GATEWAYRECEIVER + " --" + CliStrings.CREATE_GATEWAYRECEIVER__BINDADDRESS
        + "=localhost" + " --" + CliStrings.CREATE_GATEWAYRECEIVER__STARTPORT + "=11000" + " --"
        + CliStrings.CREATE_GATEWAYRECEIVER__ENDPORT + "=10000" + " --"
        + CliStrings.CREATE_GATEWAYRECEIVER__MAXTIMEBETWEENPINGS + "=100000" + " --"
        + CliStrings.CREATE_GATEWAYRECEIVER__SOCKETBUFFERSIZE + "=512000";

    return command;
  }

  public static String CREATE_GATEWAYSENDER() {
    String command = CliStrings.CREATE_GATEWAYSENDER + " --" + CliStrings.CREATE_GATEWAYSENDER__ID + "=ln" + " --"
        + CliStrings.CREATE_GATEWAYSENDER__REMOTEDISTRIBUTEDSYSTEMID + "=2" + " --"
        + CliStrings.CREATE_GATEWAYSENDER__PARALLEL + "=false" + " --" + CliStrings.CREATE_GATEWAYSENDER__MANUALSTART
        + "=true" + " --" + CliStrings.CREATE_GATEWAYSENDER__SOCKETBUFFERSIZE + "=1000" + " --"
        + CliStrings.CREATE_GATEWAYSENDER__SOCKETREADTIMEOUT + "=" + 1000 + " --"
        + CliStrings.CREATE_GATEWAYSENDER__ENABLEBATCHCONFLATION + "=true" + " --"
        + CliStrings.CREATE_GATEWAYSENDER__BATCHSIZE + "=1000" + " --"
        + CliStrings.CREATE_GATEWAYSENDER__BATCHTIMEINTERVAL + "=5000" + " --"
        + CliStrings.CREATE_GATEWAYSENDER__ENABLEPERSISTENCE + "=true" + " --"
        + CliStrings.CREATE_GATEWAYSENDER__DISKSYNCHRONOUS + "=false" + " --"
        + CliStrings.CREATE_GATEWAYSENDER__MAXQUEUEMEMORY + "=1000" + " --"
        + CliStrings.CREATE_GATEWAYSENDER__ALERTTHRESHOLD + "=100" + " --"
        + CliStrings.CREATE_GATEWAYSENDER__DISPATCHERTHREADS + "=2";
    return command;
  }

  public static String CREATE_REGION() {
    CommandStringBuilder commandStringBuilder = new CommandStringBuilder(CliStrings.CREATE_REGION);
    commandStringBuilder.addOption(CliStrings.CREATE_REGION__REGION, "R1");
    commandStringBuilder.addOption(CliStrings.CREATE_REGION__REGIONSHORTCUT, "REPLICATE");
    commandStringBuilder.addOption(CliStrings.CREATE_REGION__STATISTICSENABLED, "true");
    commandStringBuilder.addOption(CliStrings.CREATE_REGION__GROUP, "G1");
    return commandStringBuilder.toString();
  }

  public static String DEPLOY() throws IOException {
    String testJarPath =  createTempFile("testjar",".jar");
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.DEPLOY);
    csb.addOption(CliStrings.DEPLOY__JAR, testJarPath);
    return csb.toString();
  }

  public static String LIST_DEPLOYED() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LIST_DEPLOYED);
    return csb.toString();
  }

  public static String DESTROY_FUNCTION() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.DESTROY_FUNCTION);
    csb.addOption(CliStrings.DESTROY_FUNCTION__ID, "fn1");
    csb.addOption(CliStrings.DESTROY_FUNCTION__ONGROUPS, "g1");
    return csb.toString();
  }

  public static String DESTROY_INDEX() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.DESTROY_INDEX);
    csb.addOption(CliStrings.DESTROY_INDEX__NAME, "idx1");
    csb.addOption(CliStrings.DESTROY_INDEX__REGION, "/StocksParReg");
    return csb.toString();
  }

  public static String DESTROY_REGION() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.DESTROY_REGION);
    csb.addOption(CliStrings.DESTROY_REGION__REGION, "compressedRegion");
    return csb.toString();
  }

  public static String EXECUTE_FUNCTION() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.EXECUTE_FUNCTION);
    csb.addOption(CliStrings.EXECUTE_FUNCTION__ID, "fn1");
    csb.addOption(CliStrings.EXECUTE_FUNCTION__ONGROUPS, "g1");
    return csb.toString();
  }

  public static String EXPORT_CONFIG() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.EXPORT_CONFIG);
    csb.addOption(CliStrings.EXPORT_CONFIG__MEMBER, "m1");
    return csb.toString();
  }

  public static String EXPORT_DATA() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.EXPORT_DATA);
    csb.addOption(CliStrings.EXPORT_DATA__REGION, "R1");
    csb.addOption(CliStrings.EXPORT_DATA__MEMBER, "Manager");
    csb.addOption(CliStrings.EXPORT_DATA__FILE, "/temp/temp");
    return csb.toString();
  }

  public static String EXPORT_LOGS() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.EXPORT_LOGS);
    csb.addOption(CliStrings.EXPORT_LOGS__DIR, "/temp/temp");
    csb.addOption(CliStrings.EXPORT_LOGS__MEMBER, "m1");
    return csb.toString();
  }

  public static String EXPORT_STACKTRACE() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.EXPORT_STACKTRACE);
    csb.addOption(CliStrings.EXPORT_STACKTRACE__FILE, "/temp/temp");
    return csb.toString();
  }

  public static String GC() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.GC);
    csb.addOption(CliStrings.GC__MEMBER, "m1");
    return csb.toString();
  }

  public static String IMPORT_CONFIG() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.IMPORT_SHARED_CONFIG);
    csb.addOption(CliStrings.IMPORT_SHARED_CONFIG__ZIP, "test.zip");
    return csb.toString();
  }

  public static String IMPORT_DATA() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.IMPORT_DATA);
    csb.addOption(CliStrings.IMPORT_DATA__REGION, "R1");
    csb.addOption(CliStrings.IMPORT_DATA__FILE, "/temp/temp");
    csb.addOption(CliStrings.IMPORT_DATA__MEMBER, "Manager");
    return csb.toString();
  }

  public static String LOAD_BALANCE_GW_SENDER() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LOAD_BALANCE_GATEWAYSENDER);
    csb.addOption(CliStrings.LOAD_BALANCE_GATEWAYSENDER__ID, "1");
    return csb.toString();
  }

  public static String LOCATE_ENTRY() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LOCATE_ENTRY);
    csb.addOption(CliStrings.LOCATE_ENTRY__KEY, "1");
    csb.addOption(CliStrings.LOCATE_ENTRY__REGIONNAME, "R1");
    return csb.toString();
  }

  public static String NETSTAT() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.NETSTAT);
    csb.addOption(CliStrings.NETSTAT__MEMBER, "m1");
    csb.addOption(CliStrings.NETSTAT__FILE, "/temp/f1");
    return csb.toString();
  }

  public static String PAUSE_GATEWAYSENDER() {
    String command = CliStrings.PAUSE_GATEWAYSENDER + " --" + CliStrings.PAUSE_GATEWAYSENDER__ID + "=ln --"
        + CliStrings.PAUSE_GATEWAYSENDER__MEMBER + "=" + "m1" + " --" + CliStrings.PAUSE_GATEWAYSENDER__GROUP
        + "=SenderGroup1";

    return command;
  }

  public static String REBALANCE() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.REBALANCE);
    csb.addOption(CliStrings.REBALANCE__INCLUDEREGION, "r1");
    return csb.toString();
  }

  public static String PDX_RENAME() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.PDX_RENAME);
    csb.addOption(CliStrings.PDX_RENAME_OLD, "com.old");
    csb.addOption(CliStrings.PDX_RENAME_NEW, "com.new");
    csb.addOption(CliStrings.PDX_DISKSTORE, "d1");
    csb.addOption(CliStrings.PDX_DISKDIR, "/temp");
    return csb.toString();
  }

  public static String RESUME_GATEWAYSENDER() {
    String command = CliStrings.RESUME_GATEWAYSENDER + " --" + CliStrings.RESUME_GATEWAYSENDER__ID + "=ln";
    return command;
  }

  public static String SHOW_DEADLOCK() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.SHOW_DEADLOCK);
    csb.addOption(CliStrings.SHOW_DEADLOCK__DEPENDENCIES__FILE, "f1");
    return csb.toString();
  }

  public static String SHOW_LOG() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.SHOW_LOG);
    csb.addOption(CliStrings.SHOW_LOG_MEMBER, "m1");
    return csb.toString();
  }

  public static String SHOW_METRICS() {
    String command = CliStrings.SHOW_METRICS + " --" + CliStrings.SHOW_METRICS__MEMBER + "=" + "m1" + " --"
        + CliStrings.SHOW_METRICS__CACHESERVER__PORT + "=" + 2099 + " --" + CliStrings.SHOW_METRICS__FILE + "=" + "F1";
    return command;
  }

  public static String STOP_GATEWAYRECEIVER() {
    String command = CliStrings.STOP_GATEWAYRECEIVER + " --" + CliStrings.STOP_GATEWAYRECEIVER__MEMBER + "=" + "m1"
        + " --" + CliStrings.STOP_GATEWAYRECEIVER__GROUP + "=RG1";
    return command;
  }

  public static String STOP_GATEWAYSENDER() {
    String command = CliStrings.STOP_GATEWAYSENDER + " --" + CliStrings.STOP_GATEWAYSENDER__ID + "=ln --"
        + CliStrings.STOP_GATEWAYSENDER__MEMBER + "=" + "m1" + " --" + CliStrings.STOP_GATEWAYSENDER__GROUP
        + "=SenderGroup1";
    return command;
  }

  public static String UNDEPLOY() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.UNDEPLOY);
    csb.addOption(CliStrings.UNDEPLOY__JAR, "test.jar");
    return csb.toString();
  }

  public static String CHANGE_LOGLEVEL() {
    String command = CliStrings.CHANGE_LOGLEVEL + " --" + CliStrings.CHANGE_LOGLEVEL__LOGLEVEL + "=finer" + " --"
        + CliStrings.CHANGE_LOGLEVEL__GROUPS + "=" + "grp1" + "," + "grp2";
    return command;
  }

  public static String DEFINE_INDEX() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.DEFINE_INDEX);
    csb.addOption(CliStrings.CREATE_INDEX__NAME, "indexName");
    csb.addOption(CliStrings.CREATE_INDEX__EXPRESSION, "key");
    csb.addOption(CliStrings.CREATE_INDEX__REGION, "/StocksParReg");
    return csb.toString();
  }

  public static String CLEAR_DEFINED_INDEXES() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.CLEAR_DEFINED_INDEXES);
    return csb.toString();
  }

  public static String DESCRIBE_CONFIG() {
    String command = CliStrings.DESCRIBE_CONFIG + " --member=m1";
    return command;
  }

  public static String EXPORT_SHARED_CONFIG() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.EXPORT_SHARED_CONFIG);
    csb.addOption(CliStrings.EXPORT_SHARED_CONFIG__FILE, "test.zip");
    return csb.toString();
  }

  public static String IMPORT_SHARED_CONFIG() throws IOException {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.IMPORT_SHARED_CONFIG);
    
    String zipPath = createTempFile("testzip",".zip");
    csb.addOption(CliStrings.IMPORT_SHARED_CONFIG__ZIP, zipPath);
    return csb.toString();
  }
  
  private static String createTempFile(String fileName, String suffix) throws IOException {
    String tempFilePath;

    File current = new java.io.File(".");
    File tempFile = File.createTempFile(fileName, suffix, current);
    tempFile.deleteOnExit();
    tempFilePath = tempFile.getCanonicalPath();

    return tempFilePath;
  }

  public static String STATUS_SHARED_CONFIG() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.STATUS_SHARED_CONFIG);
    return csb.toString();
  }

  public static String LIST_DURABLE_CQS() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LIST_DURABLE_CQS);
    csb.addOption(CliStrings.LIST_DURABLE_CQS__DURABLECLIENTID, "c1");
    return csb.toString();
  }

  public static String LIST_FUNCTION() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LIST_FUNCTION);
    return csb.toString();
  }

  public static String LIST_ASYNC_EVENT_QUEUES() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LIST_ASYNC_EVENT_QUEUES);
    return csb.toString();
  }

  public static String LIST_GATEWAY() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LIST_GATEWAY);
    return csb.toString();
  }

  public static String START_GATEWAYRECEIVER() {
    String command = CliStrings.START_GATEWAYRECEIVER + " --" + CliStrings.START_GATEWAYRECEIVER__MEMBER + "=" + "m1"
        + " --" + CliStrings.START_GATEWAYRECEIVER__GROUP + "=RG1";
    return command;
  }

  public static String START_GATEWAYSENDER() {
    String command = CliStrings.START_GATEWAYSENDER + " --" + CliStrings.START_GATEWAYSENDER__ID + "=ln";
    return command;
  }

  public static String STATUS_GATEWAYRECEIVER() {
    String command = CliStrings.STATUS_GATEWAYRECEIVER + " --" + CliStrings.STATUS_GATEWAYRECEIVER__GROUP + "=RG1";
    return command;
  }

  public static String STATUS_GATEWAYSENDER() {
    String command = CliStrings.STATUS_GATEWAYSENDER + " --" + CliStrings.STATUS_GATEWAYSENDER__ID + "=ln_Serial --"
        + CliStrings.STATUS_GATEWAYSENDER__GROUP + "=Serial_Sender";
    return command;
  }

  public static String DESCRIBE_CLIENT() {
    String command = CliStrings.DESCRIBE_CLIENT + " --" + CliStrings.DESCRIBE_CLIENT__ID + "=\"" + 1 + "\"";
    return command;
  }

  public static String DESCRIBE_MEMBER() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.DESCRIBE_MEMBER);
    csb.addOption(CliStrings.DESCRIBE_MEMBER__IDENTIFIER, "m1");
    return csb.toString();
  }

  public static String LIST_CLIENTS() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LIST_CLIENTS);
    return csb.toString();
  }

  public static String LIST_MEMBER() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LIST_MEMBER);
    return csb.toString();
  }

  public static String CREATE_DEFINED_INDEXES() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.CREATE_DEFINED_INDEXES);
    return csb.toString();
  }

  public static String LIST_INDEX() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LIST_INDEX);
    return csb.toString();
  }

  public static String DESCRIBE_REGION() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.DESCRIBE_REGION);
    csb.addOption(CliStrings.DESCRIBE_REGION__NAME, "r1");
    return csb.toString();
  }

  public static String LIST_REGION() {
    CommandStringBuilder csb = new CommandStringBuilder(CliStrings.LIST_REGION);
    csb.addOption(CliStrings.LIST_REGION__MEMBER, "m1");
    return csb.toString();
  }
  
  public static String GET() {
    CommandStringBuilder command = new CommandStringBuilder(CliStrings.GET);
    command.addOption(CliStrings.GET__REGIONNAME, "r1");
    command.addOption(CliStrings.GET__KEY, "jondoe");
    command.addOption(CliStrings.GET__LOAD, "true");
    return command.toString();
  }

  public static String PUT() {
    String command = CliStrings.PUT + " --" + CliStrings.PUT__KEY + "=k1" + " --" + CliStrings.PUT__VALUE + "=k1"
        + " --" + CliStrings.PUT__REGIONNAME + "=R1";
    return command;
  }
  
  public static String QUERY() {
    String command = CliStrings.QUERY + " --" + CliStrings.QUERY__QUERY + "='select a from /r1'";
    return command;
  }
  
  public static String REMOVE() {
    CommandStringBuilder command = new CommandStringBuilder(CliStrings.REMOVE);
    command.addOption(CliStrings.REMOVE__KEY, "jondoe");
    command.addOption(CliStrings.REMOVE__REGION, "r1");
    return command.toString();
  }
}
