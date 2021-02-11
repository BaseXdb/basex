package org.basex.util;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Class for testing the {@link Compress} methods.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class InlineTest extends SandboxTest {
  /** Test. */
  @Test public void inlineInteger() {
    // positive integers
    run("0");
    run("1");
    run("12");
    run("123");
    run("99999");
    run("12345678");
    run("1234567890");
    run("2147483647");
  }

  /** Test. */
  @Test public void inlineToken() {
    // tokens with a maximum of 4 characters
    run("");
    run(" ");
    run("?");
    run("a");
    run("AB");
    run("abc");
    run("ABCD");
    run("-0");
    run("-1");

    // first 65536 Unicode characters
    for(char ch = 0; ch < 65535; ch++) run(String.valueOf(ch));
    for(char ch = 0; ch < 2047; ch++) run(String.valueOf(ch) + ch);
    run("\u067e");
    run("\u0631");
    run("\u067e\u0631");
  }

  /** Test. */
  @Test public void inlineWhitespaces() {
    // spaces
    run("     ");
    run("      ");
    run("         ");
    // mixture of whitespaces
    run("\t\n\r  \t\n\r");
    run("\t\n\r  \t\r\n \n\r\t ");
    run("\t\t\t\t\t");
    run("\t\t\t\t\t\t\t\t\t\t");
    run("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
  }

  /** Test. */
  @Test public void reject() {
    // too long string
    reject("abcde");
    // too long string (and too large digit)
    reject("2147483648");
    // mixture of whitespaces and non-whitespaces
    reject("    1");
    reject("1    ");
    // too many whitespaces
    reject("                ");
  }

  /**
   * Tests the correctness of the compressed tokens.
   * @param string test string
   */
  private static void run(final String string) {
    final byte[] token = token(string);
    final long packed = Inline.pack(token);
    if(packed == 0) fail("Value could not be inlined: " + string);

    final byte[] unpacked = Inline.unpack(packed);
    if(!eq(token, unpacked)) {
      final StringBuilder sb = new StringBuilder();
      sb.append("\nInput: ").append(string);
      sb.append("\nExpected: ").append(Arrays.toString(token));
      sb.append("\nFound: ").append(Arrays.toString(unpacked));
      sb.append("\nInlined Value:\n");
      for(int i = 32; i >= 0; i -= 8) {
        final StringBuilder bin = new StringBuilder(Long.toBinaryString((packed >> i) & 0xFF));
        while(bin.length() < 8) bin.insert(0, '0');
        sb.append(bin).append("\n");
      }
      fail(sb.toString());
    }
  }

  /**
   * Tokens to be rejected.
   * @param string test string
   */
  private static void reject(final String string) {
    final byte[] token = token(string);
    final long packed = Inline.pack(token);
    if(packed != 0) {
      final StringBuilder sb = new StringBuilder();
      sb.append("Value must not be inlined: ").append(string);
      sb.append("\nInlined Value:\n");
      for(int i = 32; i >= 0; i -= 8) {
        final StringBuilder bin = new StringBuilder(Long.toBinaryString((packed >> i) & 0xFF));
        while(bin.length() < 8) bin.insert(0, '0');
        sb.append(bin).append("\n");
      }
      fail(sb.toString());
    }
  }
}

