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
 * @author BaseX Team 2005-16, BSD License
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
    params.add(new Object[] { true });
    return params;
  }

  /** Test file. */
  public static final String FILE = "src/test/resources/selective.xml";

  /**
   * Initializes a test.
   */
  @Before
  public void before() {
    set(MainOptions.MAINMEM, mainmem);
  }

  /**
   * Finalize test.
   */
  @After
  public void after() {
    execute(new DropDB(NAME));
    set(MainOptions.TOKENINDEX, false);
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.AUTOOPTIMIZE, false);
    set(MainOptions.MAINMEM, false);
  }

  /**
   * Test.
   */
  @Test
  public void updindexText() {
    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      execute(new Replace("x.xml", "<x><a>A</a><a>B</a></x>"));
    }
    query(_DB_TEXT.args(NAME, "A"), "A");
    query(_DB_TEXT.args(NAME, "B"), "B");
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
  }

  /**
   * Test.
   */
  @Test
  public void updindexText2() {
    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      execute(new Replace("x.xml", "<x><a>A</a><a>B</a></x>"));
      execute(new Replace("x.xml", "<x><a>A</a><a>C</a></x>"));
    }
    query(_DB_TEXT.args(NAME, "A"), "A");
    query(_DB_TEXT.args(NAME, "C"), "C");
    query(_DB_TEXT.args(NAME, "B"), "");
  }

  /**
   * Test.
   */
  @Test
  public void updindexText3() {
    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      execute(new Replace("x.xml", "<x><a>A</a><a>BC</a><a>DEF</a></x>"));
    }
    query(_DB_TEXT.args(NAME, "A"), "A");
    query(_DB_TEXT.args(NAME, "BC"), "BC");
    query(_DB_TEXT.args(NAME, "DEF"), "DEF");
  }

  /**
   * Test.
   */
  @Test
  public void updindexAttribute() {
    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      execute(new Add("a", "<x c='c'/>"));
      execute(new Add("a", "<x a='a' b='b'/>"));
      execute(new Replace("a", "<x/>"));
    }
    query(_DB_ATTRIBUTE.args(NAME, "a"), "");
    query(_DB_ATTRIBUTE.args(NAME, "b"), "");
    query(_DB_ATTRIBUTE.args(NAME, "c"), "");
  }

  /**
   * Test.
   */
  @Test
  public void updindexToken() {
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, true);
    execute(new CreateDB(NAME));

    execute(new Add("a", "<x c='c'/>"));
    query(_DB_TOKEN.args(NAME, "a"), "");
    query(DATA.args(_DB_TOKEN.args(NAME, "c")), "c");

    for(int i = 0; i < 5; i++) {
      execute(new Add("a", "<x c='c'/>"));
      execute(new Add("a", "<x a='a' b='b'/>"));
      execute(new Replace("a", "<x/>"));
    }
    query(_DB_TOKEN.args(NAME, "a"), "");
    query(_DB_TOKEN.args(NAME, "b"), "");
    query(_DB_TOKEN.args(NAME, "c"), "");
    query(_DB_INFO.args(NAME) + "//tokenindex/text()", "true");
  }

  /**
   * Test.
   */
  @Test
  public void updindexReplace1() {
    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME, "<X><A>q</A><B>q</B></X>"));
    query("replace node /X/A with 'x', replace node /X/B with 'y'", "");
  }

  /**
   * Test.
   */
  @Test
  public void updindexReplace2() {
    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME));
    execute(new Replace("A", "<X a='?' b='a' c='1'/>"));
    execute(new Replace("A", "<X a='?' b='b' c='2'/>"));
    execute(new Replace("A", "<X/>"));
  }

  /**
   * Test.
   */
  @Test
  public void updindexOpenClose1() {
    final boolean openClose = !(Boolean) mainmem;
    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME));
    for(int i = 0; i < 5; i++) {
      if(openClose) execute(new Open(NAME));
      execute(new Replace("x.xml", "<x><a>A</a><a>BC</a></x>"));
      if(openClose) execute(new Close());
    }
    query(_DB_TEXT.args(NAME, "A"), "A");
    query(_DB_TEXT.args(NAME, "BC"), "BC");
  }

  /**
   * Test.
   */
  @Test
  public void updindexOpenClose2() {
    final boolean openClose = !(Boolean) mainmem;
    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME));
    execute(new Replace("A", "<a/>"));
    execute(new Replace("B", "<a a='1'/>"));
    execute(new Replace("C", "<a a='1'/>"));
    execute(new Replace("A", "<a a='1'/>"));
    if(openClose) {
      execute(new Close());
      execute(new Open(NAME));
    }
    execute(new Delete("A"));
  }

  /**
   * Test.
   */
  @Test
  public void autooptimize() {
    set(MainOptions.AUTOOPTIMIZE, true);
    execute(new CreateDB(NAME));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
    execute(new Replace("x.xml", "<a>A</a>"));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
    query(_DB_REPLACE.args(NAME, "x.xml", "<a>B</a>"));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");

    set(MainOptions.AUTOOPTIMIZE, false);
    execute(new Optimize());
    execute(new Replace("x.xml", "<a>C</a>"));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "false");

    execute(new Optimize());
    query(_DB_INFO.args(NAME) + "//textindex/text()", "true");
    query(_DB_REPLACE.args(NAME, "x.xml", "<a>D</a>"));
    query(_DB_INFO.args(NAME) + "//textindex/text()", "false");
  }
}
