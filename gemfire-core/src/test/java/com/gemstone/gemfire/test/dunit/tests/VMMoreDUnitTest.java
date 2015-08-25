package com.gemstone.gemfire.test.dunit.tests;

import static org.assertj.core.api.Assertions.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.test.dunit.DistributedTestCase;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;
import com.gemstone.gemfire.test.junit.rules.SerializableTestName;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class VMMoreDUnitTest extends DistributedTestCase {

  @Rule
  public final SerializableTestName testName = new SerializableTestName();
  
  @Before
  public void before() {
    super.logPerTest = true;
  }
  
  @After
  public void after() {
    super.logPerTest = false;
  }
  
  @Test
  public void foo() {
    InternalDistributedSystem mySystem = getSystem();
    
    assertThat(previousSystemCreatedInTestClass).isEqualTo(getClass());
    
    assertThat(previousProperties).containsEntry(DistributionConfig.LOG_FILE_NAME, getUniqueName() + ".log");
    assertThat(previousProperties).containsEntry(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME, getUniqueName() + ".gfs");
  }
  
  @Test
  public void bar() {
    InternalDistributedSystem mySystem = getSystem();
    
    assertThat(previousSystemCreatedInTestClass).isEqualTo(getClass());
    
    assertThat(previousProperties).containsEntry(DistributionConfig.LOG_FILE_NAME, getUniqueName() + ".log");
    assertThat(previousProperties).containsEntry(DistributionConfig.STATISTIC_ARCHIVE_FILE_NAME, getUniqueName() + ".gfs");
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
