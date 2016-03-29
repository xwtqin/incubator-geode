/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.gemstone.gemfire.security;

import static com.gemstone.gemfire.distributed.internal.DistributionConfig.*;
import static com.gemstone.gemfire.internal.AvailablePort.*;
import static com.gemstone.gemfire.test.dunit.Assert.*;
import static com.gemstone.gemfire.test.dunit.DistributedTestUtils.*;
import static com.gemstone.gemfire.test.dunit.LogWriterUtils.*;
import static com.gemstone.gemfire.test.dunit.NetworkUtils.*;
import static com.gemstone.gemfire.test.dunit.Wait.*;

import static com.gemstone.gemfire.cache30.ClientServerTestCase.configureConnectionPoolWithNameAndFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.gemstone.gemfire.cache.AttributesFactory;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.DynamicRegionFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.Scope;
import com.gemstone.gemfire.cache.client.NoAvailableServersException;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolFactory;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.client.ServerConnectivityException;
import com.gemstone.gemfire.cache.client.ServerOperationException;
import com.gemstone.gemfire.cache.client.ServerRefusedConnectionException;
import com.gemstone.gemfire.cache.client.internal.PoolImpl;
import com.gemstone.gemfire.cache.client.internal.ProxyCache;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.server.CacheServer;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.Locator;
import com.gemstone.gemfire.internal.util.Callable;
import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxSerializable;
import com.gemstone.gemfire.pdx.PdxWriter;
import com.gemstone.gemfire.test.dunit.DistributedTestCase;
import com.gemstone.gemfire.test.dunit.WaitCriterion;

/**
 * Contains utility methods for setting up servers/clients for authentication
 * and authorization tests.
 * 
 * @since 5.5
 */
public final class SecurityTestUtils {

  private final DistributedTestCase distributedTestCase = new DistributedTestCase(getClass().getSimpleName()) {}; // TODO: delete

  protected static final int NO_EXCEPTION = 0;
  protected static final int AUTHREQ_EXCEPTION = 1;
  protected static final int AUTHFAIL_EXCEPTION = 2;
  protected static final int CONNREFUSED_EXCEPTION = 3;
  protected static final int NOTAUTHZ_EXCEPTION = 4;
  protected static final int OTHER_EXCEPTION = 5;
  protected static final int NO_AVAILABLE_SERVERS = 6;
  // Indicates that AuthReqException may not necessarily be thrown
  protected static final int NOFORCE_AUTHREQ_EXCEPTION = 16;

  protected static final String REGION_NAME = "AuthRegion";
  protected static final String[] KEYS = { "key1", "key2", "key3", "key4", "key5", "key6", "key7", "key8" };
  protected static final String[] VALUES = { "value1", "value2", "value3", "value4", "value5", "value6", "value7", "value8" };
  protected static final String[] NVALUES = { "nvalue1", "nvalue2", "nvalue3", "nvalue4", "nvalue5", "nvalue6", "nvalue7", "nvalue8" };

  private static final int NUMBER_OF_USERS = 1;

  private static String[] ignoredExceptions = null;

  private static Locator locator = null;
  private static Cache cache = null;
  private static Properties currentJavaProps = null;
  private static String locatorString = null;

  private static Pool pool = null;
  private static boolean multiUserAuthMode = false;

  private static ProxyCache[] proxyCaches = new ProxyCache[NUMBER_OF_USERS];

  private static Region regionRef = null;

  public SecurityTestUtils(String name) { // TODO: delete
  }

  /**
   * @deprecated Please use {@link com.gemstone.gemfire.test.dunit.IgnoredException} instead
   */
  private static void addIgnoredExceptions(final String[] expectedExceptions) { // TODO: delete
    if (expectedExceptions != null) {
      for (int index = 0; index < expectedExceptions.length; index++) {
        getLogWriter().info("<ExpectedException action=add>" + expectedExceptions[index] + "</ExpectedException>");
      }
    }
  }

  /**
   * @deprecated Please use {@link com.gemstone.gemfire.test.dunit.IgnoredException} instead
   */
  private static void removeExpectedExceptions(final String[] expectedExceptions) { // TODO: delete
    if (expectedExceptions != null) {
      for (int index = 0; index < expectedExceptions.length; index++) {
        getLogWriter().info("<ExpectedException action=remove>" + expectedExceptions[index] + "</ExpectedException>");
      }
    }
  }

  protected static void setJavaProps(final Properties javaProps) {
    removeJavaProperties(currentJavaProps);
    addJavaProperties(javaProps);
    currentJavaProps = javaProps;
  }

  protected static ProxyCache getProxyCaches(final int index) {
    return proxyCaches[index];
  }

  protected static void initDynamicRegionFactory() {
    DynamicRegionFactory.get().open(new DynamicRegionFactory.Config(null, null, false, true));
  }

  protected static Integer getLocatorPort() {
    int locatorPort = getRandomAvailablePort(SOCKET);
    String addr = getIPLiteral();
    if (locatorString == null) {
      locatorString = addr + "[" + locatorPort + ']';
    } else {
      locatorString += "," + addr + "[" + locatorPort + ']';
    }
    return locatorPort;
  }

  /**
   * Note that this clears the string after returning for convenience in reusing
   * for other tests. Hence it should normally be invoked only once for a test.
   */
  protected static String getAndClearLocatorString() {
    String locString = locatorString;
    locatorString = null;
    return locString;
  }

  protected static Properties concatProperties(final Properties[] propsList) {
    Properties props = new Properties();
    for (int index = 0; index < propsList.length; ++index) {
      if (propsList[index] != null) {
        props.putAll(propsList[index]);
      }
    }
    return props;
  }

  protected static void registerExpectedExceptions(final String[] expectedExceptions) { // TODO: delete
    SecurityTestUtils.ignoredExceptions = expectedExceptions;
  }

  protected static int createCacheServer(final Properties authProps, final Properties javaProps, final int dsPort, final String locatorString, final int serverPort, final int expectedResult) {
    return createCacheServer(authProps, javaProps, dsPort, locatorString, serverPort, false, expectedResult);
  }

  protected static int createCacheServer(Properties authProps, final Properties javaProps, final int locatorPort, final String locatorString, final int serverPort, final boolean setupDynamicRegionFactory, final int expectedResult) {
    if (authProps == null) {
      authProps = new Properties();
    }
    authProps.setProperty(MCAST_PORT_NAME, "0");
    if (locatorString != null && locatorString.length() > 0) {
      authProps.setProperty(LOCATORS_NAME, locatorString);
      authProps.setProperty(START_LOCATOR_NAME, getIPLiteral() + "[" + locatorPort + ']');
    } else {
      authProps.setProperty("locators", "localhost["+getDUnitLocatorPort()+"]");
    }
    authProps.setProperty(SECURITY_LOG_LEVEL_NAME, "finest");
    getLogWriter().info("Set the server properties to: " + authProps);
    getLogWriter().info("Set the java properties to: " + javaProps);

    SecurityTestUtils tmpInstance = new SecurityTestUtils("temp");
    try {
      tmpInstance.createSystem(authProps, javaProps);
      if (expectedResult != NO_EXCEPTION) {
        fail("Expected a security exception when starting peer");
      }
    }
    catch (AuthenticationRequiredException ex) {
      if (expectedResult == AUTHREQ_EXCEPTION) {
        getLogWriter().info("Got expected exception when starting peer: " + ex);
        return 0;
      }
      else {
        fail("Got unexpected exception when starting peer", ex);
      }
    }
    catch (AuthenticationFailedException ex) {
      if (expectedResult == AUTHFAIL_EXCEPTION) {
        getLogWriter().info("Got expected exception when starting peer: " + ex);
        return 0;
      }
      else {
        fail("Got unexpected exception when starting peer", ex);
      }
    }
    catch (Exception ex) {
      fail("Got unexpected exception when starting peer", ex);
    }

    if (setupDynamicRegionFactory) {
      initDynamicRegionFactory();
    }
    tmpInstance.openCache();
    AttributesFactory factory = new AttributesFactory();
    factory.setScope(Scope.DISTRIBUTED_ACK);
    factory.setDataPolicy(DataPolicy.REPLICATE);
    RegionAttributes attrs = factory.create();
    cache.createRegion(REGION_NAME, attrs);
    int port;
    if (serverPort <= 0) {
      port = 0;
    }
    else {
      port = serverPort;
    }
    CacheServer server1 = cache.addCacheServer();
    server1.setPort(port);
    server1.setNotifyBySubscription(true);
    try {
      server1.start();
    }
    catch (Exception ex) {
      fail("Got unexpected exception when starting CacheServer", ex);
    }
    return server1.getPort();
  }

