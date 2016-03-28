/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gemstone.gemfire.security;

import static com.gemstone.gemfire.security.SecurityTestUtil.*;
import static org.junit.Assert.*;

import java.util.Properties;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;

/**
 * Extracted from ClientAuthenticationDUnitTest
 */
public class ClientAuthenticationUtils {

  protected ClientAuthenticationUtils() {
  }

  public static Integer createCacheServer(int dsPort, String locatorString, String authenticator, Properties extraProps, Properties javaProps) {
    Properties authProps;
    if (extraProps == null) {
      authProps = new Properties();
    } else {
      authProps = (Properties)extraProps;
    }

    if (authenticator != null) {
      authProps.setProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, authenticator.toString());
    }

    return SecurityTestUtil.createCacheServer(authProps, javaProps, dsPort, (String)locatorString, 0, NO_EXCEPTION);
  }

  public static void createCacheServer(int dsPort, String locatorString, int serverPort, String authenticator, Properties extraProps, Properties javaProps) {
    Properties authProps;
    if (extraProps == null) {
      authProps = new Properties();
    } else {
      authProps = (Properties)extraProps;
    }

    if (authenticator != null) {
      authProps.setProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, authenticator.toString());
    }
    SecurityTestUtil.createCacheServer(authProps, javaProps, dsPort, locatorString, serverPort, NO_EXCEPTION);
  }

  public static void createCacheClient(String authInit, Properties authProps, Properties javaProps, int[] ports, int numConnections, boolean multiUserMode, boolean subscriptionEnabled, int expectedResult) {

    SecurityTestUtil.createCacheClient(authInit, authProps, javaProps, ports, numConnections, false, multiUserMode, subscriptionEnabled, expectedResult);
  }

  public static void createCacheClient(String authInit, Properties authProps, Properties javaProps, int[] ports, int numConnections, boolean multiUserMode, int expectedResult) {
    createCacheClient(authInit, (Properties)authProps, (Properties)javaProps, ports, numConnections, multiUserMode, true, expectedResult);
  }

  public static void createCacheClient(String authInit, Properties authProps, Properties javaProps, int port1, int numConnections, int expectedResult) {
    createCacheClient(authInit, authProps, javaProps, new int[] { port1 }, numConnections, Boolean.FALSE, Boolean.TRUE, expectedResult);
  }

  public static void createCacheClient(String authInit, Properties authProps, Properties javaProps, int port1, int port2, int numConnections, int expectedResult) {
    createCacheClient(authInit, authProps, javaProps, port1, port2, numConnections, Boolean.FALSE, expectedResult);
  }

  public static void createCacheClient(String authInit, Properties authProps, Properties javaProps, Integer port1, Integer port2, int numConnections, boolean multiUserMode, int expectedResult) {
    createCacheClient(authInit, authProps, javaProps, port1, port2, numConnections, multiUserMode, Boolean.TRUE, expectedResult);
  }

  public static void createCacheClient(String authInit, Properties authProps, Properties javaProps, int port1, int port2, int numConnections, boolean multiUserMode, boolean subscriptionEnabled, int expectedResult) {
    createCacheClient(authInit, authProps, javaProps, new int[] { port1, port2 }, numConnections, multiUserMode, subscriptionEnabled, expectedResult);
  }

  public static void registerAllInterest() {
    Region region = SecurityTestUtil.getCache().getRegion(SecurityTestUtil.REGION_NAME);
    assertNotNull(region);
    region.registerInterestRegex(".*");
  }
}
