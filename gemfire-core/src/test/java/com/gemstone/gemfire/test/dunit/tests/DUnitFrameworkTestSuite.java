package com.gemstone.gemfire.test.dunit.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  BasicDUnitTest.class,
  VMDUnitTest.class,
})
/**
 * Suite of tests for the test.dunit DUnit Test framework.
 */
public class DUnitFrameworkTestSuite {

}