  // 1
  protected static void createCacheClient(final String authInitModule, final Properties authProps, final Properties javaProps, final int[] ports, final int numConnections, final int expectedResult) {
    createCacheClient(authInitModule, authProps, javaProps, ports, numConnections, false, expectedResult);
  }

  // 2 a
  protected static void createCacheClient(final String authInitModule, final Properties authProps, final Properties javaProps, final int[] ports, final int numConnections, final boolean multiUserMode, final int expectedResult) {
    createCacheClient(authInitModule, authProps, javaProps, ports, numConnections, false, multiUserMode, expectedResult);
  }

  // 3
  protected static void createCacheClientWithDynamicRegion(final String authInitModule, final Properties authProps, final Properties javaProps, final int[] ports, final int numConnections, final boolean setupDynamicRegionFactory, final int expectedResult) {
    createCacheClient(authInitModule, authProps, javaProps, ports, numConnections, setupDynamicRegionFactory, false, expectedResult);
  }

  // 4
  protected static void createCacheClient(final String authInitModule,
                                          final Properties authProps, final Properties javaProps, final int[] ports,
                                          final int numConnections, final boolean setupDynamicRegionFactory,
                                          final boolean multiUserMode, final int expectedResult) {
    createCacheClient(authInitModule, authProps, javaProps, ports,
        numConnections, setupDynamicRegionFactory, multiUserMode, Boolean.TRUE,
        expectedResult);
  }

  // 5
  protected static void createCacheClient(final String authInitModule,
                                          Properties authProps, final Properties javaProps, int[] ports,
                                          final int numConnections, final boolean setupDynamicRegionFactory,
                                          final boolean multiUserMode, final boolean subscriptionEnabled,
                                          final int expectedResult) {

    multiUserAuthMode = Boolean.valueOf(multiUserMode);
    if (authProps == null) {
      authProps = new Properties();
    }
    authProps.setProperty(MCAST_PORT_NAME, "0");
    authProps.setProperty(LOCATORS_NAME, "");
    authProps.setProperty(SECURITY_LOG_LEVEL_NAME, "finest");
    // TODO (ashetkar) Add " && (!multiUserAuthMode)" below.
    if (authInitModule != null) {
      authProps.setProperty(SECURITY_CLIENT_AUTH_INIT_NAME,
          authInitModule);
    }

    SecurityTestUtils tmpInstance = new SecurityTestUtils("temp");
    tmpInstance.createSystem(authProps, javaProps);
    AttributesFactory factory = new AttributesFactory();
    int[] portsI = new int[ports.length];
    for(int z=0;z<ports.length;z++) {
      portsI[z] = ports[z];
    }
   
    try {
      PoolFactory poolFactory = PoolManager.createFactory();
      poolFactory.setRetryAttempts(200);
      if (multiUserAuthMode) {
        poolFactory.setMultiuserAuthentication(multiUserAuthMode);
        // [sumedh] Why is this false here only to be overridden in
        // ClientServerTestCase.configureConnectionPoolWithNameAndFactory below?
        // Actually setting it to false causes MultiuserAPIDUnitTest to fail.
        //poolFactory.setSubscriptionEnabled(false);
      }
      pool = configureConnectionPoolWithNameAndFactory(factory,
          getIPLiteral(), portsI, subscriptionEnabled, 0,
          numConnections, null, null,
          poolFactory);

      if (setupDynamicRegionFactory) {
        initClientDynamicRegionFactory(pool.getName());
      }
      tmpInstance.openCache();
      try {
        getLogWriter().info("multi-user mode " + multiUserAuthMode);
        proxyCaches[0] = (ProxyCache)((PoolImpl) pool).createAuthenticatedCacheView(authProps);
        if (!multiUserAuthMode) {
          fail("Expected a UnsupportedOperationException but got none in single-user mode");
        }
      } catch (UnsupportedOperationException uoe) {
        if (!multiUserAuthMode) {
          getLogWriter().info("Got expected UnsupportedOperationException in single-user mode");
        }
        else {
          fail("Got unexpected exception in multi-user mode ", uoe);
        }
      }

      factory.setScope(Scope.LOCAL);
      if (multiUserAuthMode) {
        factory.setDataPolicy(DataPolicy.EMPTY);
      }
      RegionAttributes attrs = factory.create();
      cache.createRegion(REGION_NAME, attrs);

      if (expectedResult != NO_EXCEPTION
          && expectedResult != NOFORCE_AUTHREQ_EXCEPTION) {
        if (!multiUserAuthMode) {
          fail("Expected an exception when starting client");
        }
      }
    }
    catch (AuthenticationRequiredException ex) {
      if (expectedResult == AUTHREQ_EXCEPTION
          || expectedResult == NOFORCE_AUTHREQ_EXCEPTION) {
        getLogWriter().info(
            "Got expected exception when starting client: " + ex);
      }
      else {
        fail("Got unexpected exception when starting client", ex);
      }
    }
    catch (AuthenticationFailedException ex) {
      if (expectedResult == AUTHFAIL_EXCEPTION) {
        getLogWriter().info(
            "Got expected exception when starting client: " + ex);
      }
      else {
        fail("Got unexpected exception when starting client", ex);
      }
    }
    catch (ServerRefusedConnectionException ex) {
      if (expectedResult == CONNREFUSED_EXCEPTION) {
        getLogWriter().info(
            "Got expected exception when starting client: " + ex);
      }
      else {
        fail("Got unexpected exception when starting client", ex);
      }
    }
    catch (Exception ex) {
      fail("Got unexpected exception when starting client", ex);
    }
  }

  protected static void createCacheClientForMultiUserMode(final int numOfUsers,
                                                          final String authInitModule, final Properties[] authProps, final Properties javaProps,
                                                          final int[] ports, final int numConnections,
                                                          final boolean setupDynamicRegionFactory, final int expectedResult) {
    createCacheClientForMultiUserMode(numOfUsers, authInitModule, authProps,
        javaProps, ports, numConnections, setupDynamicRegionFactory, null,
        expectedResult);
  }

