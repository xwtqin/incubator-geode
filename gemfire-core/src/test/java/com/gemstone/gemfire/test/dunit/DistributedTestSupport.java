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

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.internal.InternalDataSerializer;
import com.gemstone.gemfire.internal.InternalInstantiator;
import com.gemstone.gemfire.internal.cache.tier.sockets.DataSerializerPropogationDUnitTest;

public class DistributedTestSupport {

  protected DistributedTestSupport() {
  }

  /**
   * Fetches the GemFireDescription for this test and adds its 
   * DistributedSystem properties to the provided props parameter.
   * 
   * @param config the properties to add hydra's test properties to
   */
  public static void addHydraProperties(Properties config) {
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

  public final static Properties getAllDistributedSystemProperties(Properties props) {
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
   * Get the port that the standard dunit locator is listening on.
   */
  public static int getDUnitLocatorPort() {
    return DUnitEnv.get().getLocatorPort();
  }

  public static void unregisterAllDataSerializersFromAllVms() {
    DistributedTestSupport.unregisterDataSerializerInThisVM();
    Invoke.invokeInEveryVM(new SerializableRunnable() {
      public void run() {
        DistributedTestSupport.unregisterDataSerializerInThisVM();
      }
    });
    Invoke.invokeInLocator(new SerializableRunnable() {
      public void run() {
        DistributedTestSupport.unregisterDataSerializerInThisVM();
      }
    });
  }

  public static void unregisterDataSerializerInThisVM() {
    DataSerializerPropogationDUnitTest.successfullyLoadedTestDataSerializer = false;
    // unregister all the Dataserializers
    InternalDataSerializer.reinitialize();
    // ensure that all are unregistered
    assertEquals(0, InternalDataSerializer.getSerializers().length);
  }

  public static void unregisterInstantiatorsInThisVM() {
    // unregister all the instantiators
    InternalInstantiator.reinitialize();
    assertEquals(0, InternalInstantiator.getInstantiators().length);
  }
}
