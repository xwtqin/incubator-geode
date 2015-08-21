package com.gemstone.gemfire.test.junitparams;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gemstone.gemfire.test.junit.categories.UnitTest;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@Category(UnitTest.class)
@RunWith(JUnitParamsRunner.class)
public class JUnitParamsExampleJUnitTest {
  @Test
  @Parameters({"17, false", 
               "22, true" })
  public void personIsAdult(int age, boolean valid) throws Exception {
    assertThat(true, is(true));
    assertThat(new Person(age).isAdult(), is(valid));
  }
  
  protected static class Person {
    private static final int MIN_AGE_OF_ADULT = 18;
    private final int age;
    public Person(final int age) {
      this.age = age;
    }
    public Boolean isAdult() {
      return this.age >= MIN_AGE_OF_ADULT;
    }
  }
}
