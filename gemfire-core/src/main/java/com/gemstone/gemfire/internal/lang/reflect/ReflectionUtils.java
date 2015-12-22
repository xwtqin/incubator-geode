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
package com.gemstone.gemfire.internal.lang.reflect;

/**
 * Utility class for helping in various reflection operations. See the 
 * java.lang.reflect package for the classes that this class utilizes.
 * 
 * TODO: centralize methods from these classes to here:
 * <li>com.gemstone.gemfire.management.internal.cli.util.spring.ReflectionUtils
 * <li>com.gemstone.gemfire.internal.logging.LogService
 * <li>com.gemstone.gemfire.internal.tools.gfsh.app.misc.util.ReflectionUtil
 *  
 * @author Kirk Lund
 * @see com.gemstone.gemfire.internal.tools.gfsh.app.misc.util.ReflectionUtil
 * @see com.gemstone.gemfire.internal.logging.LogService
 * @see com.gemstone.gemfire.management.internal.cli.util.spring.ReflectionUtils
 */
public abstract class ReflectionUtils {

  /**
   * Gets the class name of the caller in the current stack at the given {@code depth}.
   *
   * @param depth a 0-based index in the current stack.
   * @return a class name
   */
  public static String getClassName(final int depth) {
    return Thread.currentThread().getStackTrace()[depth].getClassName();
  }
  
  public static String getClassName() {
    return Thread.currentThread().getStackTrace()[2].getClassName();
  }
  
  public static String getMethodName(final int depth) {
    return Thread.currentThread().getStackTrace()[depth].getMethodName();
  }

  public static String getMethodName() {
    return Thread.currentThread().getStackTrace()[2].getMethodName();
  }
  
  public static String getSimpleClassName(final String className) {
    if (className.indexOf(".") > -1) {
      return className.substring(className.lastIndexOf(".")+1);
    } else {
      return className;
    }
  }

  public static String getSimpleClassName(final int depth) {
    return getSimpleClassName(Thread.currentThread().getStackTrace()[depth].getClassName());
  }

  public static String getSimpleClassName() {
    return getSimpleClassName(Thread.currentThread().getStackTrace()[2].getClassName());
  }
}
