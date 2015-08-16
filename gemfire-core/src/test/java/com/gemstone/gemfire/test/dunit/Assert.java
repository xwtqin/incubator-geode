package com.gemstone.gemfire.test.dunit;

/**
 * Extracted from DistributedTestCase
 */
public class Assert extends org.junit.Assert {

  public static void fail(final String message, final Throwable cause) {
    throw new AssertionError(cause);
  }
}
