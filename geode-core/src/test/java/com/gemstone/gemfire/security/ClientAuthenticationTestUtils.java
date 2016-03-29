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

import static com.gemstone.gemfire.security.SecurityTestUtils.*;
import static org.junit.Assert.*;

import java.util.Properties;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;

/**
 * Extracted from ClientAuthenticationDUnitTest
 */
public abstract class ClientAuthenticationTestUtils {

  protected ClientAuthenticationTestUtils() {
  }

  protected static Integer createCacheServer(int dsPort, String locatorString, String authenticator, Properties extraProps, Properties javaProps) {
    Properties authProps;
    if (extraProps == null) {
      authProps = new Properties();
    } else {
      authProps = extraProps;
    }

    if (authenticator != null) {
      authProps.setProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, authenticator);
    }

    return SecurityTestUtils.createCacheServer(authProps, javaProps, dsPort, locatorString, 0, NO_EXCEPTION);
  }

  protected static void createCacheServer(int dsPort, String locatorString, int serverPort, String authenticator, Properties extraProps, Properties javaProps) {
    Properties authProps;
    if (extraProps == null) {
      authProps = new Properties();
    } else {
      authProps = extraProps;
    }

    if (authenticator != null) {
      authProps.setProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, authenticator);
    }
    SecurityTestUtils.createCacheServer(authProps, javaProps, dsPort, locatorString, serverPort, NO_EXCEPTION);
  }

  protected static void createCacheClient(String authInit, Properties authProps, Properties javaProps, int[] ports, int numConnections, boolean multiUserMode, boolean subscriptionEnabled, int expectedResult) {
    SecurityTestUtils.createCacheClient(authInit, authProps, javaProps, ports, numConnections, false, multiUserMode, subscriptionEnabled, expectedResult);
  }

  protected static void createCacheClient(String authInit, Properties authProps, Properties javaProps, int[] ports, int numConnections, boolean multiUserMode, int expectedResult) {
    createCacheClient(authInit, authProps, javaProps, ports, numConnections, multiUserMode, true, expectedResult);
  }

  protected static void createCacheClient(String authInit, Properties authProps, Properties javaProps, int port1, int numConnections, int expectedResult) {
    createCacheClient(authInit, authProps, javaProps, new int[] { port1 }, numConnections, Boolean.FALSE, Boolean.TRUE, expectedResult);
  }

  protected static void createCacheClient(String authInit, Properties authProps, Properties javaProps, int port1, int port2, int numConnections, int expectedResult) {
    createCacheClient(authInit, authProps, javaProps, port1, port2, numConnections, Boolean.FALSE, expectedResult);
  }

  protected static void createCacheClient(String authInit, Properties authProps, Properties javaProps, Integer port1, Integer port2, int numConnections, boolean multiUserMode, int expectedResult) {
    createCacheClient(authInit, authProps, javaProps, port1, port2, numConnections, multiUserMode, Boolean.TRUE, expectedResult);
  }

  protected static void createCacheClient(String authInit, Properties authProps, Properties javaProps, int port1, int port2, int numConnections, boolean multiUserMode, boolean subscriptionEnabled, int expectedResult) {
    createCacheClient(authInit, authProps, javaProps, new int[] { port1, port2 }, numConnections, multiUserMode, subscriptionEnabled, expectedResult);
  }

  protected static void registerAllInterest() {
    Region region = getCache().getRegion(REGION_NAME);
    assertNotNull(region);
    region.registerInterestRegex(".*");
  }
}
