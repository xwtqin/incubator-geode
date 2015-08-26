package com.gemstone.gemfire.test.dunit.tests;


import static com.gemstone.gemfire.test.dunit.DUnitTestRule.*;
import static org.assertj.core.api.Assertions.*;

import java.io.Serializable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.test.dunit.DUnitTestRule;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class LogPerTestClassTwoDUnitTest implements Serializable {

  @Rule
  public final DUnitTestRule dunitTestRule = DUnitTestRule.builder().logPerTestClass(true).build();

  @Test
  public void logFileNameShouldEqualThisClassName() {
    InternalDistributedSystem mySystem = getSystem();
    
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.LOG_FILE_NAME, getTestClassName() + ".log");
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME, getTestClassName() + ".gfs");
  }
  
  @Test
  public void logFileNameShouldChangeToThisClassName() {
    InternalDistributedSystem mySystem = getSystem();
    
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.LOG_FILE_NAME, getTestClassName() + ".log");
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME, getTestClassName() + ".gfs");
  }
}
