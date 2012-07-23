package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.index.*;
import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery full-text extensions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNFtTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";

  /**
   * Initializes a test.
   * @throws BaseXException database exception
   */
  @Before
  public void initTest() throws BaseXException {
    new CreateDB(NAME, FILE).execute(context);
    new CreateIndex("fulltext").execute(context);
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void search() throws BaseXException {
    // check index results
    query(_FT_SEARCH.args(" . ", "assignments"), "Assignments");
    query(_FT_SEARCH.args(" . ", " ('exercise','1')"), "Exercise 1Exercise 2");
    query(_FT_SEARCH.args(" . ", "<x>1</x>"), "Exercise 1");
    query(_FT_SEARCH.args(" . ", "1"), "Exercise 1");
    query(_FT_SEARCH.args(" . ", "XXX"), "");

    // apply index options to query term
    new Set(Prop.STEMMING, true).execute(context);
    new CreateIndex("fulltext").execute(context);
    contains(_FT_SEARCH.args(" . ", "Exercises") + "/..",
        "<li>Exercise 1</li>");
    new Set(Prop.STEMMING, false).execute(context);
    new CreateIndex("fulltext").execute(context);

    // check match options
    query(_FT_SEARCH.args(" .", "Assignments", " map {}"), "Assignments");
    query(_FT_SEARCH.args(" .", "Azzignments", " map { 'fuzzy':='' }"), "Assignments");
    query(_FT_SEARCH.args(" .", "Azzignments", " map { 'fuzzy':='no' }"), "");
    // check search modes
    query(_FT_SEARCH.args(" .", "1 Exercise", " map { 'mode':='phrase' }"), "");
    query(_FT_SEARCH.args(" .", "1 Exercise", " map { 'mode':='all' }"), "");
    query(_FT_SEARCH.args(" .", "1 Exercise", " map { 'mode':='any' }"), "");
    query(_FT_SEARCH.args(" .", "1 Exercise", " map { 'mode':='any word' }"),
        "Exercise 1Exercise 2");
    query(_FT_SEARCH.args(" .", "1 Exercise", " map { 'mode':='all words' }"),
        "Exercise 1");

    // check buggy options
    error(_FT_SEARCH.args(" .", "x", " map { 'x':='y' }"), Err.ELMOPTION);
    error(_FT_SEARCH.args(" .", "x", " map { 'mode':='' }"), Err.ELMOPTION);
    error(_FT_SEARCH.args(" .", "x", " 1"), Err.ELMMAPTYPE);
  }

  /** Test method. */
  @Test
  public void count() {
    query(_FT_COUNT.args("()"), "0");
    query(_FT_COUNT.args(" //*[text() contains text '1']"), "1");
    query(_FT_COUNT.args(" //li[text() contains text 'exercise']"), "2");
    query("for $i in //li[text() contains text 'exercise'] return " +
        _FT_COUNT.args("$i[text() contains text 'exercise']"), "1 1");
  }

  /** Test method. */
  @Test
  public void mark() {
    query(_FT_MARK.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark></li>");
    query(_FT_MARK.args(" //*[text() contains text '2'], 'b'"),
      "<li>Exercise <b>2</b></li>");
    contains(_FT_MARK.args(" //*[text() contains text 'Exercise']"),
      "<li><mark>Exercise</mark> 1</li>");
    query("copy $a := text { 'a b' } modify () return " +
        _FT_MARK.args("$a[. contains text 'a']", "b"), "<b>a</b> b");
    query("copy $a := text { 'ab' } modify () return " +
        _FT_MARK.args("$a[. contains text 'ab'], 'b'"), "<b>ab</b>");
    query("copy $a := text { 'a b' } modify () return " +
        _FT_MARK.args("$a[. contains text 'a b'], 'b'"), "<b>a</b> <b>b</b>");
  }

  /**
   * Test method.
   * @throws IOException query exception
   */
  @Test
  public void extract() throws IOException {
    query(_FT_EXTRACT.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark></li>");
    query(_FT_EXTRACT.args(" //*[text() contains text '2'], 'b', 20"),
      "<li>Exercise <b>2</b></li>");
    query(_FT_EXTRACT.args(" //*[text() contains text '2'], '_o_', 1"),
      "<li>...<_o_>2</_o_></li>");
    contains(_FT_EXTRACT.args(" //*[text() contains text 'Exercise'], 'b', 1"),
      "<li><b>Exercise</b>...</li>");

    new CreateDB(NAME, "<a:a xmlns:a='A'>C</a:a>").execute(context);
    query(_FT_EXTRACT.args(" /descendant::*[text() contains text 'C']", 'b'),
        "<a:a xmlns:a=\"A\"><b>C</b></a:a>");
    new DropDB(NAME).execute(context);
  }

  /** Test method. */
  @Test
  public void score() {
    query(_FT_SCORE.args(_FT_SEARCH.args(" . ", "2")), "1");
    query(_FT_SCORE.args(_FT_SEARCH.args(" . ", "XML")), "1 0.5");
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void tokens() throws BaseXException {
    new CreateIndex(IndexType.FULLTEXT).execute(context);

    String entries = _FT_TOKENS.args(NAME);
    query("count(" + entries + ')', 6);
    query("exists(" + entries + "/self::entry)", "true");
    query(entries + "/@count = 1", "true");
    query(entries + "/@count = 2", "true");
    query(entries + "/@count = 3", "false");

    entries = _FT_TOKENS.args(NAME, "a");
    query("count(" + entries + ')', 1);
  }

  /** Test method. */
  @Test
  public void tokenize() {
    query(_FT_TOKENIZE.args("A bc"), "a bc");
    query("declare ft-option using stemming; " + _FT_TOKENIZE.args("Gifts"), "gift");
    query("count(" + _FT_TOKENIZE.args("") + ')', "0");
    query("count(" + _FT_TOKENIZE.args("a!b:c") + ')', "3");
  }
}
