package org.basex.query.func.archive;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link TempFiles}.
 *
 * @author BaseX Team, BSD License
 * @author Vincent Lizzi
 */
public final class TempFilesTest {
  /**
   * Registered file is deleted when the query context closes.
   * @throws IOException I/O exception
   */
  @Test public void closeDeletesRegisteredFile() throws IOException {
    final IOFile tmp = new IOFile(File.createTempFile("basex-test-", ".tmp"));
    assertTrue(tmp.exists());

    final TempFiles tf = new TempFiles();
    tf.add(tmp);
    tf.close();

    assertFalse(tmp.exists());
  }

  /**
   * All registered files are deleted when the query context closes.
   * @throws IOException I/O exception
   */
  @Test public void closeDeletesAllRegisteredFiles() throws IOException {
    final IOFile tmp1 = new IOFile(File.createTempFile("basex-test-", ".tmp"));
    final IOFile tmp2 = new IOFile(File.createTempFile("basex-test-", ".tmp"));
    assertTrue(tmp1.exists());
    assertTrue(tmp2.exists());

    final TempFiles tf = new TempFiles();
    tf.add(tmp1);
    tf.add(tmp2);
    tf.close();

    assertFalse(tmp1.exists());
    assertFalse(tmp2.exists());
  }

  /**
   * Calling close twice does not throw, even though the files are already deleted.
   * @throws IOException I/O exception
   */
  @Test public void closeIsIdempotent() throws IOException {
    final IOFile tmp = new IOFile(File.createTempFile("basex-test-", ".tmp"));

    final TempFiles tf = new TempFiles();
    tf.add(tmp);
    tf.close();
    assertDoesNotThrow(tf::close);
  }
}
