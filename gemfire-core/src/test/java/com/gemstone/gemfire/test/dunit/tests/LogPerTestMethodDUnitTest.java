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
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class LogPerTestMethodDUnitTest implements Serializable {

  @Rule
  public final DUnitTestRule dunitTestRule = DUnitTestRule.builder().logPerTestMethod(true).build();

  @Test
  public void getTestClassNameShouldReturnThisClass() {
    assertThat(getTestClassName()).isEqualTo(getClass().getName());
  }
  
  @Test
  public void logFileNameShouldEqualThisMethodName() {
    InternalDistributedSystem mySystem = getSystem();
    
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.LOG_FILE_NAME, getUniqueName() + ".log");
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME, getUniqueName() + ".gfs");
  }
  
  @Test
  public void logFileNameShouldChangeToThisMethodName() {
    InternalDistributedSystem mySystem = getSystem();
    
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.LOG_FILE_NAME, getUniqueName() + ".log");
    assertThat(mySystem.getProperties()).containsEntry(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME, getUniqueName() + ".gfs");
  }
  
  public static class InnerClass {
    public static SerializableRunnable staticSerializableRunnable() {
      return new SerializableRunnable() {
        @Override
        public void run() {
          System.out.println("printing from static SerializableRunnable");
        }
      };
    }
  }
}
