package com.gemstone.gemfire.test.dunit;

public interface StoppableWaitCriterion extends WaitCriterion {
  /**
   * If this method returns true then quit waiting even if we are not done.
   * This allows a wait to fail early.
   */
  public boolean stopWaiting();
}