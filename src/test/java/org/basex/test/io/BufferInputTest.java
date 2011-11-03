package org.basex.test.io;

import static org.junit.Assert.*;

import java.io.IOException;

import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.io.in.BufferInput;
import org.basex.io.out.ArrayOutput;
import org.basex.test.build.AddDeleteTest;
import org.basex.util.Token;
import org.basex.util.Util;
import org.junit.Test;

/**
 * Test class for the BufferInput method.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BufferInputTest {
  /** Test database name. */
  private static final String DB = Util.name(AddDeleteTest.class);

  /**
   * Test small array.
   * @throws IOException I/O exception
   */
  @Test
  public void read1() throws IOException {
    final byte[] data = new byte[1];
    for(int d = 0; d < data.length; d++) data[d] = (byte) d;
    run(data);
  }

  /**
   * Test large array.
   * @throws IOException I/O exception
   */
  @Test
  public void read4095() throws IOException {
    final byte[] data = new byte[4095];
    for(int d = 0; d < data.length; d++) data[d] = (byte) d;
    run(data);
  }

  /**
   * Test large array.
   * @throws IOException I/O exception
   */
  @Test
  public void read65535() throws IOException {
    final byte[] data = new byte[65535];
    for(int d = 0; d < data.length; d++) data[d] = (byte) d;
    run(data);
  }

  /**
   * Performs a test on the specified data.
   * @param data data to be tested
   * @throws IOException I/O exception
   */
  private void run(final byte[] data) throws IOException {
    for(int d = 0; d < data.length; d++) data[d] = (byte) d;
    final IOFile io = new IOFile(Prop.TMP, DB);
    io.write(data);

    final ArrayOutput ao = new ArrayOutput();
    final BufferInput bi = new BufferInput(io.file());
    bi.reset();
    // guess encoding
    bi.encoding();
    bi.readChar();
    bi.reset();

    for(int b; (b = bi.read()) != -1;) ao.write(b);
    try {
      bi.reset();
      assertTrue("Mark should not be supported for data size of " + data.length,
          data.length < IO.BLOCKSIZE);
      ao.reset();
      for(int b; (b = bi.read()) != -1;) ao.write(b);
      assertEquals(data, ao.toArray());
    } catch(final IOException ex) {
      assertTrue("Mark could not be reset for data size of " + data.length,
          data.length >= IO.BLOCKSIZE);
    }
  }

  /**
   * Compares two byte arrays for equality.
   * @param data1 first array
   * @param data2 first array
   */
  static void assertEquals(final byte[] data1, final byte[] data2) {
    assertTrue("Original and read data differs.", Token.eq(data1, data2));
  }
}
