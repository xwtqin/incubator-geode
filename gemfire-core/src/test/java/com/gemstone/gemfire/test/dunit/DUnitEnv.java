/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
/**
 * 
 */
package com.gemstone.gemfire.test.dunit;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.DistributionConfigImpl;
import com.gemstone.gemfire.internal.logging.LogWriterFactory;
import com.gemstone.gemfire.internal.logging.ManagerLogWriter;
import com.gemstone.gemfire.test.dunit.standalone.BounceResult;

/**
 * This class provides an abstraction over the environment
 * that is used to run dunit. This will delegate to the hydra
 * or to the standalone dunit launcher as needed.
 * 
 * Any dunit tests that rely on hydra configuration should go
 * through here, so that we can separate them out from depending on hydra
 * and run them on a different VM launching system.
 *   
 * @author dsmith
 *
 */
public abstract class DUnitEnv {
  
  public static DUnitEnv instance = null;
  
  public static final DUnitEnv get() {
    if (instance == null) {
      try {
        // for tests that are still being migrated to the open-source
        // distributed unit test framework  we need to look for this
        // old closed-source dunit environment
        Class clazz = Class.forName("dunit.hydra.HydraDUnitEnv");
        instance = (DUnitEnv)clazz.newInstance();
      } catch (Exception e) {
        throw new Error("Distributed unit test environment is not initialized");
      }
    }
    return instance;
  }
  
  public static void set(DUnitEnv dunitEnv) {
    instance = dunitEnv;
  }
  
  public abstract String getLocatorString();
  
  public abstract String getLocatorAddress();

  public abstract int getLocatorPort();
  
  public abstract Properties getDistributedSystemProperties();

  public abstract int getPid();

  public abstract int getVMID();

  public abstract BounceResult bounce(int pid) throws RemoteException;

  public abstract File getWorkingDirectory(int pid);

  //---------------------------------------------------------------------------
  // static methods
  //---------------------------------------------------------------------------
  
  public static final Properties getAllDistributedSystemProperties(final Properties props) { // TODO: delete
    Properties p = DUnitEnv.get().getDistributedSystemProperties();
    
    // our tests do not expect auto-reconnect to be on by default
    if (!p.contains(DistributionConfig.DISABLE_AUTO_RECONNECT_NAME)) {
      p.put(DistributionConfig.DISABLE_AUTO_RECONNECT_NAME, "true");
    }
        
    for (Iterator iter = props.entrySet().iterator();
    iter.hasNext(); ) {
      Map.Entry entry = (Map.Entry) iter.next();
      String key = (String) entry.getKey();
      Object value = entry.getValue();
      p.put(key, value);
    }
    return p;
  }

  /**
   * This finds the log level configured for the test run.  It should be used
   * when creating a new distributed system if you want to specify a log level.
   * 
   * @return the dunit log-level setting
   */
  public static String getDUnitLogLevel() { // TODO: delete
    Properties p = DUnitEnv.get().getDistributedSystemProperties();
    String result = p.getProperty(DistributionConfig.LOG_LEVEL_NAME);
    if (result == null) {
      result = ManagerLogWriter.levelToString(DistributionConfig.DEFAULT_LOG_LEVEL);
    }
    return result;
  }

  /**
   * Get the port that the standard dunit locator is listening on.
   * @return
   */
  public static int getDUnitLocatorPort() {
    return DUnitEnv.get().getLocatorPort();
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
  protected static LogWriter createLogWriter(Properties config) {
    Properties nonDefault = config;
    if (nonDefault == null) {
      nonDefault = new Properties();
    }
    addHydraProperties(nonDefault);
    
    DistributionConfig dc = new DistributionConfigImpl(nonDefault);
    LogWriter logger = LogWriterFactory.createLogWriterLogger(
        false/*isLoner*/, false/*isSecurityLog*/, dc, 
        false);        
    
    // if config was non-null, then these will be added to it...
    nonDefault.put(DistributionConfig.LOG_WRITER_NAME, logger);
    
    return logger;
  }

  /**
   * Fetches the GemFireDescription for this test and adds its 
   * DistributedSystem properties to the provided props parameter.
   * 
   * @param config the properties to add hydra's test properties to
   */
  private static void addHydraProperties(Properties config) {
    Properties p = DUnitEnv.get().getDistributedSystemProperties();
    for (Iterator iter = p.entrySet().iterator();
        iter.hasNext(); ) {
      Map.Entry entry = (Map.Entry) iter.next();
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();
      if (config.getProperty(key) == null) {
        config.setProperty(key, value);
      }
    }
  }
}
