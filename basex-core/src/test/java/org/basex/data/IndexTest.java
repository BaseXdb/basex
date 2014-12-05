package org.basex.data;

import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the {@link MainOptions#UPDINDEX} and {@link MainOptions#AUTOOPTIMIZE} options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class IndexTest extends AdvancedQueryTest {
  /**
   * Finalize test.
   * @throws BaseXException database exception
   */
  @After
  public void after() throws BaseXException {
    run(new Set(MainOptions.UPDINDEX, false));
    run(new Set(MainOptions.AUTOOPTIMIZE, false));
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex() throws BaseXException {
    run(new Set(MainOptions.UPDINDEX, true));
    run(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      run(new Replace("x.xml", "<x><a>A</a><a>B</a></x>"));
    }
    query(Function._DB_TEXT.args(NAME, "A"), "A");
    query(Function._DB_TEXT.args(NAME, "B"), "B");
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex2() throws BaseXException {
    run(new Set(MainOptions.UPDINDEX, true));
    run(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      run(new Replace("x.xml", "<x><a>A</a><a>B</a></x>"));
      run(new Replace("x.xml", "<x><a>A</a><a>C</a></x>"));
    }
    query(Function._DB_TEXT.args(NAME, "A"), "A");
    query(Function._DB_TEXT.args(NAME, "C"), "C");
    query(Function._DB_TEXT.args(NAME, "B"), "");
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex3() throws BaseXException {
    run(new Set(MainOptions.UPDINDEX, true));
    run(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      run(new Replace("x.xml", "<x><a>A</a><a>BC</a><a>DEF</a></x>"));
    }
    query(Function._DB_TEXT.args(NAME, "A"), "A");
    query(Function._DB_TEXT.args(NAME, "BC"), "BC");
    query(Function._DB_TEXT.args(NAME, "DEF"), "DEF");
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex4() throws BaseXException {
    run(new Set(MainOptions.UPDINDEX, true));
    run(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      run(new Open(NAME));
      run(new Replace("x.xml", "<x><a>A</a><a>BC</a></x>"));
      run(new Close());
    }
    query(Function._DB_TEXT.args(NAME, "A"), "A");
    query(Function._DB_TEXT.args(NAME, "BC"), "BC");
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex5() throws BaseXException {
    run(new Set(MainOptions.UPDINDEX, true));
    run(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      run(new Add("a", "<x c='c'/>"));
      run(new Add("a", "<x a='a' b='b'/>"));
      run(new Replace("a", "<x/>"));
    }
    query(Function._DB_ATTRIBUTE.args(NAME, "a"), "");
    query(Function._DB_ATTRIBUTE.args(NAME, "b"), "");
    query(Function._DB_ATTRIBUTE.args(NAME, "c"), "");
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex6() throws BaseXException {
    run(new Set(MainOptions.UPDINDEX, true));
    run(new CreateDB(NAME, "<X><A>q</A><B>q</B></X>"));
    query("replace node /X/A with 'x', replace node /X/B with 'y'", "");
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex7() throws BaseXException {
    run(new Set(MainOptions.UPDINDEX, true));
    run(new CreateDB(NAME));
    run(new Replace("A", "<a/>"));
    run(new Replace("B", "<a a='1'/>"));
    run(new Replace("C", "<a a='1'/>"));
    run(new Replace("A", "<a a='1'/>"));
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");
    run(new Close());
    run(new Open(NAME));
    run(new Delete("A"));
    run(new Close());
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void autooptimize() throws BaseXException {
    run(new Set(MainOptions.AUTOOPTIMIZE, true));
    run(new CreateDB(NAME));
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");
    run(new Replace("x.xml", "<a>A</a>"));
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");
    query(_DB_REPLACE.args(NAME, "x.xml", "<a>B</a>"));
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");

    run(new Set(MainOptions.AUTOOPTIMIZE, false));
    run(new Optimize());
    run(new Replace("x.xml", "<a>C</a>"));
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "false");

    run(new Optimize());
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "true");
    query(_DB_REPLACE.args(NAME, "x.xml", "<a>D</a>"));
    query(Function._DB_INFO.args(NAME) + "//textindex/text()", "false");
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
