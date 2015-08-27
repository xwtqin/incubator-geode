package com.gemstone.gemfire.test.junit.rules.examples;

import static org.assertj.core.api.Assertions.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.test.junit.categories.UnitTest;
import com.gemstone.gemfire.test.junit.rules.RetryRule;

@Category(UnitTest.class)
public class RetryRuleExampleJUnitTest {

  @Rule
  public final transient RetryRule retry = new RetryRule(2);
  
  private static int count = 0;

  @Test
  public void unreliableTestWithRaceConditions() {
    count++;
    if (count < 2) {
      assertThat(count).isEqualTo(2); // doomed to fail
    }
  }
}
