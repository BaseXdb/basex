package org.basex.data;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the {@code UPDINDEX} option.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class UpdIndexTest extends AdvancedQueryTest {
  /**
   * Initializes the tests.
   * @throws Exception exception
   */
  @Before
  public void init() throws Exception {
    run(new Set(MainOptions.UPDINDEX, true));
    run(new CreateDB(NAME));
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void test() throws BaseXException {
    for(int i = 0; i < 25; i++) {
      run(new Replace("x.xml", "<x><a>A</a><a>B</a></x>"));
    }
    query(Function._DB_TEXT.args(NAME, "A"), "A");
    query(Function._DB_TEXT.args(NAME, "B"), "B");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void test2() throws BaseXException {
    for(int i = 0; i < 25; i++) {
      run(new Replace("x.xml", "<x><a>A</a><a>B</a></x>"));
      run(new Replace("x.xml", "<x><a>A</a><a>C</a></x>"));
    }
    query(Function._DB_TEXT.args(NAME, "A"), "A");
    query(Function._DB_TEXT.args(NAME, "C"), "C");
    query(Function._DB_TEXT.args(NAME, "B"), "");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void test3() throws BaseXException {
    for(int i = 0; i < 25; i++) {
      run(new Replace("x.xml", "<x><a>A</a><a>BC</a><a>DEF</a></x>"));
    }
    query(Function._DB_TEXT.args(NAME, "A"), "A");
    query(Function._DB_TEXT.args(NAME, "BC"), "BC");
    query(Function._DB_TEXT.args(NAME, "DEF"), "DEF");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void test4() throws BaseXException {
    for(int i = 0; i < 25; i++) {
      run(new Open(NAME));
      run(new Replace("x.xml", "<x><a>A</a><a>BC</a></x>"));
      run(new Close());
    }
    query(Function._DB_TEXT.args(NAME, "A"), "A");
    query(Function._DB_TEXT.args(NAME, "BC"), "BC");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void test5() throws BaseXException {
    for(int i = 0; i < 25; i++) {
      run(new Add("a", "<x c='c'/>"));
      run(new Add("a", "<x a='a' b='b'/>"));
      run(new Replace("a", "<x/>"));
    }
    query(Function._DB_ATTRIBUTE.args(NAME, "a"), "");
    query(Function._DB_ATTRIBUTE.args(NAME, "b"), "");
    query(Function._DB_ATTRIBUTE.args(NAME, "c"), "");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void test6() throws BaseXException {
    run(new CreateDB(NAME, "<X><A>q</A><B>q</B></X>"));
    query("replace node /X/A with 'x', replace node /X/B with 'y'", "");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void test7() throws BaseXException {
    run(new Replace("A", "<a/>"));
    run(new Replace("B", "<a a='1'/>"));
    run(new Replace("C", "<a a='1'/>"));
    run(new Replace("A", "<a a='1'/>"));
    run(new Close());
    run(new Open(NAME));
    run(new Delete("A"));
    run(new Close());
  }

  /**
   * Runs the specified command.
   * @param cmd command to be run
   * @return string result
   * @throws BaseXException database exception
   */
  private static String run(final Command cmd) throws BaseXException {
    return cmd.execute(context);
  }
}
