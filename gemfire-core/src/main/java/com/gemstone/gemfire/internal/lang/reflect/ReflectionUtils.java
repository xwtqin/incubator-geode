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
}

