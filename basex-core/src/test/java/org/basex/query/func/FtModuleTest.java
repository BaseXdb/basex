package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.*;
import org.basex.index.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the functions of the Fulltext Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FtModuleTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";

  /**
   * Initializes a test.
   */
  @BeforeEach public void initTest() {
    execute(new CreateDB(NAME, FILE));
    execute(new CreateIndex(CmdIndex.FULLTEXT));
  }

  /** Test method. */
  @Test public void contains() {
    final Function func = _FT_CONTAINS;

    // check index results
    query(func.args("Assignments", "assignments"), true);
    query(func.args("Exercise 1", "('exercise','1')"), true);
    query(func.args("Exercise 1", " <x>1</x>"), true);
    query(func.args("Exercise 1", "1"), true);
    query(func.args("Exercise 1", "X"), false);
    query(func.args(" ('A','B')", " ('C','B')"), true);

    // check match options
    query(func.args("Assignments", "Azzignments", " map { 'fuzzy':'yes' }"), true);
    query(func.args("Assignments", "Azzignments", " map { 'fuzzy':'no' }"), false);
    query(func.args("Assignments", "assignment", " map { 'stemming':true() }"), true);
    query(func.args("Assignment", "assignments", " map { 'stemming':true() }"), true);
    query(func.args("A", "a", " map { 'case':'upper' }"), true);
    query(func.args("a", "A", " map { 'case':'lower' }"), true);
    query(func.args("A", "a", " map { 'case':'insensitive' }"), true);
    query(func.args("A", "a", " map { 'case':'sensitive' }"), false);
    // check search modes
    query(func.args("Exercise 1", "1 Exercise", " map { 'mode':'phrase' }"), false);
    query(func.args("Exercise 1", "1 Exercise", " map { 'mode':'all' }"), false);
    query(func.args("Exercise 1", "1 Exercise", " map { 'mode':'any' }"), false);
    query(func.args("Exercise 1", "1 Exercise", " map { 'mode':'any word' }"), true);
    query(func.args("Exercise 1", "1 Exercise", " map { 'mode':'all words' }"), true);

    query(func.args("databases and xml", "databases xml",
        " map { 'mode': 'all words', 'distance':map { 'min': 0, 'max': 1 } }"), true);
    query(func.args("databases and xml", "databases xml",
        " map { 'mode': 'all words', 'distance':map { 'max': 0 } }"), false);
    query(func.args("databases and xml", "databases xml",
        " map { 'mode': 'all words', 'window':map { 'size': 3 } }"), true);

    // check buggy options
    error(func.args("x", "x", " map { 'x': 'y' }"), INVALIDOPT_X);
    error(func.args("x", "x", " map { 'mode': '' }"), INVALIDOPT_X);
    error(func.args("x", "x", " 1"), MAP_X_X);
  }

  /** Test method. */
  @Test public void count() {
    final Function func = _FT_COUNT;
    query(func.args(" ()"), 0);
    query(func.args(" //*[text() contains text '1']"), 1);
    query(func.args(" //li[text() contains text 'exercise']"), 2);
    query("for $i in //li[text() contains text 'exercise'] return " +
       func.args(" $i[text() contains text 'exercise']"), "1\n1");
  }

  /** Test method. */
  @Test public void extract() {
    final Function func = _FT_EXTRACT;
    query(func.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark>\n</li>");
    query(func.args(" //*[text() contains text '2'], 'b', 20"),
      "<li>Exercise <b>2</b>\n</li>");
    query(func.args(" //*[text() contains text '2'], '_o_', 1"),
      "<li>...<_o_>2</_o_>\n</li>");
    contains(func.args(" //*[text() contains text 'Exercise'], 'b', 1"),
      "<li>...</li>");
  }

  /** Test method. */
  @Test public void mark() {
    final Function func = _FT_MARK;
    query(func.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark>\n</li>");
    query(func.args(" //*[text() contains text '2'], 'b'"),
      "<li>Exercise <b>2</b>\n</li>");
    contains(func.args(" //*[text() contains text 'Exercise']"),
      "<li>\n<mark>Exercise</mark> 1</li>");
    query("copy $a := text { 'a b' } modify () return " +
        func.args(" $a[. contains text 'a']", "b"), "<b>a</b>\nb");
    query("copy $a := text { 'ab' } modify () return " +
        func.args(" $a[. contains text 'ab'], 'b'"), "<b>ab</b>");
    query("copy $a := text { 'a b' } modify () return " +
        func.args(" $a[. contains text 'a b'], 'b'"), "<b>a</b>\n\n<b>b</b>");

    query(COUNT.args(func.args(" //*[text() contains text '1']/../../../../..")), 1);

    execute(new CreateDB(NAME, "<a:a xmlns:a='A'>C</a:a>"));
    query(func.args(" /descendant::*[text() contains text 'C']", "b"),
        "<a:a xmlns:a=\"A\">\n<b>C</b>\n</a:a>");
    execute(new DropDB(NAME));
    query("copy $c := <A xmlns='A'>A</A> modify () return <X>{ " +
        func.args(" $c[text() contains text 'A']") + " }</X>/*");
  }

  /** Test method. */
  @Test public void normalize() {
    final Function func = _FT_NORMALIZE;

    query(func.args(" ()"), "");
    query(func.args(" []"), "");

    query(func.args("A bc"), "a bc");
    query(func.args("A bc", " map { 'case': 'sensitive' }"), "A bc");
    query(func.args("\u00e4", " map { 'diacritics': 'sensitive' }"), "\u00e4");
    query(func.args("gifts", " map { 'stemming': 'true' }"), "gift");

    query("declare ft-option using stemming; " + func.args("Gifts"), "gift");
    query(func.args(""), "");
    query(func.args("a!b:c"), "a!b:c");

    query("ft:normalize('&#778;', map { 'stemming': true(), 'language': 'de' })", "");
    query("'/' ! " + func.args(" ."), "/");
  }

  /** Test method. */
  @Test public void score() {
    final Function func = _FT_SCORE;
    query(func.args(_FT_SEARCH.args(NAME, "2")), 1);
    query(func.args(_FT_SEARCH.args(NAME, "XML")), "1\n0.5");
  }

  /** Test method. */
  @Test public void search() {
    final Function func = _FT_SEARCH;

    // check index results
    query(func.args(NAME, "assignments"), "Assignments");
    query(func.args(NAME, " ('exercise','1')"), "Exercise 1\nExercise 2");
    query(func.args(NAME, " <x>1</x>"), "Exercise 1");
    query(func.args(NAME, "1"), "Exercise 1");
    query(func.args(NAME, "XXX"), "");

    // apply index options to query term
    set(MainOptions.STEMMING, true);
    execute(new CreateIndex("fulltext"));
    contains(func.args(NAME, "Exercises") + "/..", "<li>Exercise 1</li>");
    set(MainOptions.STEMMING, false);
    execute(new CreateIndex(CmdIndex.FULLTEXT));

    // check match options
    query(func.args(NAME, "Assignments", " map { }"), "Assignments");
    query(func.args(NAME, "Azzignments", " map { 'fuzzy':'yes' }"), "Assignments");
    query(func.args(NAME, "Azzignments", " map { 'fuzzy':'no' }"), "");
    // check search modes
    query(func.args(NAME, "1 Exercise", " map { 'mode':'phrase' }"), "");
    query(func.args(NAME, "1 Exercise", " map { 'mode':'all' }"), "");
    query(func.args(NAME, "1 Exercise", " map { 'mode':'any' }"), "");
    query(func.args(NAME, "1 Exercise", " map { 'mode':'any word' }"), "Exercise 1\nExercise 2");
    query(func.args(NAME, "1 Exercise", " map { 'mode':'all words' }"), "Exercise 1");

    query(func.args(NAME, "databases xml",
        " map { 'mode': 'all words', 'distance':map { 'min': 0, 'max': 1 } }"),
        "Databases and XML");
    query(func.args(NAME, "databases xml",
        " map { 'mode': 'all words', 'distance':map { 'max': 0 } }"),
        "");
    query(func.args(NAME, "databases xml",
        " map { 'mode': 'all words', 'window':map { 'size': 3 } }"),
        "Databases and XML");

    // check buggy options
    error(func.args(NAME, "x", " map { 'x': 'y' }"), INVALIDOPT_X);
    error(func.args(NAME, "x", " map { 'mode': '' }"), INVALIDOPT_X);
    error(func.args(NAME, "x", " 1"), MAP_X_X);
  }

  /** Test method. */
  @Test public void tokenize() {
    final Function func = _FT_TOKENIZE;

    query(func.args(" ()"), "");
    query(func.args(" []"), "");

    query(func.args("A bc"), "a\nbc");
    query(func.args("A bc", " map { 'case': 'sensitive' }"), "A\nbc");
    query(func.args("\u00e4", " map { 'diacritics': 'sensitive' }"), "\u00e4");
    query(func.args("gifts", " map { 'stemming': 'true' }"), "gift");

    query("declare ft-option using stemming; " + func.args("Gifts"), "gift");
    query("count(" + func.args("") + ')', 0);
    query("count(" + func.args("a!b:c") + ')', 3);
  }

  /** Test method. */
  @Test public void tokens() {
    final Function func = _FT_TOKENS;
    execute(new CreateIndex(IndexType.FULLTEXT));

    String entries = func.args(NAME);
    query("count(" + entries + ')', 7);
    query("exists(" + entries + "/self::entry)", true);
    query(entries + "/@count = 1", true);
    query(entries + "/@count = 2", true);
    query(entries + "/@count = 3", false);

    entries = func.args(NAME, "a");
    query("count(" + entries + ')', 2);
  }
}