  protected static void createCacheClientForMultiUserMode(final int numOfUsers,
                                                          final String authInitModule, final Properties[] authProps, final Properties javaProps,
                                                          final int[] ports, final int numConnections,
                                                          final boolean setupDynamicRegionFactory, final String durableClientId,
                                                          final int expectedResult) {

    if (numOfUsers < 1) {
      fail("Number of users cannot be less than one");
    }
    multiUserAuthMode = true;
    if (numOfUsers != authProps.length) {
      fail("Number of authProps provided does not match with numOfUsers specified, "
          + authProps.length);
    }
    if (authProps[0] == null) {
      authProps[0] = new Properties();
    }
    authProps[0].setProperty(MCAST_PORT_NAME, "0");
    authProps[0].setProperty(LOCATORS_NAME, "");
    authProps[0].setProperty(SECURITY_LOG_LEVEL_NAME,
        "finest");
    Properties props = new Properties();
    if (authInitModule != null) {
      authProps[0].setProperty(
          SECURITY_CLIENT_AUTH_INIT_NAME, authInitModule);
      props.setProperty(SECURITY_CLIENT_AUTH_INIT_NAME,
          authInitModule);
    }
    if (durableClientId != null) {
      props.setProperty(DURABLE_CLIENT_ID_NAME,
          durableClientId);
      props.setProperty(DURABLE_CLIENT_TIMEOUT_NAME, String
          .valueOf(DEFAULT_DURABLE_CLIENT_TIMEOUT));
    }

    SecurityTestUtils tmpInstance = new SecurityTestUtils("temp");
    tmpInstance.createSystem(props, javaProps);
    AttributesFactory factory = new AttributesFactory();
    int[] portsI = new int[ports.length];
    for(int z=0;z<ports.length;z++) {
      portsI[z] = ports[z];
    }
   
    try {
      tmpInstance.openCache();
      PoolFactory poolFactory = PoolManager.createFactory();
      poolFactory.setRetryAttempts(200);
      poolFactory.setMultiuserAuthentication(multiUserAuthMode);
      poolFactory.setSubscriptionEnabled(true);
      pool = configureConnectionPoolWithNameAndFactory(factory,
          getIPLiteral(), portsI, true, 1,
          numConnections, null, null,
          poolFactory);

      if (setupDynamicRegionFactory) {
        initClientDynamicRegionFactory(pool.getName());
      }
      proxyCaches = new ProxyCache[numOfUsers];
      for (int i=0; i<numOfUsers; i++) {
        proxyCaches[i] = (ProxyCache)((PoolImpl) pool).createAuthenticatedCacheView(authProps[i]);
      }

      factory.setScope(Scope.LOCAL);
      factory.setDataPolicy(DataPolicy.EMPTY);
      RegionAttributes attrs = factory.create();
      cache.createRegion(REGION_NAME, attrs);

      if (expectedResult != NO_EXCEPTION
          && expectedResult != NOFORCE_AUTHREQ_EXCEPTION) {
        if (!multiUserAuthMode) {
          fail("Expected an exception when starting client");
        }
      }
    }
    catch (AuthenticationRequiredException ex) {
      if (expectedResult == AUTHREQ_EXCEPTION
          || expectedResult == NOFORCE_AUTHREQ_EXCEPTION) {
        getLogWriter().info(
            "Got expected exception when starting client: " + ex);
      }
      else {
        fail("Got unexpected exception when starting client", ex);
      }
    }
    catch (AuthenticationFailedException ex) {
      if (expectedResult == AUTHFAIL_EXCEPTION) {
        getLogWriter().info(
            "Got expected exception when starting client: " + ex);
      }
      else {
        fail("Got unexpected exception when starting client", ex);
      }
    }
    catch (ServerRefusedConnectionException ex) {
      if (expectedResult == CONNREFUSED_EXCEPTION) {
        getLogWriter().info(
            "Got expected exception when starting client: " + ex);
      }
      else {
        fail("Got unexpected exception when starting client", ex);
      }
    }
    catch (Exception ex) {
      fail("Got unexpected exception when starting client", ex);
    }
  }

  protected static void createProxyCache(final int[] userIndices, final Properties[] props) {
    int j = 0;
    for (int i : userIndices) {
      proxyCaches[i] = (ProxyCache)((PoolImpl) pool)
          .createAuthenticatedCacheView(props[j]);
      j++;
    }
  }

  protected static void startLocator(final String name, Integer port, final Object extraProps,
                                     final Object javaProps, final String[] expectedExceptions) {
    File logFile = new File(name + "-locator" + port.intValue() + ".log");
    try {
      Properties authProps = new Properties();
      if (extraProps != null) {
        authProps.putAll((Properties)extraProps);
      }
      authProps.setProperty(MCAST_PORT_NAME, "0");
      authProps.setProperty(LOCATORS_NAME, 
                            getIPLiteral() + "[" + port + "]");
      authProps.setProperty(ENABLE_CLUSTER_CONFIGURATION_NAME, "false");
      clearStaticSSLContext();
      setJavaProps((Properties)javaProps);
      FileOutputStream logOut = new FileOutputStream(logFile);
      PrintStream logStream = new PrintStream(logOut);
      addIgnoredExceptions(expectedExceptions);
      logStream.flush();
      locator = Locator.startLocatorAndDS(port.intValue(), logFile, null,
          authProps);
    }
    catch (IOException ex) {
      fail("While starting locator on port " + port.intValue(), ex);
    }
  }

  protected static void stopLocator(final Integer port, final String[] expectedExceptions) {
    try {
      locator.stop();
      removeExpectedExceptions(expectedExceptions);
    }
    catch (Exception ex) {
      fail("While stopping locator on port " + port.intValue(), ex);
    }
  }

  protected static Cache getCache() {
    return cache;
  }

  // Some useful region methods used by security tests

  protected static void waitForCondition(final Callable cond) {
    waitForCondition(cond, 100, 120);
  }

  protected static void waitForCondition(final Callable cond, final int sleepMillis,
                                         final int numTries) {
    WaitCriterion ev = new WaitCriterion() {
      public boolean done() {
        try {
          return ((Boolean)cond.call()).booleanValue();
        }
        catch (Exception e) {
          fail("Unexpected exception", e);
        }
        return false; // NOTREACHED
      }
      public String description() {
        return null;
      }
    };
    waitForCriterion(ev, sleepMillis * numTries, 200, true);
  }

  protected static Object getLocalValue(final Region region, final Object key) {
    Region.Entry entry = region.getEntry(key);
    return (entry != null ? entry.getValue() : null);
  }

  protected static void doProxyCacheClose() {
    for (int i = 0; i< proxyCaches.length; i++) {
      proxyCaches[i].close();
    }
  }


  protected static void doPutAllP() throws Exception {
    Region region = getCache().getRegion(REGION_NAME);
    assertNotNull(region);
    Map map = new LinkedHashMap();
    map.put("1010L", new Employee(1010L, "John", "Doe"));
    region.putAll(map);
  }
  
  protected static void doPuts(final Integer num) {
    doPutsP(num, new Integer(NO_EXCEPTION), false);
  }

  protected static void doPuts(final Integer num, final Integer expectedResult) {
    doPutsP(num, expectedResult, false);
  }

  protected static void doMultiUserPuts(final Integer num, final Integer numOfUsers,
                                        final Integer[] expectedResults) {
    if (numOfUsers != expectedResults.length) {
      fail("SecurityTestUtils.doMultiUserPuts(): numOfUsers = " + numOfUsers
          + ", but expected results " + expectedResults.length);
    }
    for (int i = 0; i < numOfUsers; i++) {
      getLogWriter().info("PUT: MultiUser# " + i);
      doPutsP(num, Integer.valueOf(i), expectedResults[i], false);
    }
  }

