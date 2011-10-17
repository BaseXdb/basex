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
    check(EVAL);
    query(EVAL.args("1"), 1);
    query(EVAL.args("1 + 2"), 3);
    error(EVAL.args("1+"), Err.INCOMPLETE);
    error("declare variable $a := 1; " + EVAL.args("\"$a\""), Err.VARUNDEF);
    error("for $a in (1,2) return " + EVAL.args("\"$a\""), Err.VARUNDEF);
  }

  /**
   * Test method for the util:run() function.
   */
  @Test
  public void utilRun() {
    check(RUN);
    query(RUN.args("etc/test/input.xq"), "XML");
    error(RUN.args("etc/test/xxx.xq"), Err.UNDOC);
  }

  /**
   * Test method for the util:mb() function.
   */
  @Test
  public void utilMB() {
    check(MB);
    query(MB.args("()"));
    query(MB.args(" 1 to 1000", false));
    query(MB.args(" 1 to 1000", true));
  }

  /**
   * Test method for the util:ms() function.
   */
  @Test
  public void utilMS() {
    check(MS);
    query(MS.args("()"));
    query(MS.args(" 1 to 1000", false));
    query(MS.args(" 1 to 1000", true));
  }

  /**
   * Test method for the util:integer-to-base() function.
   */
  @Test
  public void utilToBase() {
    check(TO_BASE);
    query(TO_BASE.args(4, 2), 100);
    query(TO_BASE.args(65535, 2), "1111111111111111");
    query(TO_BASE.args(65536, 2), "10000000000000000");
    query(TO_BASE.args(4, 16), 4);
    query(TO_BASE.args(65535, 16), "ffff");
    query(TO_BASE.args(65536, 16), "10000");
    query(TO_BASE.args(4, 10), 4);
    query(TO_BASE.args(65535, 10), 65535);
    query(TO_BASE.args(65536, 10), 65536);
    error(TO_BASE.args(1, 1), Err.INVBASE);
    error(TO_BASE.args(1, 100), Err.INVBASE);
    error(TO_BASE.args(1, 100), Err.INVBASE);
  }

  /**
   * Test method for the util:integer-from-base() function.
   */
  @Test
  public void utilFromBase() {
    check(FRM_BASE);
    query(FRM_BASE.args("100", 2), "4");
    query(FRM_BASE.args("1111111111111111", 2), 65535);
    query(FRM_BASE.args("10000000000000000", 2), 65536);
    query(FRM_BASE.args("4", 16), 4);
    query(FRM_BASE.args("ffff", 16), 65535);
    query(FRM_BASE.args("FFFF", 16), 65535);
    query(FRM_BASE.args("10000", 16), 65536);
    query(FRM_BASE.args("4", 10), 4);
    query(FRM_BASE.args("65535", 10), 65535);
    query(FRM_BASE.args("65536", 10), 65536);
    error(FRM_BASE.args("1", 1), Err.INVBASE);
    error(FRM_BASE.args("1", 100), Err.INVBASE);
    error(FRM_BASE.args("abc", 10), Err.INVDIG);
    error(FRM_BASE.args("012", 2), Err.INVDIG);
  }

  /**
   * Test method for the util:{md5, sha1}() function.
   */
  @Test
  public void utilHashing() {
    check(MD5);
    check(SHA1);
    query(MD5.args(""), "D41D8CD98F00B204E9800998ECF8427E");
    query(SHA1.args(""), "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");

    query(MD5.args("BaseX"), "0D65185C9E296311C0A2200179E479A2");
    query(SHA1.args("BaseX"), "3AD5958F0F27D5AFFDCA2957560F121D0597A4ED");

    error(MD5.args("()"), Err.XPEMPTY);
    error(SHA1.args("()"), Err.XPEMPTY);
  }

  /**
   * Test method for the util:crc32() function.
   */
  @Test
  public void utilCRC32() {
    check(CRC32);
    query(CRC32.args(""), "00000000");
    query(CRC32.args("BaseX"), "4C06FC7F");
  }

  /**
   * Test method for the util:to-bytes() function.
   */
  @Test
  public void utilToBytes() {
    check(TO_BYTES);
    query(TO_BYTES.args("xs:base64Binary('QmFzZVggaXMgY29vbA==')"),
      "66 97 115 101 88 32 105 115 32 99 111 111 108");
    query(TO_BYTES.args("xs:base64Binary(xs:hexBinary('4261736558'))"),
      "66 97 115 101 88");
    query(TO_BYTES.args("a"), 97);
    query(COUNT.args(TO_BYTES.args("a\u00f4c")), 4);
    query(COUNT.args(TO_BYTES.args(123)), 3);
  }

  /**
   * Test method for the util:to-string() function.
   */
  @Test
  public void utilToString() {
    check(TO_STRING);
    query(TO_STRING.args("xs:base64Binary(xs:hexBinary('41'))"), "A");
    query(TO_STRING.args("xs:hexBinary('41')"), "A");
    query(TO_STRING.args("xs:hexBinary('41')", "CP1252"), "A");
    error(TO_STRING.args("xs:hexBinary('41')", "X"), Err.CONVERT);
  }

  /**
   * Test method for the util:uuid() function.
   */
  @Test
  public void utilUuid() {
    check(UUID);
    final String s1 = query(UUID.args());
    final String s2 = query(UUID.args());
    assertTrue(!s1.equals(s2));
  }
}
