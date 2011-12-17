package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;

/**
 * This class tests the XQuery utility functions prefixed with "util".
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNUtilTest extends AdvancedQueryTest {
  /**
   * Test method for the util:eval() function.
   */
  @Test
  public void utilEval() {
    check(_UTIL_EVAL);
    query(_UTIL_EVAL.args("1"), 1);
    query(_UTIL_EVAL.args("1 + 2"), 3);
    error(_UTIL_EVAL.args("1+"), Err.INCOMPLETE);
    error("declare variable $a:=1;" + _UTIL_EVAL.args("\"$a\""), Err.VARUNDEF);
    error("for $a in (1,2) return " + _UTIL_EVAL.args("\"$a\""), Err.VARUNDEF);
  }

  /**
   * Test method for the util:run() function.
   */
  @Test
  public void utilRun() {
    check(_UTIL_RUN);
    query(_UTIL_RUN.args("src/test/resources/input.xq"), "XML");
    error(_UTIL_RUN.args("src/test/resources/xxx.xq"), Err.UNDOC);
  }

  /**
   * Test method for the util:mb() function.
   */
  @Test
  public void utilMB() {
    check(_UTIL_MB);
    query(_UTIL_MB.args("()"));
    query(_UTIL_MB.args(" 1 to 1000", false));
    query(_UTIL_MB.args(" 1 to 1000", true));
  }

  /**
   * Test method for the util:ms() function.
   */
  @Test
  public void utilMS() {
    check(_UTIL_MS);
    query(_UTIL_MS.args("()"));
    query(_UTIL_MS.args(" 1 to 1000", false));
    query(_UTIL_MS.args(" 1 to 1000", true));
  }

  /**
   * Test method for the util:integer-to-base() function.
   */
  @Test
  public void utilToBase() {
    check(_UTIL_INTEGER_TO_BASE);
    query(_UTIL_INTEGER_TO_BASE.args(4, 2), 100);
    query(_UTIL_INTEGER_TO_BASE.args(65535, 2), "1111111111111111");
    query(_UTIL_INTEGER_TO_BASE.args(65536, 2), "10000000000000000");
    query(_UTIL_INTEGER_TO_BASE.args(4, 16), 4);
    query(_UTIL_INTEGER_TO_BASE.args(65535, 16), "ffff");
    query(_UTIL_INTEGER_TO_BASE.args(65536, 16), "10000");
    query(_UTIL_INTEGER_TO_BASE.args(4, 10), 4);
    query(_UTIL_INTEGER_TO_BASE.args(65535, 10), 65535);
    query(_UTIL_INTEGER_TO_BASE.args(65536, 10), 65536);
    error(_UTIL_INTEGER_TO_BASE.args(1, 1), Err.INVBASE);
    error(_UTIL_INTEGER_TO_BASE.args(1, 100), Err.INVBASE);
    error(_UTIL_INTEGER_TO_BASE.args(1, 100), Err.INVBASE);
  }

  /**
   * Test method for the util:integer-from-base() function.
   */
  @Test
  public void utilFromBase() {
    check(_UTIL_INTEGER_FROM_BASE);
    query(_UTIL_INTEGER_FROM_BASE.args("100", 2), "4");
    query(_UTIL_INTEGER_FROM_BASE.args("1111111111111111", 2), 65535);
    query(_UTIL_INTEGER_FROM_BASE.args("10000000000000000", 2), 65536);
    query(_UTIL_INTEGER_FROM_BASE.args("4", 16), 4);
    query(_UTIL_INTEGER_FROM_BASE.args("ffff", 16), 65535);
    query(_UTIL_INTEGER_FROM_BASE.args("FFFF", 16), 65535);
    query(_UTIL_INTEGER_FROM_BASE.args("10000", 16), 65536);
    query(_UTIL_INTEGER_FROM_BASE.args("4", 10), 4);
    query(_UTIL_INTEGER_FROM_BASE.args("65535", 10), 65535);
    query(_UTIL_INTEGER_FROM_BASE.args("65536", 10), 65536);
    error(_UTIL_INTEGER_FROM_BASE.args("1", 1), Err.INVBASE);
    error(_UTIL_INTEGER_FROM_BASE.args("1", 100), Err.INVBASE);
    error(_UTIL_INTEGER_FROM_BASE.args("abc", 10), Err.INVDIG);
    error(_UTIL_INTEGER_FROM_BASE.args("012", 2), Err.INVDIG);
  }

  /**
   * Test method for the util:{md5, sha1}() function.
   */
  @Test
  public void utilHashing() {
    check(_UTIL_MD5);
    check(_UTIL_SHA1);
    query(_UTIL_MD5.args(""), "D41D8CD98F00B204E9800998ECF8427E");
    query(_UTIL_SHA1.args(""), "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");

    query(_UTIL_MD5.args("BaseX"), "0D65185C9E296311C0A2200179E479A2");
    query(_UTIL_SHA1.args("BaseX"), "3AD5958F0F27D5AFFDCA2957560F121D0597A4ED");

    error(_UTIL_MD5.args("()"), Err.XPEMPTY);
    error(_UTIL_SHA1.args("()"), Err.XPEMPTY);
  }

  /**
   * Test method for the util:crc32() function.
   */
  @Test
  public void utilCRC32() {
    check(_UTIL_CRC32);
    query(_UTIL_CRC32.args(""), "00000000");
    query(_UTIL_CRC32.args("BaseX"), "4C06FC7F");
  }

  /**
   * Test method for the util:to-bytes() function.
   */
  @Test
  public void utilToBytes() {
    check(_UTIL_TO_BYTES);
    query(_UTIL_TO_BYTES.args("xs:base64Binary('QmFzZVggaXMgY29vbA==')"),
      "66 97 115 101 88 32 105 115 32 99 111 111 108");
    query(_UTIL_TO_BYTES.args("xs:base64Binary(xs:hexBinary('4261736558'))"),
      "66 97 115 101 88");
    query(_UTIL_TO_BYTES.args("xs:base64Binary(<x>AAE=</x>)"), "0 1");
    query(_UTIL_TO_BYTES.args("a"), 97);
    query(COUNT.args(_UTIL_TO_BYTES.args("a\u00f4c")), 4);
    query(COUNT.args(_UTIL_TO_BYTES.args(123)), 3);
  }

  /**
   * Test method for the util:to-string() function.
   */
  @Test
  public void utilToString() {
    check(_UTIL_TO_STRING);
    query(_UTIL_TO_STRING.args("xs:base64Binary(xs:hexBinary('41'))"), "A");
    query(_UTIL_TO_STRING.args("xs:hexBinary('41')"), "A");
    query(_UTIL_TO_STRING.args("xs:hexBinary('41')", "CP1252"), "A");
    error(_UTIL_TO_STRING.args("xs:hexBinary('41')", "X"), Err.CONVERT);
  }

  /**
   * Test method for the util:uuid() function.
   */
  @Test
  public void utilUuid() {
    check(_UTIL_UUID);
    final String s1 = query(_UTIL_UUID.args());
    final String s2 = query(_UTIL_UUID.args());
    assertTrue(!s1.equals(s2));
  }
}
