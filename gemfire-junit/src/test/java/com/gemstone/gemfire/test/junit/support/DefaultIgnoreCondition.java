package com.gemstone.gemfire.test.junit.support;

import org.junit.runner.Description;
import com.gemstone.gemfire.test.junit.IgnoreCondition;

/**
 * The DefaultIgnoreCondition class...
 *
 * @author John Blum
 * @see org.junit.runner.Description
 * @see com.gemstone.gemfire.test.junit.ConditionalIgnore
 * @see com.gemstone.gemfire.test.junit.IgnoreCondition
 */
@SuppressWarnings("unused")
public class DefaultIgnoreCondition implements IgnoreCondition {

  public static final boolean DEFAULT_IGNORE = false;

  public static final DefaultIgnoreCondition DO_NOT_IGNORE = new DefaultIgnoreCondition(false);
  public static final DefaultIgnoreCondition IGNORE = new DefaultIgnoreCondition(true);

  private final boolean ignore;

  public DefaultIgnoreCondition() {
    this(DEFAULT_IGNORE);
  }

  public DefaultIgnoreCondition(final boolean ignore) {
    this.ignore = ignore;
  }

  public boolean isIgnore() {
    return ignore;
  }

  @Override
  public boolean evaluate(final Description testCaseDescription) {
    return isIgnore();
  }

}
