package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.io.out.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery utility functions prefixed with "util".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNUtilTest extends AdvancedQueryTest {
  /** Null output stream. */
  static final PrintStream NULL = new PrintStream(new NullOutput());

  /**
   * Test method for the util:nl() function.
   */
  @Test
  public void utilNl() {
    // compared with empty string, because query() function removes all newlines
    query(_UTIL_NL.args(), "");
  }

  /**
   * Test method for the util:tab() function.
   */
  @Test
  public void utilTab() {
    query(_UTIL_TAB.args(), "\t");
  }

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
    error(_UTIL_RUN.args("src/test/resources/xxx.xq"), Err.FILE_IO);
  }

  /**
   * Test method for the util:type() function.
   */
  @Test
  public void utilType() {
    final PrintStream err = System.err;
    System.setErr(NULL);
    check(_UTIL_TYPE);
    query(_UTIL_TYPE.args("()"), "");
    query(_UTIL_TYPE.args("1"), "1");
    query(_UTIL_TYPE.args("(1, 2, 3)"), "1 2 3");
    query(_UTIL_TYPE.args("<x a='1' b='2' c='3'/>/@*/data()"), "1 2 3");
    System.setErr(err);
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
