package com.gemstone.gemfire.test.junit.rules;

import org.junit.rules.TestWatcher;

/**
 * Serializable version of TestWatcher JUnit Rule. JUnit lifecycle is not
 * executed in remote JVMs.
 * 
 * @author Kirk Lund
 */
@SuppressWarnings("serial")
public class SerializableTestWatcher extends TestWatcher implements SerializableTestRule {
}
