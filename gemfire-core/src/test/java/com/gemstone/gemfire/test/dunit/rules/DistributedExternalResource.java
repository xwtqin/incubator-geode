package com.gemstone.gemfire.test.dunit.rules;

import com.gemstone.gemfire.test.junit.rules.SerializableExternalResource;

@SuppressWarnings("serial")
public class DistributedExternalResource extends SerializableExternalResource {

  private final RemoteInvoker invoker;

  public DistributedExternalResource() {
    this(new RemoteInvoker());
  }
   
  public DistributedExternalResource(final RemoteInvoker invoker) {
    super();
    this.invoker = invoker;
  }

  protected RemoteInvoker invoker() {
    return this.invoker;
  }
  
  @Override
  protected void before() throws Throwable {
    // do nothing
  }

  @Override
  protected void after() throws Throwable {
    // do nothing
  }

  @Override
  protected void beforeClass() throws Throwable {
    // do nothing
  }

  @Override
  protected void afterClass() throws Throwable {
    // do nothing
  }
}
