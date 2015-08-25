package com.gemstone.gemfire.test.junit.rules;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import org.junit.rules.TestName;

/**
 * Serializable version of TestName JUnit Rule. JUnit lifecycle is not
 * executed in remote JVMs.
 * 
 * @author Kirk Lund
 */
@SuppressWarnings("serial")
public class SerializableTestName extends TestName implements Serializable {

  private void writeObject(final ObjectOutputStream out) throws Exception {
    writeName(out);
  }

  private void readObject(final ObjectInputStream in) throws Exception {
    readName(in);
  }
  
  private void writeName(final ObjectOutputStream out) throws Exception {
    final Field nameField = TestName.class.getDeclaredField("name");
    nameField.setAccessible(true);
    final String nameValue = (String) nameField.get(this);
    out.writeObject(nameValue);
  }
  
  private void readName(final ObjectInputStream in) throws Exception {
    Field nameField = TestName.class.getDeclaredField("name");
    nameField.setAccessible(true);
    nameField.set(this, (String) in.readObject());
  }
}
