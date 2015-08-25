package com.gemstone.gemfire.test.dunit.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.test.dunit.DistributedTestCase;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class LogPerTestTwoDUnitTest extends DistributedTestCase {

  @Before
  public void before() {
    super.logPerTest = true;
  }
  
  @After
  public void after() {
    super.logPerTest = false;
  }
  
  @Test
  public void logPerTestShouldUseUniqueName() {
    InternalDistributedSystem mySystem = getSystem();
    assertThat(mySystem).isNotNull();
    
    assertThat(previousSystemCreatedInTestClass).isEqualTo(getClass());
    
    assertThat(previousProperties).containsEntry(DistributionConfig.LOG_FILE_NAME, getUniqueName() + ".log");
    assertThat(previousProperties).containsEntry(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME, getUniqueName() + ".gfs");
  }
}
