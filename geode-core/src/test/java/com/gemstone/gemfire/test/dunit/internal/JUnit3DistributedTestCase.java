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
package com.gemstone.gemfire.test.dunit.internal;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;
import junit.framework.TestCase;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

/**
 * This class is the superclass of all distributed tests using JUnit 3.
 *
 * tests/hydra/JUnitTestTask is the main DUnit driver. It supports two
 * additional public static methods if they are defined in the test case:
 *
 * public static void caseSetUp() -- comparable to JUnit's BeforeClass annotation
 *
 * public static void caseTearDown() -- comparable to JUnit's AfterClass annotation
 */
@Category(DistributedTest.class)
public abstract class JUnit3DistributedTestCase extends TestCase implements DistributedTestFixture, Serializable {

  private final JUnit4DistributedTestCase delegate = new JUnit4DistributedTestCase(this);

//  private static final Logger logger = LogService.getLogger();
//
//  private static final Set<String> testHistory = new LinkedHashSet<String>();
//
//  /** This VM's connection to the distributed system */
//  public static InternalDistributedSystem system;
//  private static Class lastSystemCreatedInTest;
//  private static Properties lastSystemProperties;
//  private static volatile String testMethodName;
//
//  /** For formatting timing info */
//  private static final DecimalFormat format = new DecimalFormat("###.###");
//
//  public static boolean reconnect = false;
//
//  public static final boolean logPerTest = Boolean.getBoolean("dunitLogPerTest");

  static {
    JUnit4DistributedTestCase.initializeDistributedTestCase();
  }

  /**
   * Creates a new <code>JUnit3DistributedTestCase</code> test with the
   * given name.
   */
  public JUnit3DistributedTestCase(final String name) {
    super(name);
  }

  //---------------------------------------------------------------------------
  // methods for tests
  //---------------------------------------------------------------------------

  public final void setSystem(final Properties props, final DistributedSystem ds) { // TODO: override getDistributedSystemProperties and then delete
    delegate.setSystem(props, ds);
  }

  /**
   * Returns this VM's connection to the distributed system.  If
   * necessary, the connection will be lazily created using the given
   * <code>Properties</code>.  Note that this method uses hydra's
   * configuration to determine the location of log files, etc.
   * Note: "final" was removed so that WANTestBase can override this method.
   * This was part of the xd offheap merge.
   *
   * see hydra.DistributedConnectionMgr#connect
   * @since 3.0
   */
  public /*final*/ InternalDistributedSystem getSystem(final Properties props) { // TODO: make final
    return delegate.getSystem(props);
  }

  /**
   * Returns this VM's connection to the distributed system.  If
   * necessary, the connection will be lazily created using the
   * <code>Properties</code> returned by {@link
   * #getDistributedSystemProperties}.
   *
   * @see #getSystem(Properties)
   *
   * @since 3.0
   */
  public final InternalDistributedSystem getSystem() {
    return delegate.getSystem();
  }

  /**
   * Returns a loner distributed system that isn't connected to other
   * vms
   *
   * @since 6.5
   */
  public final InternalDistributedSystem getLonerSystem() {
    return delegate.getLonerSystem();
  }

  /**
   * Returns whether or this VM is connected to a {@link
   * DistributedSystem}.
   */
  public final boolean isConnectedToDS() {
    return delegate.isConnectedToDS();
  }

  /**
   * Returns a <code>Properties</code> object used to configure a
   * connection to a {@link
   * com.gemstone.gemfire.distributed.DistributedSystem}.
   * Unless overridden, this method will return an empty
   * <code>Properties</code> object.
   *
   * @since 3.0
   */
  public Properties getDistributedSystemProperties() {
    return delegate.getDistributedSystemProperties();
  }

  public static void disconnectAllFromDS() {
    JUnit4DistributedTestCase.disconnectAllFromDS();
  }

  /**
   * Disconnects this VM from the distributed system
   */
  public static void disconnectFromDS() {
    JUnit4DistributedTestCase.disconnectFromDS();
  }

  //---------------------------------------------------------------------------
  // name methods
  //---------------------------------------------------------------------------

  public static String getTestMethodName() {
    return JUnit4DistributedTestCase.getTestMethodName();
  }

  public static void setTestMethodName(final String testMethodName) { // TODO: delete
    JUnit4DistributedTestCase.setTestMethodName(testMethodName);
  }

  /**
   * Returns a unique name for this test method.  It is based on the
   * name of the class as well as the name of the method.
   */
  public String getUniqueName() {
    return delegate.getUniqueName();
  }

  //---------------------------------------------------------------------------
  // setup methods
  //---------------------------------------------------------------------------

  /**
   * Sets up the DistributedTestCase.
   * <p>
   * Do not override this method. Override {@link #preSetUp()} with work that
   * needs to occur before setUp() or override {@link #postSetUp()} with work
   * that needs to occur after setUp().
   */
  @Override
  public final void setUp() throws Exception {
    delegate.setUp();
  }

  /**
   * {@code preSetUp()} is invoked before {@link JUnit4DistributedTestCase#setUpDistributedTestCase()}.
   *
   * <p>Override this as needed. Default implementation is empty.
   */
  public void preSetUp() throws Exception {
  }

  /**
   * {@code postSetUp()} is invoked after {@link JUnit4DistributedTestCase#setUpDistributedTestCase()}.
   *
   * <p>Override this as needed. Default implementation is empty.
   */
  public void postSetUp() throws Exception {
  }

  //---------------------------------------------------------------------------
  // teardown methods
  //---------------------------------------------------------------------------

  /**
   * Tears down the DistributedTestCase.
   * <p>
   * Do not override this method. Override {@link #preTearDown()} with work that
   * needs to occur before tearDown() or override {@link #postTearDown()} with work
   * that needs to occur after tearDown().
   */
  @Override
  public final void tearDown() throws Exception {
    delegate.tearDown();
  }

  /**
   * {@code preTearDown()} is invoked before {@link JUnit4DistributedTestCase#tearDownDistributedTestCase()}.
   *
   * <p>Override this as needed. Default implementation is empty.
   */
  public void preTearDown() throws Exception {
  }

  /**
   * {@code postTearDown()} is invoked after {@link JUnit4DistributedTestCase#tearDownDistributedTestCase()}.
   *
   * <p>Override this as needed. Default implementation is empty.
   */
  public void postTearDown() throws Exception {
  }

  public static void cleanupAllVms() { // TODO: make private
    JUnit4DistributedTestCase.cleanupAllVms();
  }
}
