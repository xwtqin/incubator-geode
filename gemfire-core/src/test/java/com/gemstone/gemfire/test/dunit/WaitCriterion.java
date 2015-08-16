package com.gemstone.gemfire.test.dunit;

/**
 * Extracted from DistributedTestCase
 */
public interface WaitCriterion {

  public boolean done();
  
  public String description();
}
