package com.gemstone.gemfire.test.dunit.rules.tests;

import static com.gemstone.gemfire.test.junit.rules.tests.RunTest.*;
import static java.lang.System.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.Result;

import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.dunit.rules.DistributedRestoreSystemProperties;
import com.gemstone.gemfire.test.dunit.rules.RemoteInvoker;
import com.gemstone.gemfire.test.junit.categories.UnitTest;

/**
 * Unit tests for DistributedRestoreSystemProperties
 * 
 * @author Kirk Lund
 */
@Category(UnitTest.class)
public class DistributedRestoreSystemPropertiesJUnitTest {

  private static final String SOME_PROPERTY = "SOME_PROPERTY"; 
  private static final String SOME_PROPERTY_ORIG_VALUE = "SOME_PROPERTY_ORIG_VALUE"; 
  private static final String SOME_PROPERTY_NEW_VALUE = "SOME_PROPERTY_NEW_VALUE"; 

  private static final AtomicReference<RemoteInvoker> remoteInvokerRef = new AtomicReference<RemoteInvoker>();
  
  @Before
  public void before() {
    setProperty(SOME_PROPERTY, SOME_PROPERTY_ORIG_VALUE);
    remoteInvokerRef.set(mock(RemoteInvoker.class));
  }
  
  @After
  public void after() {
    clearProperty(SOME_PROPERTY);
  }
  
  @Test
  public void shouldResetOriginalValue() {
    Result result = runTest(SetProperty.class);
    
    assertThat(result.wasSuccessful()).isTrue();
    assertThat(getProperty(SOME_PROPERTY)).isEqualTo(SOME_PROPERTY_ORIG_VALUE);
  }
  
  @Test
  public void shouldInvokeRemoteInvoker() {
    Result result = runTest(SetProperty.class);
    
    assertThat(result.wasSuccessful()).isTrue();
    verify(remoteInvokerRef.get(), times(2)).remoteInvokeInEveryVMAndLocator(isA(SerializableRunnable.class));
  }
  
  public static class SetProperty {
    
    @Rule
    public final DistributedRestoreSystemProperties ruleChain = new DistributedRestoreSystemProperties(remoteInvokerRef.get());

    @Test
    public void doTest() throws Exception {
      setProperty(SOME_PROPERTY, SOME_PROPERTY_NEW_VALUE);
    }
  }
}
