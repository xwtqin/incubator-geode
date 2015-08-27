package com.gemstone.gemfire.test.junit.rules.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  ExpectedTimeoutRuleJUnitTest.class,
  IgnoreUntilRuleJUnitTest.class,
  RepeatRuleJUnitTest.class,
  RetryRuleGlobalWithErrorJUnitTest.class,
  RetryRuleGlobalWithExceptionJUnitTest.class,
  RetryRuleLocalWithErrorJUnitTest.class,
  RetryRuleLocalWithExceptionJUnitTest.class,
})
public class JUnitRuleTestSuite {
}
