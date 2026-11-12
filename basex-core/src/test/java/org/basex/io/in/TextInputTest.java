package org.basex.io.in;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Test class for the {@link TextInput} method.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TextInputTest {
  /**
   * Test small array.
   */
  @Test public void read1() {
    final byte[] data = { ' ' };
    run(data);
  }

  /**
   * Test intermediate array.
   */
  @Test public void read255() {
    final int dl = 255;
    final byte[] data = new byte[dl];
    for(int d = 0; d < dl; d++) data[d] = (byte) (' ' + (d & 0x3F));
    run(data);
  }

  /**
   * Test large array.
   */
  @Test public void read4095() {
    final int dl = 4095;
    final byte[] data = new byte[dl];
    for(int d = 0; d < dl; d++) data[d] = (byte) (' ' + (d & 0x3F));
    run(data);
  }

  /**
   * Test large array.
   */
  @Test public void read65535() {
    final int dl = 65536;
    final byte[] data = new byte[dl];
    for(int d = 0; d < dl; d++) data[d] = (byte) (' ' + (d & 0x3F));
    run(data);
  }

  /**
   * Test unknown encoding.
   */
  @Test public void unknownEncoding() {
    assertThrows(IOException.class, () -> encoding("unknown", ""));
  }

  /**
   * Test alternate encodings.
   * @throws IOException I/O exception
   */
  @Test public void ascii() throws IOException {
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
  @Test public void japanese() throws IOException {
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
   * Test supplementary characters (encoded as surrogate pairs).
   * @throws IOException I/O exception
   */
  @Test public void supplementary() throws IOException {
    // U+2000B and U+2A6B2 (CJK Extension B/C), surrounded by ASCII
    final String in = "A" + cp(0x2000B) + cp(0x2A6B2) + "B";
    encoding("GB18030", in);
    encoding("UTF-8", in);
    encoding("UTF-16LE", in);
    encoding("UTF-16BE", in);
    encoding("UTF-32", in);
  }

  /**
   * Test a stateful encoding (escape sequences) with a mid-string mode switch.
   * @throws IOException I/O exception
   */
  @Test public void stateful() throws IOException {
    // U+4E2D and U+3042 force switches between ASCII and JIS X 0208
    encoding("ISO-2022-JP", "A" + cp(0x4E2D) + "B" + cp(0x3042) + "C");
  }

  /**
   * Test malformed UTF-8 input.
   * @throws IOException I/O exception
   */
  @Test public void malformedUtf8() throws IOException {
    // second byte is not a valid continuation byte (0x80-0xBF)
    malformed(0xC2, 0xC2);
    // truncated two-byte sequence at end of input
    malformed(0xC2);
    // truncated three-byte sequence at end of input
    malformed(0xE2, 0x82);
    // overlong encodings, surrogates, codepoints beyond U+10FFFF
    malformed(0xE0, 0x81, 0x81);
    malformed(0xE0, 0x80, 0xAF);
    malformed(0xF0, 0x80, 0x81, 0x81);
    malformed(0xED, 0xA0, 0x80);
    malformed(0xF4, 0x90, 0x80, 0x80);
  }

  /**
   * Test that malformed input does not swallow the next character.
   * @throws IOException I/O exception
   */
  @Test public void resync() throws IOException {
    final String r = cp(Token.REPLACEMENT);
    decoded("UTF-8", r + 'A', bytes(0xC3, 0x41));
    decoded("UTF-8", r + 'A', bytes(0xE2, 0x82, 0x41));
    decoded("UTF-8", r.repeat(3) + 'A', bytes(0xE0, 0x80, 0xAF, 0x41));
    // byte order mark
    decoded("UTF-32", "A", bytes(0x00, 0x00, 0xFE, 0xFF, 0x00, 0x00, 0x00, 0x41));
  }

  /**
   * Checks the decoded input (with fallback).
   * @param enc encoding
   * @param expected expected string
   * @param data input bytes
   * @throws IOException I/O exception
   */
  private static void decoded(final String enc, final String expected, final byte[] data)
      throws IOException {
    try(TextInput ti = new TextInput(new IOContent(data), enc)) {
      assertEquals(expected, Token.string(ti.content()));
    }
  }

  /**
   * Returns the string representation of a codepoint.
   * @param codepoint codepoint
   * @return string
   */
  private static String cp(final int codepoint) {
    return new String(Character.toChars(codepoint));
  }

  /**
   * Converts integers to bytes.
   * @param ints integers
   * @return bytes
   */
  private static byte[] bytes(final int... ints) {
    final byte[] bytes = new byte[ints.length];
    for(int i = 0; i < ints.length; i++) bytes[i] = (byte) ints[i];
    return bytes;
  }

  /**
   * Checks that malformed UTF-8 bytes yield a replacement character (with fallback)
   * and raise an exception (without fallback).
   * @param bytes malformed input bytes
   * @throws IOException I/O exception
   */
  private static void malformed(final int... bytes) throws IOException {
    final byte[] data = bytes(bytes);

    // with fallback: replacement character is returned
    try(TextInput ti = new TextInput(new IOContent(data), "UTF-8")) {
      boolean replaced = false;
      for(int ch; (ch = ti.read()) != -1;) replaced |= ch == Token.REPLACEMENT;
      assertTrue(replaced, "Expected replacement character");
    }
    // without fallback: exception is raised
    assertThrows(IOException.class, () -> {
      try(TextInput ti = new TextInput(new IOContent(data), "UTF-8").fallback(false)) {
        while(ti.read() != -1);
      }
    });
  }

  /**
   * Test alternate encoding.
   * @param enc encoding to be tested
   * @param input input string
   * @throws IOException I/O exception
   */
  private static void encoding(final String enc, final String input) throws IOException {
    decoded(enc, input, input.getBytes(enc));
  }

  /**
   * Performs a test on the specified data.
   * @param data data to be tested
   */
  private static void run(final byte[] data) {
    try(TextInput ti = new TextInput(new IOContent(data))) {
      ti.read();
      ti.reset();

      final TokenBuilder tb = new TokenBuilder();
      for(int b; (b = ti.read()) != -1;) tb.add(b);
      ti.reset();
      assertTrue(data.length < IO.BLOCKSIZE,
        "Mark should not be supported for data size of " + data.length);
      tb.reset();
      for(int b; (b = ti.read()) != -1;) tb.add(b);
      assertSame(data, tb.finish());
    } catch(final IOException ex) {
      Util.debug(ex);
      assertTrue(data.length >= IO.BLOCKSIZE,
        "Mark could not be reset for data size of " + data.length);
    }
  }

  /**
   * Compares two byte arrays for equality.
   * @param data1 first array
   * @param data2 first array
   */
  private static void assertSame(final byte[] data1, final byte[] data2) {
    assertEquals(data1.length, data2.length, "Different array size: ");
    assertTrue(Token.eq(data1, data2), "Data arrays differ: ");
  }
}
