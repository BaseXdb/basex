package org.basex.test.query.func;

import static org.junit.Assert.*;

import org.basex.query.func.Function;
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
    final String fun = check(Function.EVAL);
    query(fun + "('1')", "1");
    query(fun + "('1 + 2')", "3");
    error(fun + "('1+')", Err.INCOMPLETE);
    error("declare variable $a := 1; " + fun + "('$a')", Err.VARUNDEF);
    error("for $a in (1,2) return " + fun + "('$a')", Err.VARUNDEF);
  }

  /**
   * Test method for the util:run() function.
   */
  @Test
  public void utilRun() {
    final String fun = check(Function.RUN);
    query(fun + "('etc/test/input.xq')", "XML");
    error(fun + "('etc/test/xxx.xq')", Err.UNDOC);
  }

  /**
   * Test method for the util:mb() function.
   */
  @Test
  public void utilMB() {
    final String fun = check(Function.MB);
    query(fun + "(())");
    query(fun + "(1 to 1000, false())");
    query(fun + "(1 to 1000, true())");
  }

  /**
   * Test method for the util:ms() function.
   */
  @Test
  public void utilMS() {
    final String fun = check(Function.MS);
    query(fun + "(())");
    query(fun + "(1 to 1000, false())");
    query(fun + "(1 to 1000, true())");
  }

  /**
   * Test method for the util:integer-to-base() function.
   */
  @Test
  public void utilToBase() {
    final String fun = check(Function.TO_BASE);
    query(fun + "(4, 2)", "100");
    query(fun + "(65535, 2)", "1111111111111111");
    query(fun + "(65536, 2)", "10000000000000000");
    query(fun + "(4, 16)", "4");
    query(fun + "(65535, 16)", "ffff");
    query(fun + "(65536, 16)", "10000");
    query(fun + "(4, 10)", "4");
    query(fun + "(65535, 10)", "65535");
    query(fun + "(65536, 10)", "65536");
    error(fun + "(1, 1)", Err.INVBASE);
    error(fun + "(1, 100)", Err.INVBASE);
    error(fun + "(1, 100)", Err.INVBASE);
  }

  /**
   * Test method for the util:integer-from-base() function.
   */
  @Test
  public void utilFromBase() {
    final String fun = check(Function.FRM_BASE);
    query(fun + "('100', 2)", "4");
    query(fun + "('1111111111111111', 2)", "65535");
    query(fun + "('10000000000000000', 2)", "65536");
    query(fun + "('4', 16)", "4");
    query(fun + "('ffff', 16)", "65535");
    query(fun + "('FFFF', 16)", "65535");
    query(fun + "('10000', 16)", "65536");
    query(fun + "('4', 10)", "4");
    query(fun + "('65535', 10)", "65535");
    query(fun + "('65536', 10)", "65536");
    error(fun + "('1', 1)", Err.INVBASE);
    error(fun + "('1', 100)", Err.INVBASE);
    error(fun + "('abc', 10)", Err.INVDIG);
    error(fun + "('012', 2)", Err.INVDIG);
  }

  /**
   * Test method for the util:{md5, sha1}() function.
   */
  @Test
  public void utilHashing() {
    final String md5 = check(Function.MD5);
    final String sha1 = check(Function.SHA1);
    query(md5 + "('')", "D41D8CD98F00B204E9800998ECF8427E");
    query(sha1 + "('')", "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");

    query(md5 + "('BaseX')", "0D65185C9E296311C0A2200179E479A2");
    query(sha1 + "('BaseX')", "3AD5958F0F27D5AFFDCA2957560F121D0597A4ED");

    error(md5 + "(())", Err.XPEMPTY);
    error(sha1 + "(())", Err.XPEMPTY);
  }

  /**
   * Test method for the util:crc32() function.
   */
  @Test
  public void utilCRC32() {
    final String fun = check(Function.CRC32);
    query(fun + "('')", "00000000");
    query(fun + "('BaseX')", "4C06FC7F");
  }

  /**
   * Test method for the util:to-bytes() function.
   */
  @Test
  public void utilToBytes() {
    final String fun = check(Function.TO_BYTES);
    query(fun + "(xs:base64Binary('QmFzZVggaXMgY29vbA=='))",
      "66 97 115 101 88 32 105 115 32 99 111 111 108");
    query(fun + "(xs:base64Binary(xs:hexBinary('4261736558')))",
      "66 97 115 101 88");
    query(fun + "('a')", "97");
    query("count(" + fun + "('a\u00f4c'))", "4");
    query("count(" + fun + "(123))", "3");
  }

  /**
   * Test method for the util:uuid() function.
   */
  @Test
  public void utilUuid() {
    final String fun = check(Function.UUID);
    final String s1 = query(fun + "()");
    final String s2 = query(fun + "()");
    assertTrue(!s1.equals(s2));
  }
}
