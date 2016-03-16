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
package com.gemstone.gemfire.test.dunit;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.gemstone.gemfire.test.dunit.internal.DistributedTestFixture;
import com.gemstone.gemfire.test.dunit.internal.JUnit3DistributedTestCase;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.SystemFailure;
import com.gemstone.gemfire.admin.internal.AdminDistributedSystemImpl;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.hdfs.internal.hoplog.HoplogConfig;
import com.gemstone.gemfire.cache.query.QueryTestUtils;
import com.gemstone.gemfire.cache.query.internal.QueryObserverHolder;
import com.gemstone.gemfire.cache30.ClientServerTestCase;
import com.gemstone.gemfire.cache30.GlobalLockingDUnitTest;
import com.gemstone.gemfire.cache30.MultiVMRegionTestCase;
import com.gemstone.gemfire.cache30.RegionTestCase;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.DistributionMessageObserver;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem.CreationStackGenerator;
import com.gemstone.gemfire.distributed.internal.tcpserver.TcpClient;
import com.gemstone.gemfire.internal.SocketCreator;
import com.gemstone.gemfire.internal.admin.ClientStatsManager;
import com.gemstone.gemfire.internal.cache.DiskStoreObserver;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.internal.cache.HARegion;
import com.gemstone.gemfire.internal.cache.InitialImageOperation;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.tier.InternalClientMembership;
import com.gemstone.gemfire.internal.cache.tier.sockets.CacheServerTestUtil;
import com.gemstone.gemfire.internal.cache.tier.sockets.ClientProxyMembershipID;
import com.gemstone.gemfire.internal.cache.xmlcache.CacheCreation;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.management.internal.cli.LogWrapper;
import com.gemstone.gemfire.test.dunit.standalone.DUnitLauncher;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

import junit.framework.TestCase;

/**
 * This class is the superclass of all distributed unit tests.
 *
 * @author David Whitlock
 */
@Category(DistributedTest.class)
@SuppressWarnings("serial")
public abstract class DistributedTestCase extends JUnit3DistributedTestCase implements DistributedTestFixture, Serializable {
  
  /**
   * Creates a new <code>DistributedTestCase</code> test with the given name.
   */
  public DistributedTestCase(final String name) {
    super(name);
    DUnitLauncher.launchIfNeeded();
  }
}
