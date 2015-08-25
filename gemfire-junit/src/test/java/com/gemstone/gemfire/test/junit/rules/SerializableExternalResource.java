package com.gemstone.gemfire.test.junit.rules;

import java.io.Serializable;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Serializable version of ExternalResource JUnit Rule. JUnit lifecycle is not
 * executed in remote JVMs. The after() callback has throws-clause that matches
 * before().
 * 
 * @author Kirk Lund
 */
@SuppressWarnings("serial")
public abstract class SerializableExternalResource implements Serializable, TestRule {
  public Statement apply(Statement base, Description description) {
    return statement(base);
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
   */
  protected void after() throws Throwable { // ExternalResource is missing this throws-clause
    // do nothing
  }
}
