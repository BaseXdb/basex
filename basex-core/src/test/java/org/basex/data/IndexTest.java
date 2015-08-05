package org.basex.data;

import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

/**
 * This class tests the {@link MainOptions#UPDINDEX} and {@link MainOptions#AUTOOPTIMIZE} options.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
@RunWith(Parameterized.class)
public final class IndexTest extends AdvancedQueryTest {
  /** Main memory flag. */
  @Parameter
  public Object mainmem;

  /**
   * Mainmem parameters.
   * @return parameters
   */
  @Parameters
  public static Collection<Object[]> params() {
    final List<Object[]> params = new ArrayList<>();
    params.add(new Object[] { false });
    // [CG] MAINMEM: fix bugs
    //params.add(new Object[] { true });
    return params;
  }

  /** Test file. */
  public static final String FILE = "src/test/resources/selective.xml";

  /** Initializes a test. */
  @Before
  public void before() {
    context.options.set(MainOptions.MAINMEM, (Boolean) mainmem);
  }

  /**
   * Finalize test.
   * @throws BaseXException database exception
   */
  @After
  public void after() throws BaseXException {
    run(new Close());
    context.options.set(MainOptions.UPDINDEX, false);
    context.options.set(MainOptions.AUTOOPTIMIZE, false);
    context.options.set(MainOptions.MAINMEM, false);
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex() throws BaseXException {
    context.options.set(MainOptions.UPDINDEX, true);
    run(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      run(new Replace("x.xml", "<x><a>A</a><a>B</a></x>"));
    }
    query(_DB_TEXT.args(NAME, "A"), "A");
    query(_DB_TEXT.args(NAME, "B"), "B");
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex2() throws BaseXException {
    context.options.set(MainOptions.UPDINDEX, true);
    run(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      run(new Replace("x.xml", "<x><a>A</a><a>B</a></x>"));
      run(new Replace("x.xml", "<x><a>A</a><a>C</a></x>"));
    }
    query(_DB_TEXT.args(NAME, "A"), "A");
    query(_DB_TEXT.args(NAME, "C"), "C");
    query(_DB_TEXT.args(NAME, "B"), "");
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex3() throws BaseXException {
    context.options.set(MainOptions.UPDINDEX, true);
    run(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      run(new Replace("x.xml", "<x><a>A</a><a>BC</a><a>DEF</a></x>"));
    }
    query(_DB_TEXT.args(NAME, "A"), "A");
    query(_DB_TEXT.args(NAME, "BC"), "BC");
    query(_DB_TEXT.args(NAME, "DEF"), "DEF");
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex4() throws BaseXException {
    context.options.set(MainOptions.UPDINDEX, true);
    run(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      run(new Open(NAME));
      run(new Replace("x.xml", "<x><a>A</a><a>BC</a></x>"));
      run(new Close());
    }
    query(_DB_TEXT.args(NAME, "A"), "A");
    query(_DB_TEXT.args(NAME, "BC"), "BC");
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex5() throws BaseXException {
    context.options.set(MainOptions.UPDINDEX, true);
    run(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      run(new Add("a", "<x c='c'/>"));
      run(new Add("a", "<x a='a' b='b'/>"));
      run(new Replace("a", "<x/>"));
    }
    query(_DB_ATTRIBUTE.args(NAME, "a"), "");
    query(_DB_ATTRIBUTE.args(NAME, "b"), "");
    query(_DB_ATTRIBUTE.args(NAME, "c"), "");
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex6() throws BaseXException {
    context.options.set(MainOptions.UPDINDEX, true);
    run(new CreateDB(NAME, "<X><A>q</A><B>q</B></X>"));
    query("replace node /X/A with 'x', replace node /X/B with 'y'", "");
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex7() throws BaseXException {
    context.options.set(MainOptions.UPDINDEX, true);
    run(new CreateDB(NAME));
    run(new Replace("A", "<a/>"));
    run(new Replace("B", "<a a='1'/>"));
    run(new Replace("C", "<a a='1'/>"));
    run(new Replace("A", "<a a='1'/>"));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
    run(new Close());
    run(new Open(NAME));
    run(new Delete("A"));
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void updindex8() throws BaseXException {
    context.options.set(MainOptions.UPDINDEX, true);
    run(new CreateDB(NAME));
    run(new Replace("A", "<X a='?' b='a' c='1'/>"));
    run(new Replace("A", "<X a='?' b='b' c='2'/>"));
    run(new Replace("A", "<X/>"));
  }

  /**
   * Test.
   * @throws BaseXException database exception
   */
  @Test
  public void autooptimize() throws BaseXException {
    context.options.set(MainOptions.AUTOOPTIMIZE, true);
    run(new CreateDB(NAME));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
    run(new Replace("x.xml", "<a>A</a>"));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
    query(_DB_REPLACE.args(NAME, "x.xml", "<a>B</a>"));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");

    context.options.set(MainOptions.AUTOOPTIMIZE, false);
    run(new Optimize());
    run(new Replace("x.xml", "<a>C</a>"));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "false");

    run(new Optimize());
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
    query(_DB_REPLACE.args(NAME, "x.xml", "<a>D</a>"));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "false");
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
