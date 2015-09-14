package com.gemstone.gemfire.test.junit.rules;

import java.io.Serializable;

import org.junit.rules.TestRule;

/**
 * Serializable version of JUnit TestRule. JUnit lifecycle is not
 * executed in remote JVMs.
 * 
 * The simplest way to satisfy this interface is to apply <tt>transient</tt>
 * to every instance field.
 * 
 * @author Kirk Lund
 */
public interface SerializableTestRule extends Serializable, TestRule {
}
