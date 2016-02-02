package com.gemstone.gemfire.test.dunit;

import java.util.Properties;

import org.apache.logging.log4j.Logger;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.DistributionConfigImpl;
import com.gemstone.gemfire.internal.logging.InternalLogWriter;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.internal.logging.LogWriterFactory;
import com.gemstone.gemfire.internal.logging.ManagerLogWriter;
import com.gemstone.gemfire.internal.logging.log4j.LogWriterLogger;

public class LogWriterSupport {

  private static final Logger logger = LogService.getLogger();
  private static final LogWriterLogger oldLogger = LogWriterLogger.create(logger);
  
  /**
   * Returns a <code>LogWriter</code> for logging information
   * @deprecated Use a static logger from the log4j2 LogService.getLogger instead.
   */
  @Deprecated
  public static InternalLogWriter getLogWriter() {
    return LogWriterSupport.oldLogger;
  }

  /**
   * Creates a new LogWriter and adds it to the config properties. The config
   * can then be used to connect to DistributedSystem, thus providing early
   * access to the LogWriter before connecting. This call does not connect
   * to the DistributedSystem. It simply creates and returns the LogWriter
   * that will eventually be used by the DistributedSystem that connects using
   * config.
   * 
   * @param config the DistributedSystem config properties to add LogWriter to
   * @return early access to the DistributedSystem LogWriter
   */
  public static LogWriter createLogWriter(Properties config) { // TODO:LOG:CONVERT: this is being used for ExpectedExceptions
    Properties nonDefault = config;
    if (nonDefault == null) {
      nonDefault = new Properties();
    }
    DistributedTestSupport.addHydraProperties(nonDefault);
    
    DistributionConfig dc = new DistributionConfigImpl(nonDefault);
    LogWriter logger = LogWriterFactory.createLogWriterLogger(
        false/*isLoner*/, false/*isSecurityLog*/, dc, 
        false);        
    
    // if config was non-null, then these will be added to it...
    nonDefault.put(DistributionConfig.LOG_WRITER_NAME, logger);
    
    return logger;
  }

  /**
   * This finds the log level configured for the test run.  It should be used
   * when creating a new distributed system if you want to specify a log level.
   * @return the dunit log-level setting
   */
  public static String getDUnitLogLevel() {
    Properties p = DUnitEnv.get().getDistributedSystemProperties();
    String result = p.getProperty(DistributionConfig.LOG_LEVEL_NAME);
    if (result == null) {
      result = ManagerLogWriter.levelToString(DistributionConfig.DEFAULT_LOG_LEVEL);
    }
    return result;
  }
}
