package com.gemstone.gemfire.test.dunit.tests;

import static com.gemstone.gemfire.test.dunit.DistributedTestRule.*;
import static org.assertj.core.api.Assertions.*;

import java.io.Serializable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.test.dunit.DistributedTestRule;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class LogPerTestClassOneDUnitTest implements Serializable {

  @Rule
  public final DistributedTestRule dunitTestRule = DistributedTestRule.builder().logPerTestClass(true).build();

  @Test
  public void getTestClassNameShouldReturnThisClassName() {
    assertThat(getTestClassName()).isEqualTo(getClass().getName());
  }
  
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
