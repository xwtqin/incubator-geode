package com.gemstone.gemfire.test.dunit;

public interface WaitCriterion {
  public boolean done();
  public String description();
}