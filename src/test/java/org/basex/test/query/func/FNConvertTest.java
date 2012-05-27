package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.io.out.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery conversions functions prefixed with "convert".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNConvertTest extends AdvancedQueryTest {
  /** Null output stream. */
  static final PrintStream NULL = new PrintStream(new NullOutput());

  /**
   * Test method for the util:to-string() function.
   */
  @Test
  public void utilToString() {
    check(_CONVERT_TO_STRING);
    query(_CONVERT_TO_STRING.args("xs:base64Binary(xs:hexBinary('41'))"), "A");
    query(_CONVERT_TO_STRING.args("xs:hexBinary('41')"), "A");
    query(_CONVERT_TO_STRING.args("xs:hexBinary('41')", "CP1252"), "A");
    error(_CONVERT_TO_STRING.args("xs:hexBinary('41')", "X"), Err.BXCO_ENCODING);
  }

  /**
   * Test method for the util:to-base64Binary() function.
   */
  @Test
  public void utilToBase64Binary() {
    check(_CONVERT_TO_BASE64BINARY);
    query(_CONVERT_TO_BASE64BINARY.args("a"), "YQ==");
    query(_CONVERT_TO_BASE64BINARY.args("a", "UTF-8"), "YQ==");
    query(_CONVERT_TO_BASE64BINARY.args("a", "US-ASCII"), "YQ==");
    error(_CONVERT_TO_BASE64BINARY.args("\u00fc", "US-ASCII"), Err.BXCO_BASE64);
    error(_CONVERT_TO_BASE64BINARY.args("a", "X"), Err.BXCO_ENCODING);
  }

  /**
   * Test method for the util:to-hexBinary() function.
   */
  @Test
  public void utilToHexBinary() {
    check(_CONVERT_TO_HEXBINARY);
    query(_CONVERT_TO_HEXBINARY.args("a"), "61");
    query(_CONVERT_TO_HEXBINARY.args("a", "UTF-8"), "61");
    query(_CONVERT_TO_HEXBINARY.args("a", "US-ASCII"), "61");
    error(_CONVERT_TO_HEXBINARY.args("\u00fc", "US-ASCII"), Err.BXCO_BASE64);
    error(_CONVERT_TO_HEXBINARY.args("a", "X"), Err.BXCO_ENCODING);
  }

  /**
   * Test method for the util:to-bytes() function.
   */
  @Test
  public void utilToBytes() {
    check(_CONVERT_TO_BYTES);
    query(_CONVERT_TO_BYTES.args("xs:base64Binary('QmFzZVggaXMgY29vbA==')"),
      "66 97 115 101 88 32 105 115 32 99 111 111 108");
    query(_CONVERT_TO_BYTES.args("xs:base64Binary(xs:hexBinary('4261736558'))"),
      "66 97 115 101 88");
    query(_CONVERT_TO_BYTES.args("xs:base64Binary(<x>AAE=</x>)"), "0 1");
    query(_CONVERT_TO_BYTES.args("a"), 97);
    query(COUNT.args(_CONVERT_TO_BYTES.args("\u00e4")), 2);
    query(COUNT.args(_CONVERT_TO_BYTES.args(123)), 3);
  }

  /**
   * Test method for the util:integer-to-base() function.
   */
  @Test
  public void utilToBase() {
    check(_CONVERT_INTEGER_TO_BASE);
    query(_CONVERT_INTEGER_TO_BASE.args(4, 2), 100);
    query(_CONVERT_INTEGER_TO_BASE.args(65535, 2), "1111111111111111");
    query(_CONVERT_INTEGER_TO_BASE.args(65536, 2), "10000000000000000");
    query(_CONVERT_INTEGER_TO_BASE.args(4, 16), 4);
    query(_CONVERT_INTEGER_TO_BASE.args(65535, 16), "ffff");
    query(_CONVERT_INTEGER_TO_BASE.args(65536, 16), "10000");
    query(_CONVERT_INTEGER_TO_BASE.args(4, 10), 4);
    query(_CONVERT_INTEGER_TO_BASE.args(65535, 10), 65535);
    query(_CONVERT_INTEGER_TO_BASE.args(65536, 10), 65536);
    error(_CONVERT_INTEGER_TO_BASE.args(1, 1), Err.INVBASE);
    error(_CONVERT_INTEGER_TO_BASE.args(1, 100), Err.INVBASE);
    error(_CONVERT_INTEGER_TO_BASE.args(1, 100), Err.INVBASE);
  }

  /**
   * Test method for the util:integer-from-base() function.
   */
  @Test
  public void utilFromBase() {
    check(_CONVERT_INTEGER_FROM_BASE);
    query(_CONVERT_INTEGER_FROM_BASE.args("100", 2), "4");
    query(_CONVERT_INTEGER_FROM_BASE.args("1111111111111111", 2), 65535);
    query(_CONVERT_INTEGER_FROM_BASE.args("10000000000000000", 2), 65536);
    query(_CONVERT_INTEGER_FROM_BASE.args("4", 16), 4);
    query(_CONVERT_INTEGER_FROM_BASE.args("ffff", 16), 65535);
    query(_CONVERT_INTEGER_FROM_BASE.args("FFFF", 16), 65535);
    query(_CONVERT_INTEGER_FROM_BASE.args("10000", 16), 65536);
    query(_CONVERT_INTEGER_FROM_BASE.args("4", 10), 4);
    query(_CONVERT_INTEGER_FROM_BASE.args("65535", 10), 65535);
    query(_CONVERT_INTEGER_FROM_BASE.args("65536", 10), 65536);
    error(_CONVERT_INTEGER_FROM_BASE.args("1", 1), Err.INVBASE);
    error(_CONVERT_INTEGER_FROM_BASE.args("1", 100), Err.INVBASE);
    error(_CONVERT_INTEGER_FROM_BASE.args("abc", 10), Err.INVDIG);
    error(_CONVERT_INTEGER_FROM_BASE.args("012", 2), Err.INVDIG);
  }
}
