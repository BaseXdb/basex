package org.basex.io.in;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Test class for the {@link TextInput} method.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class TextInputTest {
  /**
   * Test small array.
   */
  @Test
  public void read1() {
    final byte[] data = { ' ' };
    run(data);
  }

  /**
   * Test intermediate array.
   */
  @Test
  public void read255() {
    final int dl = 255;
    final byte[] data = new byte[dl];
    for(int d = 0; d < dl; d++) data[d] = (byte) (' ' + (d & 0x3F));
    run(data);
  }

  /**
   * Test large array.
   */
  @Test
  public void read4095() {
    final int dl = 4095;
    final byte[] data = new byte[dl];
    for(int d = 0; d < dl; d++) data[d] = (byte) (' ' + (d & 0x3F));
    run(data);
  }

  /**
   * Test large array.
   */
  @Test
  public void read65535() {
    final int dl = 65536;
    final byte[] data = new byte[dl];
    for(int d = 0; d < dl; d++) data[d] = (byte) (' ' + (d & 0x3F));
    run(data);
  }

  /**
   * Test unknown encoding.
   * @throws IOException I/O exception
   */
  @Test(expected = IOException.class)
  public void unknownEncoding() throws IOException {
    encoding("unknown", "");
  }

  /**
   * Test alternate encodings.
   * @throws IOException I/O exception
   */
  @Test
  public void ascii() throws IOException {
    final String in = "a";
    encoding("Shift_JIS", in);
    encoding("UTF8", in);
    encoding("UTF-8", in);
    encoding("UTF-16LE", in);
    encoding("UTF-16BE", in);
    encoding("UTF-32", in);
    encoding("GBK", in);
    encoding("GBK", in);
  }

  /**
   * Test alternate encodings.
   * @throws IOException I/O exception
   */
  @Test
  public void japanese() throws IOException {
    final String in = "\u3053\u308c\u306f\u6bb4\u843d\u3067\u3059\u3002";
    encoding("Shift_JIS", in);
    encoding("UTF8", in);
    encoding("UTF-8", in);
    encoding("UTF-16LE", in);
    encoding("UTF-16BE", in);
    encoding("UTF-32", in);
    encoding("GBK", in);
    encoding("GBK", in);
  }

  /**
   * Test alternate encoding.
   * @param enc encoding to be tested
   * @param input input string
   * @throws IOException I/O exception
   */
  private static void encoding(final String enc, final String input) throws IOException {
    try(TextInput ti = new TextInput(input.getBytes(enc))) {
      assertSame(ti.encoding(enc).content(), Token.token(input));
    }
  }

  /**
   * Performs a test on the specified data.
   * @param data data to be tested
   */
  private static void run(final byte[] data) {
    try(TextInput ti = new TextInput(data)) {
      ti.read();
      ti.reset();

      final TokenBuilder tb = new TokenBuilder();
      for(int b; (b = ti.read()) != -1;) tb.add(b);
      ti.reset();
      assertTrue("Mark should not be supported for data size of " + data.length,
          data.length < IO.BLOCKSIZE);
      tb.reset();
      for(int b; (b = ti.read()) != -1;) tb.add(b);
      assertSame(data, tb.finish());
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
  private static void assertSame(final byte[] data1, final byte[] data2) {
    assertEquals("Different array size: ", data1.length, data2.length);
    assertTrue("Data arrays differ: ", Token.eq(data1, data2));
  }
}
