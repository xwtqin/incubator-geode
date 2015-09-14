package com.gemstone.gemfire.test.junit.rules;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import org.junit.rules.TemporaryFolder;

/**
 * Serializable version of TemporaryFolder JUnit Rule. JUnit lifecycle is not
 * executed in remote JVMs.
 * 
 * @author Kirk Lund
 */
@SuppressWarnings("serial")
public class SerializableTemporaryFolder extends TemporaryFolder implements SerializableTestRule {

  private void writeObject(final ObjectOutputStream out) throws Exception {
    writeParentFolder(out);
    writeFolder(out);
  }

  private void readObject(final ObjectInputStream in) throws Exception {
    readParentFolder(in);
    readFolder(in);
  }
  
  private void readParentFolder(final ObjectInputStream in) throws Exception {
    final Field parentFolderField = TemporaryFolder.class.getDeclaredField("parentFolder");
    parentFolderField.setAccessible(true);
    parentFolderField.set(this, (File) in.readObject());
  }
  
  private void readFolder(final ObjectInputStream in) throws Exception {
    final Field folderField = TemporaryFolder.class.getDeclaredField("folder");
    folderField.setAccessible(true);
    folderField.set(this, (File) in.readObject());
  }
  
  private void writeParentFolder(final ObjectOutputStream out) throws Exception {
    final Field parentFolderField = TemporaryFolder.class.getDeclaredField("parentFolder");
    parentFolderField.setAccessible(true);
    final File parentFolderFieldValue = (File) parentFolderField.get(this);
    out.writeObject(parentFolderFieldValue);
  }
  
  private void writeFolder(final ObjectOutputStream out) throws Exception {
    final Field folderField = TemporaryFolder.class.getDeclaredField("folder");
    folderField.setAccessible(true);
    final File folderFieldValue = (File) folderField.get(this);
    out.writeObject(folderFieldValue);
  }
}
