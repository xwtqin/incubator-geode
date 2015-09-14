package com.gemstone.gemfire.test.junit.rules;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Serializable version of ExternalResource JUnit Rule. JUnit lifecycle is not
 * executed in remote JVMs. The <tt>after()</tt> callback has a throws-clause 
 * that matches <tt>before()</tt>.
 * 
 * Implementation copied from <tt>org.junit.rules.ExternalResource</tt>.
 * 
 * @author Kirk Lund
 */
@SuppressWarnings("serial")
public abstract class SerializableExternalResource implements SerializableTestRule {
  
  @Override
  public Statement apply(final Statement base, final Description description) {
    if (description.isTest()) {
      return statement(base);
    } else if (description.isSuite()) {
      return statementClass(base);
    }
    return base;
  }

  private Statement statement(final Statement base) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        before();
        try {
          base.evaluate();
        } finally {
          after();
        }
      }
    };
  }

  private Statement statementClass(final Statement base) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        beforeClass();
        try {
          base.evaluate();
        } finally {
          afterClass();
        }
      }
    };
  }

  /**
   * Override to set up your specific external resource.
   *
   * @throws Throwable if setup fails (which will disable {@code after}
   */
  protected void before() throws Throwable {
    // do nothing
  }

  /**
   * Override to tear down your specific external resource.
   * 
   * @throws Throwable if teardown fails (which will disable {@code after}
   */
  protected void after() throws Throwable {
    // do nothing
  }

  /**
   * Override to set up your specific external resource.
   *
   * @throws Throwable if setup fails (which will disable {@code after}
   */
  protected void beforeClass() throws Throwable {
    // do nothing
  }

  /**
   * Override to tear down your specific external resource.
   *
   * @throws Throwable if teardown fails (which will disable {@code after}
   */
  protected void afterClass() throws Throwable {
    // do nothing
  }
}