  protected static void doGets(final Integer num) {
    doGetsP(num, new Integer(NO_EXCEPTION), false);
  }

  protected static void doGets(final Integer num, final Integer expectedResult) {
    doGetsP(num, expectedResult, false);
  }

  protected static void doMultiUserGetAll(final Integer numOfUsers, final Integer[] expectedResults) {
    doMultiUserGetAll(numOfUsers, expectedResults, false);
  }

  protected static void doMultiUserGetAll(final Integer numOfUsers,
                                          final Integer[] expectedResults, final boolean useTX) {
    if (numOfUsers != expectedResults.length) {
      fail("SecurityTestUtils.doMultiUserGetAll(): numOfUsers = " + numOfUsers
          + ", but expected results " + expectedResults.length);
    }
    for (int i = 0; i < numOfUsers; i++) {
      getLogWriter().info(
          "GET_ALL" + (useTX ? " in TX" : "") + ": MultiUser# " + i);
      doGetAllP(Integer.valueOf(i), expectedResults[i], useTX);
    }
  }

  protected static void doMultiUserGets(final Integer num, final Integer numOfUsers,
                                        final Integer[] expectedResults) {
    if (numOfUsers != expectedResults.length) {
      fail("SecurityTestUtils.doMultiUserGets(): numOfUsers = " + numOfUsers
          + ", but expected results " + expectedResults.length);
    }
    for (int i = 0; i < numOfUsers; i++) {
      getLogWriter().info("GET: MultiUser# " + i);
      doGetsP(num, Integer.valueOf(i), expectedResults[i], false);
    }
  }

  protected static void doMultiUserRegionDestroys(final Integer numOfUsers,
                                                  final Integer[] expectedResults) {
    if (numOfUsers != expectedResults.length) {
      fail("SecurityTestUtils.doMultiUserRegionDestroys(): numOfUsers = " + numOfUsers
          + ", but expected results " + expectedResults.length);
    }
    for (int i = numOfUsers-1; i >= 0; i--) {
      getLogWriter().info("DESTROY: MultiUser# " + i);
      doRegionDestroysP(Integer.valueOf(i), expectedResults[i]);
    }
  }

  protected static void doMultiUserDestroys(final Integer num, final Integer numOfUsers,
                                            final Integer[] expectedResults) {
    if (numOfUsers != expectedResults.length) {
      fail("SecurityTestUtils.doMultiUserDestroys(): numOfUsers = " + numOfUsers
          + ", but expected results " + expectedResults.length);
    }
    for (int i = 0; i < numOfUsers; i++) {
      getLogWriter().info("DESTROY: MultiUser# " + i);
      doDestroysP(num, Integer.valueOf(i), expectedResults[i], false);
    }
  }

  protected static void doMultiUserInvalidates(final Integer num, final Integer numOfUsers,
                                               final Integer[] expectedResults) {
    if (numOfUsers != expectedResults.length) {
      fail("SecurityTestUtils.doMultiUserInvalidates(): numOfUsers = " + numOfUsers
          + ", but expected results " + expectedResults.length);
    }
    for (int i = 0; i < numOfUsers; i++) {
      getLogWriter().info("INVALIDATE: MultiUser# " + i);
      doInvalidatesP(num, Integer.valueOf(i), expectedResults[i], false);
    }
  }

  protected static void doMultiUserContainsKeys(final Integer num, final Integer numOfUsers,
                                                final Integer[] expectedResults, final Boolean[] results) {
    if (numOfUsers != expectedResults.length) {
      fail("SecurityTestUtils.doMultiUserContainsKeys(): numOfUsers = " + numOfUsers
          + ", but #expected results " + expectedResults.length);
    }
    if (numOfUsers != results.length) {
      fail("SecurityTestUtils.doMultiUserContainsKeys(): numOfUsers = " + numOfUsers
          + ", but #expected output " + results.length);
    }
    for (int i = 0; i < numOfUsers; i++) {
      getLogWriter().info("CONTAINS_KEY: MultiUser# " + i);
      doContainsKeysP(num, Integer.valueOf(i), expectedResults[i], false, results[i]);
    }
  }

  protected static void doMultiUserQueries(final Integer numOfUsers,
                                           final Integer[] expectedResults, final Integer valueSize) {
    if (numOfUsers != expectedResults.length) {
      fail("SecurityTestUtils.doMultiUserQueries(): numOfUsers = " + numOfUsers
          + ", but #expected results " + expectedResults.length);
    }
    for (int i = 0; i < numOfUsers; i++) {
      getLogWriter().info("QUERY: MultiUser# " + i);
      doQueriesP(Integer.valueOf(i), expectedResults[i], valueSize);
    }
  }

  protected static void doMultiUserFE(final Integer numOfUsers, final Function function,
                                      final Integer[] expectedResults, final Object[] results, final Boolean isFailoverCase) {
    if (numOfUsers != expectedResults.length) {
      fail("SecurityTestUtils.doMultiUserFE(): numOfUsers = " + numOfUsers
          + ", but #expected results " + expectedResults.length);
    }
    if (numOfUsers != results.length) {
      fail("SecurityTestUtils.doMultiUserFE(): numOfUsers = " + numOfUsers
          + ", but #expected output " + results.length);
    }
    for (int i = 0; i < numOfUsers; i++) {
      getLogWriter().info("FunctionExecute:onRegion MultiUser# " + i);
      doFunctionExecuteP(Integer.valueOf(i), function, expectedResults[i], results[i], "region");
    }
    for (int i = 0; i < numOfUsers; i++) {
      getLogWriter().info("FunctionExecute:onServer MultiUser# " + i);
      doFunctionExecuteP(Integer.valueOf(i), function, expectedResults[i], results[i], "server");
    }
    if (!isFailoverCase) {
      for (int i = 0; i < numOfUsers; i++) {
        getLogWriter().info("FunctionExecute:onServers MultiUser# " + i);
        doFunctionExecuteP(Integer.valueOf(i), function, expectedResults[i],
            results[i], "servers");
      }
    }
  }

  protected static void doMultiUserQueryExecute(final Integer numOfUsers,
                                                final Integer[] expectedResults, final Integer result) {
    if (numOfUsers != expectedResults.length) {
      fail("SecurityTestUtils.doMultiUserFE(): numOfUsers = " + numOfUsers
          + ", but #expected results " + expectedResults.length);
    }
    for (int i = 0; i < numOfUsers; i++) {
      getLogWriter().info("QueryExecute: MultiUser# " + i);
      doQueryExecuteP(Integer.valueOf(i), expectedResults[i], result);
    }
  }

  protected static void doLocalGets(final Integer num) {
    doLocalGetsP(num.intValue(), false);
  }

  protected static void doNPuts(final Integer num) {
    doPutsP(num, new Integer(NO_EXCEPTION), true);
  }

  protected static void doNPuts(final Integer num, final Integer expectedResult) {
    doPutsP(num, expectedResult, true);
  }

  protected static void doNGets(final Integer num) {
    doGetsP(num, new Integer(NO_EXCEPTION), true);
  }

  protected static void doNGets(final Integer num, final Integer expectedResult) {
    doGetsP(num, expectedResult, true);
  }

  protected static void doNLocalGets(final Integer num) {
    doLocalGetsP(num.intValue(), true);
  }

