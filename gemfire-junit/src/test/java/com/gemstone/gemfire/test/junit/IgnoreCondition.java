package com.gemstone.gemfire.test.junit;

import org.junit.runner.Description;

/**
 * The IgnoreCondition class...
 *
 * @author John Blum
 * @see org.junit.runner.Description
 */
@SuppressWarnings("unused")
public interface IgnoreCondition {

  boolean evaluate(Description testCaseDescription);

}
