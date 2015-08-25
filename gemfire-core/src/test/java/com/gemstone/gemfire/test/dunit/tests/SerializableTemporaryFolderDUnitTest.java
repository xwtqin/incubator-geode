package com.gemstone.gemfire.test.dunit.tests;

import static com.gemstone.gemfire.test.dunit.Invoke.invokeInEveryVM;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.Serializable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.internal.lang.reflect.ReflectionUtils;
import com.gemstone.gemfire.test.dunit.DUnitTestRule;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.junit.categories.DistributedTest;
import com.gemstone.gemfire.test.junit.rules.SerializableTemporaryFolder;

@Category(DistributedTest.class)
@SuppressWarnings("serial")
public class SerializableTemporaryFolderDUnitTest implements Serializable {

  @Rule
  public final DUnitTestRule dunitTestRule = new DUnitTestRule();
  
  @Rule 
  public final SerializableTemporaryFolder temporaryFolder = new SerializableTemporaryFolder();

  @Before
  public void preconditions() {
    assertThat(Host.getHostCount()).isEqualTo(1);
    assertThat(Host.getHost(0).getVMCount()).isEqualTo(4);
  }
  
  @Test
  public void temporaryFolderShouldBeSerializable() throws Exception {
    final File root = this.temporaryFolder.getRoot();
    
    invokeInEveryVM(new SerializableRunnable(ReflectionUtils.getMethodName()) {
      @Override
      public void run() {
        assertThat(temporaryFolder.getRoot()).exists();
        assertThat(temporaryFolder.getRoot()).isEqualTo(root);
      }
    });
  }
}
