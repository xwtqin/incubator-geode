package com.gemstone.gemfire.distributed;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  DistributedMemberDUnitTest.class,
  HostedLocatorsDUnitTest.class,
})
/**
 * Suite of tests for Membership.
 * 
 * @author Kirk Lund
 */
public class MembershipTestSuite {
}
