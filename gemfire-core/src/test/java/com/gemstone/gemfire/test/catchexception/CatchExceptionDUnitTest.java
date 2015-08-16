package com.gemstone.gemfire.test.catchexception;

import static com.googlecode.catchexception.CatchException.*;
import static com.googlecode.catchexception.apis.BDDCatchException.when;
import static com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers.*;
import static org.assertj.core.api.BDDAssertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.gemstone.gemfire.test.dunit.DistributedTestCase;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.RMIException;
import com.gemstone.gemfire.test.dunit.SerializableCallable;
import com.gemstone.gemfire.test.dunit.VM;

/**
 * Using Catch-Exception works well for remote exceptions and asserting details
 * about root cause of RMIExceptions in DUnit tests.
 */
public class CatchExceptionDUnitTest extends DistributedTestCase {
  private static final long serialVersionUID = 1L;

  private static final String REMOTE_THROW_EXCEPTION_MESSAGE = "Throwing remoteThrowException";

  @Test
  public void testRemoteInvocationWithException() {
    Host host = Host.getHost(0);
    VM vm = host.getVM(0);

    when(vm).invoke(new ThrowBasicTestException());

    then(caughtException())
        .isInstanceOf(RMIException.class)
        .hasCause(new BasicTestException(REMOTE_THROW_EXCEPTION_MESSAGE));
  }
  
  protected static class ThrowBasicTestException extends SerializableCallable<Object> {
    private static final long serialVersionUID = 1L;
    
    @Override
    public Object call() throws Exception {
      throw new BasicTestException(REMOTE_THROW_EXCEPTION_MESSAGE);
    }
  }
  
  protected static class BasicTestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BasicTestException() {
      super();
    }
    
    public BasicTestException(String message) {
      super(message);
    }
  }
}
