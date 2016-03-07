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
package com.gemstone.gemfire.test.dunit.internal;

import java.util.Properties;

import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;

/**
 * Defines the {@code DistributedTestCase} methods that can be overridden by subclasses.
 */
public interface DistributedTestFixture {

  /**
   * {@code preSetUp()} is invoked before {@code DistributedTestCase#setUp()}.
   *
   * <p>Override this as needed. Default implementation is empty.
   */
  public void preSetUp() throws Exception;

  /**
   * {@code postSetUp()} is invoked after {@code DistributedTestCase#setUp()}.
   *
   * <p>Override this as needed. Default implementation is empty.
   */
  public void postSetUp() throws Exception;

  /**
   * {@code preTearDown()} is invoked before {@code DistributedTestCase#tearDown()}.
   *
   * <p>Override this as needed. Default implementation is empty.
   */
  public void preTearDown() throws Exception;

  /**
   * {@code postTearDown()</code> is invoked after {@code DistributedTestCase#tearDown()}.
   *
   * <p>Override this as needed. Default implementation is empty.
   */
  public void postTearDown() throws Exception;

  /**
   * Returns the {@code Properties} used to define the {@code DistributedSystem}.
   *
   * <p>Override this as needed. This method is called by various
   * {@code getSystem} methods in {@code DistributedTestCase}.
   */
  public Properties getDistributedSystemProperties();

  /**
   * @deprecated Please override {@link #getDistributedSystemProperties()} instead. This should be removed.
   */
  public InternalDistributedSystem getSystem(final Properties props); // TODO: remove and make final in DistributedTestCase
}
