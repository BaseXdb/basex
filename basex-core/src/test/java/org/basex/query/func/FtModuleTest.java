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
 * @author BaseX Team, BSD License
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
    query(func.args("Exercise 1", "('exercise', '1')"), true);
    query(func.args("Exercise 1", wrap(1)), true);
    query(func.args("Exercise 1", "1"), true);
    query(func.args("Exercise 1", "X"), false);
    query(func.args(" ('A', 'B')", " ('C', 'B')"), true);

    // check match options
    query(func.args("Assignments", "Azzignments", " { 'fuzzy': 'yes' }"), true);
    query(func.args("Assignments", "Azzignments", " { 'fuzzy': 'no' }"), false);
    query(func.args("Assignments", "assignment", " { 'stemming': true() }"), true);
    query(func.args("Assignment", "assignments", " { 'stemming': true() }"), true);
    query(func.args("A", "a", " { 'case': 'upper' }"), true);
    query(func.args("a", "A", " { 'case': 'lower' }"), true);
    query(func.args("A", "a", " { 'case': 'insensitive' }"), true);
    query(func.args("A", "a", " { 'case': 'sensitive' }"), false);
    // check search modes
    query(func.args("Exercise 1", "1 Exercise", " { 'mode': 'phrase' }"), false);
    query(func.args("Exercise 1", "1 Exercise", " { 'mode': 'all' }"), false);
    query(func.args("Exercise 1", "1 Exercise", " { 'mode': 'any' }"), false);
    query(func.args("Exercise 1", "1 Exercise", " { 'mode': 'any word' }"), true);
    query(func.args("Exercise 1", "1 Exercise", " { 'mode': 'all words' }"), true);

    query(func.args("databases and xml", "databases xml",
        " { 'mode': 'all words', 'distance': { 'min': 0, 'max': 1 } }"), true);
    query(func.args("databases and xml", "databases xml",
        " { 'mode': 'all words', 'distance': { 'max': 0 } }"), false);
    query(func.args("databases and xml", "databases xml",
        " { 'mode': 'all words', 'window': { 'size': 3 } }"), true);

    // GH-2296
    query(func.args("serch", "serch", " { 'fuzzy': true() }"), true);
    query(func.args("surch", "serch", " { 'fuzzy': true() }"), true);
    query(func.args("serch", "surch", " { 'fuzzy': true() }"), true);
    query(func.args("行イ音便", "行イ音便", " { 'fuzzy': true() }"), true);
    query(func.args("行イ音音", "行イ音便", " { 'fuzzy': true() }"), true);
    query(func.args("行イ音便", "行イ音音", " { 'fuzzy': true() }"), true);
    query(func.args("イイ音便", "行イ音便", " { 'fuzzy': true() }"), true);
    query(func.args("行イ音便", "イイ音便", " { 'fuzzy': true() }"), true);

    query(func.args("行イ音便", "serch", " { 'fuzzy': true() }"), false);
    query(func.args("serch", "行イ音便", " { 'fuzzy': true() }"), false);

    // check buggy options
    error(func.args("x", "x", " { 'x': 'y' }"), INVALIDOPTION_X);
    error(func.args("x", "x", " { 'mode': '' }"), INVALIDOPTION_X);
    error(func.args("x", "x", " 1"), INVCONVERT_X_X_X);
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
      "<li>Exercise <mark>1</mark></li>");
    query(func.args(" //*[text() contains text '2'], 'b', 20"),
      "<li>Exercise <b>2</b></li>");
    query(func.args(" //*[text() contains text '2'], '_o_', 1"),
      "<li>...<_o_>2</_o_></li>");
    contains(func.args(" //*[text() contains text 'Exercise'], 'b', 1"),
      "<li>...</li>");
  }

  /** Test method. */
  @Test public void mark() {
    final Function func = _FT_MARK;
    query(func.args(" //*[text() contains text '1']"),
      "<li>Exercise <mark>1</mark></li>");
    query(func.args(" //*[text() contains text '2'], 'b'"),
      "<li>Exercise <b>2</b></li>");
    contains(func.args(" //*[text() contains text 'Exercise']"),
      "<li><mark>Exercise</mark> 1</li>");
    query("copy $a := text { 'a b' } modify () return " +
        func.args(" $a[. contains text 'a']", "b"), "<b>a</b>\n b");
    query("copy $a := text { 'ab' } modify () return " +
        func.args(" $a[. contains text 'ab'], 'b'"), "<b>ab</b>");
    query("copy $a := text { 'a b' } modify () return " +
        func.args(" $a[. contains text 'a b'], 'b'"), "<b>a</b>\n \n<b>b</b>");

    query(COUNT.args(func.args(" //*[text() contains text '1']/../../../../..")), 1);

    execute(new CreateDB(NAME, "<a:a xmlns:a='A'>C</a:a>"));
    query(func.args(" /descendant::*[text() contains text 'C']", "b"),
        "<a:a xmlns:a=\"A\"><b>C</b></a:a>");
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
    query(func.args("A bc", " { 'case': 'sensitive' }"), "A bc");
    query(func.args("\u00e4", " { 'diacritics': 'sensitive' }"), "\u00e4");
    query(func.args("gifts", " { 'stemming': 'true' }"), "gift");

    query("declare ft-option using stemming; " + func.args("Gifts"), "gift");
    query(func.args(""), "");
    query(func.args("a!b:c"), "a!b:c");

    query(_FT_NORMALIZE.args("&#778;", " { 'stemming': true(), 'language': 'de' }"), "");
    query("'/' ! " + func.args(" ."), "/");

    query(_FT_NORMALIZE.args("a*", " { 'stemming': true(), 'language': 'de' }"), "a*");
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
    query(func.args(NAME, " ('exercise', '1')"), "Exercise 1\nExercise 2");
    query(func.args(NAME, wrap(1)), "Exercise 1");
    query(func.args(NAME, "1"), "Exercise 1");
    query(func.args(NAME, "XXX"), "");

    // apply index options to query term
    set(MainOptions.STEMMING, true);
    execute(new CreateIndex("fulltext"));
    contains(func.args(NAME, "Exercises") + "/..", "<li>Exercise 1</li>");
    set(MainOptions.STEMMING, false);
    execute(new CreateIndex(CmdIndex.FULLTEXT));

    // check match options
    query(func.args(NAME, "Assignments", " {}"), "Assignments");
    query(func.args(NAME, "Azzignments", " { 'fuzzy': 'yes' }"), "Assignments");
    query(func.args(NAME, "Azzignments", " { 'fuzzy': 'no' }"), "");
    // check search modes
    query(func.args(NAME, "1 Exercise", " { 'mode': 'phrase' }"), "");
    query(func.args(NAME, "1 Exercise", " { 'mode': 'all' }"), "");
    query(func.args(NAME, "1 Exercise", " { 'mode': 'any' }"), "");
    query(func.args(NAME, "1 Exercise", " { 'mode': 'any word' }"), "Exercise 1\nExercise 2");
    query(func.args(NAME, "1 Exercise", " { 'mode': 'all words' }"), "Exercise 1");

    query(func.args(NAME, "databases xml",
        " { 'mode': 'all words', 'distance': { 'min': 0, 'max': 1 } }"),
        "Databases and XML");
    query(func.args(NAME, "databases xml",
        " { 'mode': 'all words', 'distance': { 'max': 0 } }"),
        "");
    query(func.args(NAME, "databases xml",
        " { 'mode': 'all words', 'window': { 'size': 3 } }"),
        "Databases and XML");

    // check buggy options
    error(func.args(NAME, "x", " { 'x': 'y' }"), INVALIDOPTION_X);
    error(func.args(NAME, "x", " { 'mode': '' }"), INVALIDOPTION_X);
    error(func.args(NAME, "x", " 1"), INVCONVERT_X_X_X);
  }

  /** Test method. */
  @Test public void thesaurus() {
    final Function func = _FT_THESAURUS;
    final String doc = " doc('src/test/resources/thesaurus.xml')";

    query(func.args(doc, "happy"), "lucky\nhappy");
    query(func.args(doc, "happy", " { 'levels': 0 }"), "");
    query(func.args(doc, "happy", " { 'levels': 5 }"), "lucky\nhappy");
    query(func.args(doc, "happy", " { 'relationship': 'RT' }"), "lucky\nhappy");
    query(func.args(doc, "happy", " { 'relationship': 'XYZ' }"), "");
  }

  /** Test method. */
  @Test public void tokenize() {
    final Function func = _FT_TOKENIZE;

    query(func.args(" ()"), "");
    query(func.args(" []"), "");

    query(func.args("A bc"), "a\nbc");
    query(func.args("A bc", " { 'case': 'sensitive' }"), "A\nbc");
    query(func.args("\u00e4", " { 'diacritics': 'sensitive' }"), "\u00e4");
    query(func.args("gifts", " { 'stemming': 'true' }"), "gift");

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