  protected static void doSimpleGet(final String expectedResult) {
    if (regionRef != null) {
      try {
        regionRef.get("KEY");
        if (expectedResult != null && expectedResult.endsWith("Exception")) {
          fail("Expected " + expectedResult + " but found none in doSimpleGet()");
        }
      } catch (Exception e) {
        if (!e.getClass().getSimpleName().endsWith(expectedResult)) {
          fail("Expected " + expectedResult + " but found "
              + e.getClass().getSimpleName() + " in doSimpleGet()");
        } else {
          getLogWriter().fine(
              "Got expected " + e.getClass().getSimpleName()
                  + " in doSimpleGet()");
        }
      }
    }
  }

  protected static void doSimplePut(final String expectedResult) {
    if (regionRef != null) {
      try {
        regionRef.put("KEY", "VALUE");
        if (expectedResult != null && expectedResult.endsWith("Exception")) {
          fail("Expected " + expectedResult + " but found none in doSimplePut()");
        }
      } catch (Exception e) {
        if (!e.getClass().getSimpleName().endsWith(expectedResult)) {
          fail("Expected " + expectedResult + " but found "
              + e.getClass().getSimpleName() + " in doSimplePut()", e);
        } else {
          getLogWriter().fine(
              "Got expected " + e.getClass().getSimpleName()
                  + " in doSimplePut()");
        }
      }
    }
  }

  // This is a hack using reflection to clear the static objects in JSSE since
  // otherwise changing the javax.* store related properties has no effect
  // during the course of running dunit suite unless the VMs are restarted.
  protected static void clearStaticSSLContext() {
    ServerSocketFactory defaultServerFact = SSLServerSocketFactory.getDefault();
    // Get the class of this and use reflection to blank out any static
    // SSLContext objects inside
    Map contextMap = getSSLFields(defaultServerFact, new Class[] {
        SSLContext.class, SSLContextSpi.class });
    makeNullSSLFields(defaultServerFact, contextMap);
    Iterator contextObjsIter = contextMap.values().iterator();
    while (contextObjsIter.hasNext()) {
      Object contextObj = contextObjsIter.next();
      Map contextObjsMap = getSSLFields(contextObj, new Class[] {
          TrustManager.class, KeyManager.class, TrustManager[].class,
          KeyManager[].class });
      makeNullSSLFields(contextObj, contextObjsMap);
    }
    makeNullStaticField(SSLServerSocketFactory.class);

    // Do the same for normal SSL socket factory
    SocketFactory defaultFact = SSLSocketFactory.getDefault();
    contextMap = getSSLFields(defaultFact, new Class[] { SSLContext.class,
        SSLContextSpi.class });
    makeNullSSLFields(defaultFact, contextMap);
    contextObjsIter = contextMap.values().iterator();
    while (contextObjsIter.hasNext()) {
      Object contextObj = contextObjsIter.next();
      Map contextObjsMap = getSSLFields(contextObj, new Class[] {
          TrustManager.class, KeyManager.class, TrustManager[].class,
          KeyManager[].class });
      makeNullSSLFields(contextObj, contextObjsMap);
    }
    makeNullStaticField(SSLSocketFactory.class);
    makeNullStaticField(SSLContext.class);
  }

  protected static void closeCache() {
    removeExpectedExceptions(ignoredExceptions);
    if (cache != null && !cache.isClosed()) {
      DistributedSystem sys = cache.getDistributedSystem();
      cache.close();
      sys.disconnect();
      cache = null;
    }
    DistributedTestCase.disconnectFromDS();
  }

  protected static void closeCache(final Boolean keepAlive) {
    removeExpectedExceptions(ignoredExceptions);
    if (cache != null && !cache.isClosed()) {
      DistributedSystem sys = cache.getDistributedSystem();
      cache.close(keepAlive);
      sys.disconnect();
      cache = null;
    }
    DistributedTestCase.disconnectFromDS();
  }

  // ------------------------- private static methods -------------------------

  private static void initClientDynamicRegionFactory(final String poolName) {
    DynamicRegionFactory.get().open(new DynamicRegionFactory.Config(null, poolName, false, true));
  }

