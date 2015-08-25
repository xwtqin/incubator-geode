package com.gemstone.gemfire.test.junit.rules;

import java.io.Serializable;

import org.junit.rules.TestWatcher;

@SuppressWarnings("serial")
public class SerializableTestWatcher extends TestWatcher implements Serializable {
}
