package com.gemstone.gemfire.test.junit.rules.tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

/**
 * Used by Rule Unit Tests to execute Test Cases.
 * 
 * @author Kirk Lund
 */
public class RunTest {

  protected RunTest() {
  }
  
  public static Result runTest(Class<?> test) {
    JUnitCore junitCore = new JUnitCore();
    return junitCore.run(Request.aClass(test).getRunner());
  }
}
