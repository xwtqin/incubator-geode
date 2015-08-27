package com.gemstone.gemfire.test.junit.support;

/**
 * The IgnoreConditionEvaluationException class...
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 */
@SuppressWarnings({ "serial", "unused" })
public class IgnoreConditionEvaluationException extends RuntimeException {

  public IgnoreConditionEvaluationException() {
  }

  public IgnoreConditionEvaluationException(final String message) {
    super(message);
  }

  public IgnoreConditionEvaluationException(final Throwable cause) {
    super(cause);
  }

  public IgnoreConditionEvaluationException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
