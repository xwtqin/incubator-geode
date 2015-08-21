package com.gemstone.gemfire.test.junit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Java Annotation used to annotate a test suite class test case method in order to
 * retry it in case of failure up to the specified maximum attempts.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
  
  public static int DEFAULT = 1;
  
  int value() default DEFAULT;
  
}