  private static void addJavaProperties(final Properties javaProps) {
    if (javaProps != null) {
      Iterator iter = javaProps.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry entry = (Map.Entry)iter.next();
        System.setProperty((String)entry.getKey(), (String)entry.getValue());
      }
    }
  }

  private static void removeJavaProperties(final Properties javaProps) {
    if (javaProps != null) {
      Properties props = System.getProperties();
      Iterator iter = javaProps.keySet().iterator();
      while (iter.hasNext()) {
        props.remove(iter.next());
      }
      System.setProperties(props);
    }
  }

  private static void doPutsP(final Integer num, final Integer expectedResult,
                              final boolean newVals) {
    doPutsP(num, Integer.valueOf(0), expectedResult, newVals);
  }

  private static void doPutsP(final Integer num, final Integer multiUserIndex,
                              final Integer expectedResult, final boolean newVals) {
    assertTrue(num.intValue() <= KEYS.length);
    Region region = null;
    try {
      if (multiUserAuthMode) {
        region = proxyCaches[multiUserIndex].getRegion(REGION_NAME);
        regionRef = region;
      }
      else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNotNull(region);
    }
    catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing puts: " + ex);
      }
      else {
        fail("Got unexpected exception when doing puts", ex);
      }
    }
    for (int index = 0; index < num.intValue(); ++index) {
      try {
        if (newVals) {
          region.put(KEYS[index], NVALUES[index]);
        }
        else {
          region.put(KEYS[index], VALUES[index]);
        }
        if (expectedResult.intValue() != NO_EXCEPTION) {
          fail("Expected a NotAuthorizedException while doing puts");
        }
      }
      catch(NoAvailableServersException ex) {
        if(expectedResult.intValue() == NO_AVAILABLE_SERVERS) {
          getLogWriter().info(
                  "Got expected NoAvailableServers when doing puts: "
                          + ex.getCause());
          continue;
        }
        else {
          fail("Got unexpected exception when doing puts", ex);
        }
      }
      catch (ServerConnectivityException ex) {
        if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
                && (ex.getCause() instanceof NotAuthorizedException)) {
          getLogWriter().info(
                  "Got expected NotAuthorizedException when doing puts: "
                          + ex.getCause());
          continue;
        }
        if ((expectedResult.intValue() == AUTHREQ_EXCEPTION)
                && (ex.getCause() instanceof AuthenticationRequiredException)) {
          getLogWriter().info(
                  "Got expected AuthenticationRequiredException when doing puts: "
                          + ex.getCause());
          continue;
        }
        if ((expectedResult.intValue() == AUTHFAIL_EXCEPTION)
                && (ex.getCause() instanceof AuthenticationFailedException)) {
          getLogWriter().info(
                  "Got expected AuthenticationFailedException when doing puts: "
                          + ex.getCause());
          continue;
        }
        else if (expectedResult.intValue() == OTHER_EXCEPTION) {
          getLogWriter().info("Got expected exception when doing puts: " + ex);
        }
        else {
          fail("Got unexpected exception when doing puts", ex);
        }
      }
      catch (Exception ex) {
        if (expectedResult.intValue() == OTHER_EXCEPTION) {
          getLogWriter().info("Got expected exception when doing puts: " + ex);
        }
        else {
          fail("Got unexpected exception when doing puts", ex);
        }
      }
    }
  }

  private static HashMap getSSLFields(final Object obj, final Class[] classes) {
    HashMap resultFields = new HashMap();
    Field[] fields = obj.getClass().getDeclaredFields();
    for (int index = 0; index < fields.length; ++index) {
      Field field = fields[index];
      try {
        field.setAccessible(true);
        Object fieldObj = field.get(obj);
        boolean isInstance = false;
        for (int classIndex = 0; classIndex < classes.length; ++classIndex) {
          if ((isInstance = classes[classIndex].isInstance(fieldObj)) == true) {
            break;
          }
        }
        if (isInstance) {
          resultFields.put(field, fieldObj);
        }
      }
      catch (IllegalAccessException ex) {
        getLogWriter().warning("Exception while getting SSL fields.", ex);
      }
    }
    return resultFields;
  }

  private static void makeNullSSLFields(final Object obj, final Map fieldMap) {
    Iterator fieldIter = fieldMap.entrySet().iterator();
    while (fieldIter.hasNext()) {
      Map.Entry entry = (Map.Entry)fieldIter.next();
      Field field = (Field)entry.getKey();
      Object fieldObj = entry.getValue();
      try {
        field.setAccessible(true);
        makeNullStaticField(fieldObj.getClass());
        field.set(obj, null);
        assertNull(field.get(obj));
      }
      catch (IllegalAccessException ex) {
        getLogWriter().warning("Exception while clearing SSL fields.", ex);
      }
    }
  }

  // Deal with javax SSL properties
  private static void makeNullStaticField(final Class cls) {
    Field[] fields = cls.getDeclaredFields();
    for (int index = 0; index < fields.length; ++index) {
      Field field = fields[index];
      try {
        if (Modifier.isStatic(field.getModifiers())) {
          field.setAccessible(true);
          if (field.getClass().equals(boolean.class)) {
            field.setBoolean(null, false);
            assertFalse(field.getBoolean(null));
          }
          else if (cls.isInstance(field.get(null))) {
            field.set(null, null);
            assertNull(field.get(null));
          }
        }
      }
      catch (IllegalAccessException ex) {
        getLogWriter()
                .warning("Exception while clearing static SSL field.", ex);
      }
      catch (ClassCastException ex) {
        getLogWriter()
                .warning("Exception while clearing static SSL field.", ex);
      }
    }
  }

  private static void doQueryExecuteP(final Integer multiUserIndex,
                                      final Integer expectedResult, final Integer expectedValue) {
    Region region = null;
    try {
      if (multiUserAuthMode) {
        region = proxyCaches[multiUserIndex].getRegion(REGION_NAME);
      } else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNotNull(region);
    } catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info(
                "Got expected exception when executing query: " + ex);
      } else {
        fail("Got unexpected exception when executing query", ex);
      }
    }
    try {
      String queryString = "SELECT DISTINCT * FROM " + region.getFullPath();
      Query query = null;
      if (multiUserAuthMode) {
        query = proxyCaches[multiUserIndex].getQueryService().newQuery(queryString);
      }
      else {
        region.getCache().getQueryService().newQuery(queryString);
      }
      SelectResults result = (SelectResults)query.execute();
      if (expectedResult.intValue() != NO_EXCEPTION) {
        fail("Expected a NotAuthorizedException while executing function");
      }
      assertEquals(expectedValue.intValue(), result.asList().size());
    } catch (NoAvailableServersException ex) {
      if (expectedResult.intValue() == NO_AVAILABLE_SERVERS) {
        getLogWriter().info(
                "Got expected NoAvailableServers when executing query: "
                        + ex.getCause());
      } else {
        fail("Got unexpected exception when executing query", ex);
      }
    } catch (ServerConnectivityException ex) {
      if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
              && (ex.getCause() instanceof NotAuthorizedException)) {
        getLogWriter().info(
                "Got expected NotAuthorizedException when executing query: "
                        + ex.getCause());
      } else if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info(
                "Got expected exception when executing query: " + ex);
      } else {
        fail("Got unexpected exception when executing query", ex);
      }
    } catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info(
                "Got expected exception when executing query: " + ex);
      } else {
        fail("Got unexpected exception when executing query", ex);
      }
    }
  }

  private static void doFunctionExecuteP(final Integer multiUserIndex,
                                         final Function function, Integer expectedResult, final Object expectedValue,
                                         final String method) {
    Region region = null;
    try {
      if (multiUserAuthMode) {
        region = proxyCaches[multiUserIndex].getRegion(REGION_NAME);
      } else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNotNull(region);
    } catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info(
                "Got expected exception when executing function: " + ex);
      } else {
        fail("Got unexpected exception when executing function", ex);
      }
    }
    try {
      FunctionService.registerFunction(function);
      Execution execution = null;
      if ("region".equals(method)) {
        execution = FunctionService.onRegion(region);
      } else if ("server".equals(method)) {
        if (multiUserAuthMode) {
          execution = FunctionService.onServer(proxyCaches[multiUserIndex]);
        } else {
          execution = FunctionService.onServer(pool);
        }
      } else { // if ("servers".equals(method)) {
        if (multiUserAuthMode) {
          execution = FunctionService.onServers(proxyCaches[multiUserIndex]);
        } else {
          execution = FunctionService.onServers(pool);
        }
      }
      execution.execute(function.getId());
      if (expectedResult.intValue() != NO_EXCEPTION) {
        fail("Expected a NotAuthorizedException while executing function");
      }
    } catch (NoAvailableServersException ex) {
      if (expectedResult.intValue() == NO_AVAILABLE_SERVERS) {
        getLogWriter().info(
                "Got expected NoAvailableServers when executing function: "
                        + ex.getCause());
      } else {
        fail("Got unexpected exception when executing function", ex);
      }
    } catch (ServerConnectivityException ex) {
      if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
              && (ex.getCause() instanceof NotAuthorizedException)) {
        getLogWriter().info(
                "Got expected NotAuthorizedException when executing function: "
                        + ex.getCause());
      } else if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info(
                "Got expected exception when executing function: " + ex);
      } else {
        fail("Got unexpected exception when executing function", ex);
      }
    } catch (FunctionException ex) {
      if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
              && ((ex.getCause() instanceof NotAuthorizedException) || ((ex
              .getCause() instanceof ServerOperationException) && (((ServerOperationException)ex
              .getCause()).getCause() instanceof NotAuthorizedException)))) {
        getLogWriter().info(
                "Got expected NotAuthorizedException when executing function: "
                        + ex.getCause());
      } else if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info(
                "Got expected exception when executing function: " + ex);
      } else {
        fail("Got unexpected exception when executing function", ex);
      }
    } catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info(
                "Got expected exception when executing function: " + ex);
      } else {
        fail("Got unexpected exception when executing function", ex);
      }
    }
  }

  private static void doQueriesP(final Integer multiUserIndex,
                                 final Integer expectedResult, final Integer expectedValue) {
    Region region = null;
    try {
      if (multiUserAuthMode) {
        region = proxyCaches[multiUserIndex].getRegion(REGION_NAME);
      } else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNotNull(region);
    } catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing queries: " + ex);
      } else {
        fail("Got unexpected exception when doing queries", ex);
      }
    }
    String queryStr = "SELECT DISTINCT * FROM " + region.getFullPath();
    try {
      SelectResults queryResults = region.query(queryStr);
      Set resultSet = queryResults.asSet();
      assertEquals(expectedValue.intValue(), resultSet.size());
      if (expectedResult.intValue() != NO_EXCEPTION) {
        fail("Expected a NotAuthorizedException while doing queries");
      }
    } catch (NoAvailableServersException ex) {
      if (expectedResult.intValue() == NO_AVAILABLE_SERVERS) {
        getLogWriter().info(
                "Got expected NoAvailableServers when doing queries: "
                        + ex.getCause());
      } else {
        fail("Got unexpected exception when doing queries", ex);
      }
    } catch (ServerConnectivityException ex) {
      if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
              && (ex.getCause() instanceof NotAuthorizedException)) {
        getLogWriter().info(
                "Got expected NotAuthorizedException when doing queries: "
                        + ex.getCause());
      } else if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing queries: " + ex);
      } else {
        fail("Got unexpected exception when doing queries", ex);
      }
    } catch (QueryInvocationTargetException qite) {
      if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
              && (qite.getCause() instanceof NotAuthorizedException)) {
        getLogWriter().info(
                "Got expected NotAuthorizedException when doing queries: "
                        + qite.getCause());
      } else if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing queries: " + qite);
      } else {
        fail("Got unexpected exception when doing queries", qite);
      }
    } catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing queries: " + ex);
      } else {
        fail("Got unexpected exception when doing queries", ex);
      }
    }
  }

  private static void doContainsKeysP(final Integer num, final Integer multiUserIndex,
                                      final Integer expectedResult, final boolean newVals, final boolean expectedValue) {

    assertTrue(num.intValue() <= KEYS.length);
    Region region = null;
    try {
      if (multiUserAuthMode) {
        region = proxyCaches[multiUserIndex].getRegion(REGION_NAME);
      }
      else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNotNull(region);
    }
    catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing containsKey: " + ex);
      }
      else {
        fail("Got unexpected exception when doing containsKey", ex);
      }
    }
    for (int index = 0; index < num.intValue(); ++index) {
      boolean result = false;
      try {
        result = region.containsKeyOnServer(KEYS[index]);
        if (expectedResult.intValue() != NO_EXCEPTION) {
          fail("Expected a NotAuthorizedException while doing containsKey");
        }
      }
      catch(NoAvailableServersException ex) {
        if(expectedResult.intValue() == NO_AVAILABLE_SERVERS) {
          getLogWriter().info(
                  "Got expected NoAvailableServers when doing containsKey: "
                          + ex.getCause());
          continue;
        }
        else {
          fail("Got unexpected exception when doing containsKey", ex);
        }
      }
      catch (ServerConnectivityException ex) {
        if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
                && (ex.getCause() instanceof NotAuthorizedException)) {
          getLogWriter().info(
                  "Got expected NotAuthorizedException when doing containsKey: "
                          + ex.getCause());
          continue;
        }
        else if (expectedResult.intValue() == OTHER_EXCEPTION) {
          getLogWriter().info("Got expected exception when doing containsKey: " + ex);
        }
        else {
          fail("Got unexpected exception when doing containsKey", ex);
        }
      }
      catch (Exception ex) {
        if (expectedResult.intValue() == OTHER_EXCEPTION) {
          getLogWriter().info("Got expected exception when doing containsKey: " + ex);
        }
        else {
          fail("Got unexpected exception when doing containsKey", ex);
        }
      }
      assertEquals(expectedValue, result);
    }
  }

  private static void doInvalidatesP(final Integer num, final Integer multiUserIndex,
                                     final Integer expectedResult, final boolean newVals) {
    assertTrue(num.intValue() <= KEYS.length);
    Region region = null;
    try {
      if (multiUserAuthMode) {
        region = proxyCaches[multiUserIndex].getRegion(REGION_NAME);
      }
      else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNotNull(region);
    }
    catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing invalidates: " + ex);
      }
      else {
        fail("Got unexpected exception when doing invalidates", ex);
      }
    }
    for (int index = 0; index < num.intValue(); ++index) {
      try {
        region.invalidate(KEYS[index]);
        if (expectedResult.intValue() != NO_EXCEPTION) {
          fail("Expected a NotAuthorizedException while doing invalidates");
        }
      }
      catch(NoAvailableServersException ex) {
        if(expectedResult.intValue() == NO_AVAILABLE_SERVERS) {
          getLogWriter().info(
                  "Got expected NoAvailableServers when doing invalidates: "
                          + ex.getCause());
          continue;
        }
        else {
          fail("Got unexpected exception when doing invalidates", ex);
        }
      }
      catch (ServerConnectivityException ex) {
        if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
                && (ex.getCause() instanceof NotAuthorizedException)) {
          getLogWriter().info(
                  "Got expected NotAuthorizedException when doing invalidates: "
                          + ex.getCause());
          continue;
        }
        else if (expectedResult.intValue() == OTHER_EXCEPTION) {
          getLogWriter().info("Got expected exception when doing invalidates: " + ex);
        }
        else {
          fail("Got unexpected exception when doing invalidates", ex);
        }
      }
      catch (Exception ex) {
        if (expectedResult.intValue() == OTHER_EXCEPTION) {
          getLogWriter().info("Got expected exception when doing invalidates: " + ex);
        }
        else {
          fail("Got unexpected exception when doing invalidates", ex);
        }
      }
    }
  }

  private static void doDestroysP(final Integer num, final Integer multiUserIndex,
                                  final Integer expectedResult, final boolean newVals) {
    assertTrue(num.intValue() <= KEYS.length);
    Region region = null;
    try {
      if (multiUserAuthMode) {
        region = proxyCaches[multiUserIndex].getRegion(REGION_NAME);
      }
      else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNotNull(region);
    }
    catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing destroys: " + ex);
      }
      else {
        fail("Got unexpected exception when doing destroys", ex);
      }
    }
    for (int index = 0; index < num.intValue(); ++index) {
      try {
        region.destroy(KEYS[index]);
        if (expectedResult.intValue() != NO_EXCEPTION) {
          fail("Expected a NotAuthorizedException while doing destroys");
        }
      }
      catch(NoAvailableServersException ex) {
        if(expectedResult.intValue() == NO_AVAILABLE_SERVERS) {
          getLogWriter().info(
                  "Got expected NoAvailableServers when doing destroys: "
                          + ex.getCause());
          continue;
        }
        else {
          fail("Got unexpected exception when doing destroys", ex);
        }
      }
      catch (ServerConnectivityException ex) {
        if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
                && (ex.getCause() instanceof NotAuthorizedException)) {
          getLogWriter().info(
                  "Got expected NotAuthorizedException when doing destroys: "
                          + ex.getCause());
          continue;
        }
        else if (expectedResult.intValue() == OTHER_EXCEPTION) {
          getLogWriter().info("Got expected exception when doing destroys: " + ex);
        }
        else {
          fail("Got unexpected exception when doing destroys", ex);
        }
      }
      catch (Exception ex) {
        if (expectedResult.intValue() == OTHER_EXCEPTION) {
          getLogWriter().info("Got expected exception when doing destroys: " + ex);
        }
        else {
          fail("Got unexpected exception when doing destroys", ex);
        }
      }
    }
  }

  private static void doRegionDestroysP(final Integer multiuserIndex,
                                        final Integer expectedResult) {
    Region region = null;
    try {
      if (multiUserAuthMode) {
        region = proxyCaches[multiuserIndex].getRegion(REGION_NAME);
      } else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNotNull(region);
    } catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info(
                "Got expected exception when doing region destroy: " + ex);
      } else {
        fail("Got unexpected exception when doing region destroy", ex);
      }
    }

    try {
      region.destroyRegion();
      if (expectedResult.intValue() != NO_EXCEPTION) {
        fail("Expected a NotAuthorizedException while doing region destroy");
      }
      if (multiUserAuthMode) {
        region = proxyCaches[multiuserIndex].getRegion(REGION_NAME);
      } else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNull(region);
    } catch (NoAvailableServersException ex) {
      if (expectedResult.intValue() == NO_AVAILABLE_SERVERS) {
        getLogWriter().info(
                "Got expected NoAvailableServers when doing region destroy: "
                        + ex.getCause());
      } else {
        fail("Got unexpected exception when doing region destroy", ex);
      }
    } catch (ServerConnectivityException ex) {
      if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
              && (ex.getCause() instanceof NotAuthorizedException)) {
        getLogWriter().info(
                "Got expected NotAuthorizedException when doing region destroy: "
                        + ex.getCause());
      } else if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info(
                "Got expected exception when doing region destroy: " + ex);
      } else {
        fail("Got unexpected exception when doing region destroy", ex);
      }
    } catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info(
                "Got expected exception when doing region destroy: " + ex);
      } else {
        fail("Got unexpected exception when doing region destroy", ex);
      }
    }
  }

  private static void doLocalGetsP(final int num, final boolean checkNVals) {
    assertTrue(num <= KEYS.length);
    String[] vals = VALUES;
    if (checkNVals) {
      vals = NVALUES;
    }
    final Region region = getCache().getRegion(REGION_NAME);
    assertNotNull(region);
    for (int index = 0; index < num; ++index) {
      final String key = KEYS[index];
      final String expectedVal = vals[index];
      waitForCondition(new Callable() {
        public Object call() throws Exception {
          Object value = getLocalValue(region, key);
          return Boolean.valueOf(expectedVal.equals(value));
        }
      }, 1000, 30 / num);
    }
    for (int index = 0; index < num; ++index) {
      Region.Entry entry = region.getEntry(KEYS[index]);
      assertNotNull(entry);
      assertEquals(vals[index], entry.getValue());
    }
  }

  private static void doGetAllP(final Integer multiUserIndex,
                                final Integer expectedResult, final boolean useTX) {
    Region region = null;
    try {
      if (multiUserAuthMode) {
        region = proxyCaches[multiUserIndex].getRegion(REGION_NAME);
      }
      else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNotNull(region);
    }
    catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing getAll: " + ex);
      }
      else {
        fail("Got unexpected exception when doing getAll", ex);
      }
    }
    try {
      List keys = new ArrayList();
      keys.add("key1");
      keys.add("key2");
      if (useTX) {
        getCache().getCacheTransactionManager().begin();
      }
      Map entries = region.getAll(keys);
      // Also check getEntry()
      region.getEntry("key1");
      if (useTX) {
        getCache().getCacheTransactionManager().commit();
      }
      assertNotNull(entries);
      if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)) {
        assertEquals(0, entries.size());
      } else if ((expectedResult.intValue() == NO_EXCEPTION)) {
        assertEquals(2, entries.size());
        assertEquals("value1", entries.get("key1"));
        assertEquals("value2", entries.get("key2"));
      }
    } catch (NoAvailableServersException ex) {
      if (expectedResult.intValue() == NO_AVAILABLE_SERVERS) {
        getLogWriter().info(
                "Got expected NoAvailableServers when doing getAll: "
                        + ex.getCause());
      } else {
        fail("Got unexpected exception when doing getAll", ex);
      }
    } catch (ServerConnectivityException ex) {
      if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
              && (ex.getCause() instanceof NotAuthorizedException)) {
        getLogWriter().info(
                "Got expected NotAuthorizedException when doing getAll: "
                        + ex.getCause());
      } else if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing getAll: " + ex);
      } else {
        fail("Got unexpected exception when doing getAll", ex);
      }
    } catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing getAll: " + ex);
      } else {
        fail("Got unexpected exception when doing getAll", ex);
      }
    }
  }

  private static void doGetsP(final Integer num, final Integer expectedResult,
                              final boolean newVals) {
    doGetsP(num, Integer.valueOf(0), expectedResult, newVals);
  }

  private static void doGetsP(final Integer num, final Integer multiUserIndex,
                              final Integer expectedResult, final boolean newVals) {
    assertTrue(num.intValue() <= KEYS.length);
    Region region = null;
    try {
      if (multiUserAuthMode) {
        region = proxyCaches[multiUserIndex].getRegion(REGION_NAME);
      }
      else {
        region = getCache().getRegion(REGION_NAME);
      }
      assertNotNull(region);
    }
    catch (Exception ex) {
      if (expectedResult.intValue() == OTHER_EXCEPTION) {
        getLogWriter().info("Got expected exception when doing gets: " + ex);
      }
      else {
        fail("Got unexpected exception when doing gets", ex);
      }
    }
    for (int index = 0; index < num.intValue(); ++index) {
      Object value = null;
      try {
        try {
          region.localInvalidate(KEYS[index]);
        }
        catch (Exception ex) {
        }
        value = region.get(KEYS[index]);
        if (expectedResult.intValue() != NO_EXCEPTION) {
          fail("Expected a NotAuthorizedException while doing gets");
        }
      }
      catch(NoAvailableServersException ex) {
        if(expectedResult.intValue() == NO_AVAILABLE_SERVERS) {
          getLogWriter().info(
                  "Got expected NoAvailableServers when doing gets: "
                          + ex.getCause());
          continue;
        }
        else {
          fail("Got unexpected exception when doing gets", ex);
        }
      }
      catch (ServerConnectivityException ex) {
        if ((expectedResult.intValue() == NOTAUTHZ_EXCEPTION)
                && (ex.getCause() instanceof NotAuthorizedException)) {
          getLogWriter().info(
                  "Got expected NotAuthorizedException when doing gets: "
                          + ex.getCause());
          continue;
        }
        else if (expectedResult.intValue() == OTHER_EXCEPTION) {
          getLogWriter().info("Got expected exception when doing gets: " + ex);
        }
        else {
          fail("Got unexpected exception when doing gets", ex);
        }
      }
      catch (Exception ex) {
        if (expectedResult.intValue() == OTHER_EXCEPTION) {
          getLogWriter().info("Got expected exception when doing gets: " + ex);
        }
        else {
          fail("Got unexpected exception when doing gets", ex);
        }
      }
      assertNotNull(value);
      if (newVals) {
        assertEquals(NVALUES[index], value);
      }
      else {
        assertEquals(VALUES[index], value);
      }
    }
  }

  // ----------------------------- member methods -----------------------------

  public DistributedSystem createSystem(final Properties sysProps, final Properties javaProps) {
    closeCache();
    clearStaticSSLContext();
    setJavaProps(javaProps);

    DistributedSystem dsys = distributedTestCase.getSystem(sysProps);
    assertNotNull(dsys);
    addIgnoredExceptions(ignoredExceptions);
    return dsys;
  }

  private void openCache() {
    assertNotNull(distributedTestCase.basicGetSystem());
    assertTrue(distributedTestCase.basicGetSystem().isConnected());
    cache = CacheFactory.create(distributedTestCase.basicGetSystem());
    assertNotNull(cache);
  }

  // ------------------------------- inner classes ----------------------------

  private static class Employee implements PdxSerializable {

    private Long Id;
    private String fname;
    private String lname;

    public Employee() {}

    public Employee(Long id, String fn, String ln){
      this.Id = id;
      this.fname = fn;
      this.lname = ln;
    }

    /**
     * For test purpose, to make sure
     * the object is not deserialized
     */
    @Override
    public void fromData(PdxReader in) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void toData(PdxWriter out) {
      out.writeLong("Id", Id);
      out.writeString("fname", fname);
      out.writeString("lname", lname);
    }
  }
}
