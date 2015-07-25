package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.CmdIndex;
import org.basex.index.*;
import org.basex.query.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the functions of the Fulltext Module.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FtModuleTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";

  /**
   * Initializes a test.
   * @throws BaseXException database exception
   */
  @Before
  public void initTest() throws BaseXException {
    new CreateDB(NAME, FILE).execute(context);
    new CreateIndex(CmdIndex.FULLTEXT).execute(context);
  }

  /**
   * Test method.
   */
  @Test
  public void contains() {
    // check index results
    query(_FT_CONTAINS.args("Assignments", "assignments"), true);
    query(_FT_CONTAINS.args("Exercise 1", "('exercise','1')"), true);
    query(_FT_CONTAINS.args("Exercise 1", "<x>1</x>"), true);
    query(_FT_CONTAINS.args("Exercise 1", "1"), true);
    query(_FT_CONTAINS.args("Exercise 1", "X"), false);
    query(_FT_CONTAINS.args("('A','B')", "('C','B')"), true);

    // check match options
    query(_FT_CONTAINS.args("Assignments", "Azzignments", " map { 'fuzzy':'yes' }"), true);
    query(_FT_CONTAINS.args("Assignments", "Azzignments", " map { 'fuzzy':'no' }"), false);
    query(_FT_CONTAINS.args("Assignments", "assignment", " map { 'stemming':true() }"), true);
    query(_FT_CONTAINS.args("Assignment", "assignments", " map { 'stemming':true() }"), true);
    query(_FT_CONTAINS.args("A", "a", " map { 'case':'upper' }"), true);
    query(_FT_CONTAINS.args("a", "A", " map { 'case':'lower' }"), true);
    query(_FT_CONTAINS.args("A", "a", " map { 'case':'insensitive' }"), true);
    query(_FT_CONTAINS.args("A", "a", " map { 'case':'sensitive' }"), false);
    // check search modes
    query(_FT_CONTAINS.args("Exercise 1", "1 Exercise", " map { 'mode':'phrase' }"), false);
    query(_FT_CONTAINS.args("Exercise 1", "1 Exercise", " map { 'mode':'all' }"), false);
    query(_FT_CONTAINS.args("Exercise 1", "1 Exercise", " map { 'mode':'any' }"), false);
    query(_FT_CONTAINS.args("Exercise 1", "1 Exercise", " map { 'mode':'any word' }"), true);
    query(_FT_CONTAINS.args("Exercise 1", "1 Exercise", " map { 'mode':'all words' }"), true);

    query(_FT_CONTAINS.args("databases and xml", "databases xml", " map { 'mode':'all words'," +
        "'distance':map {'min':0,'max':1} }"), true);
    query(_FT_CONTAINS.args("databases and xml", "databases xml", " map { 'mode':'all words'," +
        "'distance':map {'max':0} }"), false);
    query(_FT_CONTAINS.args("databases and xml", "databases xml", " map { 'mode':'all words'," +
        "'window':map {'size':3} }"), true);

    // check buggy options
    error(_FT_CONTAINS.args("x", "x", " map { 'x':'y' }"), INVALIDOPT_X);
    error(_FT_CONTAINS.args("x", "x", " map { 'mode':'' }"), INVALIDOPT_X);
    error(_FT_CONTAINS.args("x", "x", " 1"), ELMMAP_X_X_X);
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void search() throws BaseXException {
    // check index results
    query(_FT_SEARCH.args(NAME, "assignments"), "Assignments");
    query(_FT_SEARCH.args(NAME, " ('exercise','1')"), "Exercise 1\nExercise 2");
    query(_FT_SEARCH.args(NAME, "<x>1</x>"), "Exercise 1");
    query(_FT_SEARCH.args(NAME, "1"), "Exercise 1");
    query(_FT_SEARCH.args(NAME, "XXX"), "");

    // apply index options to query term
    new Set(MainOptions.STEMMING, true).execute(context);
    new CreateIndex("fulltext").execute(context);
    contains(_FT_SEARCH.args(NAME, "Exercises") + "/..", "<li>Exercise 1</li>");
    new Set(MainOptions.STEMMING, false).execute(context);
    new CreateIndex(CmdIndex.FULLTEXT).execute(context);

    // check match options
    query(_FT_SEARCH.args(NAME, "Assignments", " map { }"), "Assignments");
    query(_FT_SEARCH.args(NAME, "Azzignments", " map { 'fuzzy':'yes' }"), "Assignments");
    query(_FT_SEARCH.args(NAME, "Azzignments", " map { 'fuzzy':'no' }"), "");
    // check search modes
    query(_FT_SEARCH.args(NAME, "1 Exercise", " map { 'mode':'phrase' }"), "");
    query(_FT_SEARCH.args(NAME, "1 Exercise", " map { 'mode':'all' }"), "");
    query(_FT_SEARCH.args(NAME, "1 Exercise", " map { 'mode':'any' }"), "");
    query(_FT_SEARCH.args(NAME, "1 Exercise", " map { 'mode':'any word' }"),
        "Exercise 1\nExercise 2");
    query(_FT_SEARCH.args(NAME, "1 Exercise", " map { 'mode':'all words' }"),
        "Exercise 1");

    query(_FT_SEARCH.args(NAME, "databases xml", " map { 'mode':'all words'," +
        "'distance':map {'min':0,'max':1} }"), "Databases and XML");
    query(_FT_SEARCH.args(NAME, "databases xml", " map { 'mode':'all words'," +
        "'distance':map {'max':0} }"), "");
    query(_FT_SEARCH.args(NAME, "databases xml", " map { 'mode':'all words'," +
        "'window':map {'size':3} }"), "Databases and XML");

    // check buggy options
    error(_FT_SEARCH.args(NAME, "x", " map { 'x':'y' }"), INVALIDOPT_X);
    error(_FT_SEARCH.args(NAME, "x", " map { 'mode':'' }"), INVALIDOPT_X);
    error(_FT_SEARCH.args(NAME, "x", " 1"), ELMMAP_X_X_X);
  }

  /** Test method. */
  @Test
  public void count() {
    query(_FT_COUNT.args("()"), "0");
    query(_FT_COUNT.args(" //*[text() contains text '1']"), "1");
    query(_FT_COUNT.args(" //li[text() contains text 'exercise']"), "2");
    query("for $i in //li[text() contains text 'exercise'] return " +
       _FT_COUNT.args("$i[text() contains text 'exercise']"), "1\n1");
  }

  /**
   * Test method.
   * @throws IOException query exception
   */
  @Test
  public void mark() throws IOException {
    query(_FT_MARK.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark>\n</li>");
    query(_FT_MARK.args(" //*[text() contains text '2'], 'b'"),
      "<li>Exercise <b>2</b>\n</li>");
    contains(_FT_MARK.args(" //*[text() contains text 'Exercise']"),
      "<li>\n<mark>Exercise</mark> 1</li>");
    query("copy $a := text { 'a b' } modify () return " +
        _FT_MARK.args("$a[. contains text 'a']", "b"), "<b>a</b>\nb");
    query("copy $a := text { 'ab' } modify () return " +
        _FT_MARK.args("$a[. contains text 'ab'], 'b'"), "<b>ab</b>");
    query("copy $a := text { 'a b' } modify () return " +
        _FT_MARK.args("$a[. contains text 'a b'], 'b'"), "<b>a</b>\n\n<b>b</b>");

    query(COUNT.args(_FT_MARK.args(" //*[text() contains text '1']/../../../../..")), "1");

    new CreateDB(NAME, "<a:a xmlns:a='A'>C</a:a>").execute(context);
    query(_FT_MARK.args(" /descendant::*[text() contains text 'C']", 'b'),
        "<a:a xmlns:a=\"A\">\n<b>C</b>\n</a:a>");
    new DropDB(NAME).execute(context);
    query("copy $c := <A xmlns='A'>A</A> modify () return <X>{ " +
        _FT_MARK.args(" $c[text() contains text 'A']") + " }</X>/*");
  }

  /** Test method. */
  @Test
  public void extract() {
    query(_FT_EXTRACT.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark>\n</li>");
    query(_FT_EXTRACT.args(" //*[text() contains text '2'], 'b', 20"),
      "<li>Exercise <b>2</b>\n</li>");
    query(_FT_EXTRACT.args(" //*[text() contains text '2'], '_o_', 1"),
      "<li>...<_o_>2</_o_>\n</li>");
    contains(_FT_EXTRACT.args(" //*[text() contains text 'Exercise'], 'b', 1"),
      "<li>...</li>");
  }

  /** Test method. */
  @Test
  public void score() {
    query(_FT_SCORE.args(_FT_SEARCH.args(NAME, "2")), "1");
    query(_FT_SCORE.args(_FT_SEARCH.args(NAME, "XML")), "1\n0.5");
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void tokens() throws BaseXException {
    new CreateIndex(IndexType.FULLTEXT).execute(context);

    String entries = _FT_TOKENS.args(NAME);
    query("count(" + entries + ')', 7);
    query("exists(" + entries + "/self::entry)", "true");
    query(entries + "/@count = 1", "true");
    query(entries + "/@count = 2", "true");
    query(entries + "/@count = 3", "false");

    entries = _FT_TOKENS.args(NAME, "a");
    query("count(" + entries + ')', 2);
  }

  /** Test method. */
  @Test
  public void tokenize() {
    query(_FT_TOKENIZE.args("A bc"), "a\nbc");
    query(_FT_TOKENIZE.args("A bc", " map { 'case': 'sensitive' }"), "A\nbc");
    query(_FT_TOKENIZE.args("\u00e4", " map { 'diacritics': 'sensitive' }"), "\u00e4");
    query(_FT_TOKENIZE.args("gifts", " map { 'stemming': 'true' }"), "gift");

    query("declare ft-option using stemming; " + _FT_TOKENIZE.args("Gifts"), "gift");
    query("count(" + _FT_TOKENIZE.args("") + ')', "0");
    query("count(" + _FT_TOKENIZE.args("a!b:c") + ')', "3");
  }

  /** Test method. */
  @Test
  public void normalize() {
    query(_FT_NORMALIZE.args("A bc"), "a bc");
    query(_FT_NORMALIZE.args("A bc", " map { 'case': 'sensitive' }"), "A bc");
    query(_FT_NORMALIZE.args("\u00e4", " map { 'diacritics': 'sensitive' }"), "\u00e4");
    query(_FT_NORMALIZE.args("gifts", " map { 'stemming': 'true' }"), "gift");

    query("declare ft-option using stemming; " + _FT_NORMALIZE.args("Gifts"), "gift");
    query(_FT_NORMALIZE.args(""), "");
    query(_FT_NORMALIZE.args("a!b:c"), "a!b:c");

    query("ft:normalize('&#778;', map { 'stemming': true(), 'language': 'de' })", "");
    query("'/' ! " + _FT_NORMALIZE.args(" ."), "/");
  }
}
