package com.gemstone.gemfire.test.junit.rules.examples;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.gemstone.gemfire.test.junit.Repeat;
import com.gemstone.gemfire.test.junit.rules.RepeatRule;

/**
 * The RepeatingTestCasesExampleTest class is a test suite of test cases testing the contract and functionality
 * of the JUnit @Repeat annotation on a test suite class test case methods.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see com.gemstone.gemfire.test.junit.Repeat
 * @see com.gemstone.gemfire.test.junit.rules.RepeatRule
 */
public class RepeatingTestCasesExampleJUnitTest {

  private static AtomicInteger repeatOnceCounter = new AtomicInteger(0);
  private static AtomicInteger repeatOnlyOnceCounter = new AtomicInteger(0);
  private static AtomicInteger repeatTenTimesCounter = new AtomicInteger(0);
  private static AtomicInteger repeatTwiceCounter = new AtomicInteger(0);

  @Rule
  public RepeatRule repeatRule = new RepeatRule();

  @BeforeClass
  public static void setupBeforeClass() {
    System.setProperty("tdd.example.test.case.two.repetitions", "2");
  }

  @AfterClass
  public static void tearDownAfterClass() {
    assertThat(repeatOnceCounter.get(), is(equalTo(1)));
    assertThat(repeatOnlyOnceCounter.get(), is(equalTo(1)));
    assertThat(repeatTenTimesCounter.get(), is(equalTo(10)));
    assertThat(repeatTwiceCounter.get(), is(equalTo(2)));
  }

  @Test
  @Repeat
  public void repeatOnce() {
    repeatOnceCounter.incrementAndGet();
    assertThat(repeatOnceCounter.get() <= 1, is(true));
  }

  @Test
  @Repeat(property = "tdd.example.test.case.with.non-existing.system.property")
  public void repeatOnlyOnce() {
    repeatOnlyOnceCounter.incrementAndGet();
    assertThat(repeatOnlyOnceCounter.get() <= 1, is(true));
  }

  @Test
  @Repeat(10)
  public void repeatTenTimes() {
    repeatTenTimesCounter.incrementAndGet();
    assertThat(repeatTenTimesCounter.get() <= 10, is(true));
  }

  @Test
  @Repeat(property = "tdd.example.test.case.two.repetitions")
  public void repeatTwiceCounter() {
    repeatTwiceCounter.incrementAndGet();
    assertThat(repeatTwiceCounter.get() <= 2, is(true));
  }
}
