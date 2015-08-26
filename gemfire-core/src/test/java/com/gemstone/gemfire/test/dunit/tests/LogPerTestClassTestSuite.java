package com.gemstone.gemfire.test.dunit.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  LogPerTestClassOneDUnitTest.class,
  LogPerTestClassTwoDUnitTest.class,
})
public class LogPerTestClassTestSuite {
}
