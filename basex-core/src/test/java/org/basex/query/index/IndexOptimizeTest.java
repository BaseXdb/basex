package org.basex.query.index;

import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.ast.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.index.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests if queries are rewritten for index access.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class IndexOptimizeTest extends QueryPlanTest {
  /** Creates a test database. */
  @BeforeClass public static void start() {
    execute(new DropDB(NAME));
    set(MainOptions.FTINDEX, true);
    set(MainOptions.TOKENINDEX, true);
    set(MainOptions.QUERYINFO, true);
  }

  /** Checks the open command. */
  @Test public void openDocTest() {
    createDoc();
    execute(new Open(NAME));
    check("//*[text() = '1']");
    check("data(//*[@* = 'y'])", 1);
    check("data(//@*[. = 'y'])", "y");
    check("//*[text() contains text '1']");
    check("//a[. = '1']");
    check("//xml[a = '1']");
    check(".[.//text() contains text '1']");
    check("for $s in ('x', '') return //*[text() = $s]", "");
  }

  /** Checks the open command. */
  @Test public void openCollTest() {
    createColl();
    execute(new Open(NAME));
    check("//*[text() = '1']");
    check("//*[text() contains text '1']");
    check("//a[. = '1']");
    check("//xml[a = '1']");
    check(".[.//text() contains text '1']");
    check("for $s in ('x', '') return //*[text() = $s]", "");
  }

  /** Checks the XQuery doc() function. */
  @Test public void docTest() {
    createDoc();
    final String func = DOC.args(NAME);
    check(func + "//*[text() = '1']");
    check(func + "//*[text() contains text '2']");
    check(func + "//a[. = '1']");
    check(func + "//xml[a = '1']");
    check(func + "/.[.//text() contains text '1']");
    check(func + "[.//text() contains text '1']");
    check("for $s in ('x', '') return " + func + "//*[text() = $s]", "");
  }

  /** Checks the XQuery collection() function. */
  @Test public void collTest() {
    createColl();
    final String func = COLLECTION.args(NAME);
    check(func + "//*[text() = '1']");
    check(func + "//*[text() contains text '2']");
    check(func + "//a[. = '1']");
    check(func + "//xml[a = '1']");
    check(func + "/.[.//text() contains text '1']");
    check(func + "[.//text() contains text '1']");
    check("for $s in ('x', '', string-join((1 to 513) ! 'a'))"
        + "return " + func + "//*[text() = $s]", "");
  }

  /** Checks the XQuery db:open() function. */
  @Test public void dbOpenTest() {
    createColl();
    final String func = _DB_OPEN.args(NAME);
    check(func + "//*[text() = '1']");
    check(func + "//*[text() contains text '2']");
    check(func + "//a[. = '1']");
    check(func + "//xml[a = '1']");
    check("for $s in ('x', '', string-join((1 to 513) ! 'a'))"
        + "return " + func + "//*[text() = $s]", "");
  }

  /** Checks the XQuery db:open() function, using a specific path. */
  @Test public void dbOpenExtTest() {
    createColl();
    final String func = _DB_OPEN.args(NAME, "two");
    check(func + "//*[text() = '1']", "");
    check(func + "//*[text() contains text '2']", "");
    check(func + "//a[. = '1']", "");
    check(func + "//xml[a = '1']", "");
    check(func + "//*[text() = '4']", "<a>4</a>");
    check("for $s in ('x', '', string-join((1 to 513) ! 'a'))"
        + "return " + func + "//*[text() = $s]", "");
  }

  /** Checks token requests. */
  @Test public void tokenTest() {
    createDoc();
    execute(new Open(NAME));
    check("data(//*[tokenize(@idref) = 'id1'])", 1);
    check("data(//@*[tokenize(.) = 'id1'])", "id1 id2");
    check("for $s in ('id2', 'id3') return data(//*[tokenize(@idref) = $s])", 1);
    check("for $s in ('id2', 'id3') return data(//@*[tokenize(.) = $s])", "id1 id2");

    check("data(//*[contains-token(@idref, 'id1')])", 1);
    check("data(//*[contains-token(@idref, '   id1  ')])", 1);
    check("data(//@*[contains-token(., 'id1')])", "id1 id2");
    check("for $s in ('id2', 'id3') return data(//*[contains-token(@idref, $s)])", 1);
    check("for $s in ('id2', 'id3') return data(//@*[contains-token(., $s)])", "id1 id2");
  }

  /** Checks full-text requests. */
  @Test public void ftTest() {
    createDoc();
    execute(new Open(NAME));
    check("data(//*[text() contains text '1'])", 1);
    check("data(//*[text() contains text '1 2' any word])", "1\n2 3");
    check("//*[text() contains text {'2','4'} all]", "");
    check("//*[text() contains text {'2','3'} all words]", "<a>2 3</a>");
    check("//*[text() contains text {'2','4'} all words]", "");
  }

  /** Checks if a full-text index with language option is used. */
  @Test public void ftTestLang() {
    set(MainOptions.LANGUAGE, "de");
    createDoc();
    execute(new Open(NAME));
    check("//text()[. contains text '1']");
    check("//text()[. contains text '1' using language 'de']");
    check("//text()[. contains text '1' using language 'German']");
  }

  /** Checks index optimizations inside functions. */
  @Test public void functionTest() {
    createColl();
    // document access after inlining
    check("declare function db:x($d) { collection($d)//text()[. = '1'] }; "
        + "db:x('" + NAME + "')", 1);
    check("declare function db:x($d, $s) { collection($d)//text()[. = $s] }; "
        + "db:x('" + NAME + "', '1')", 1);

    // text: search term must be string
    final String db = _DB_OPEN.args(NAME);
    check("declare function db:x() {" + db + "//text()[. = '1'] }; db:x()", 1);
    check("declare function db:x($x as xs:string) {" + db + "//text()[. = $x] }; db:x('1')", 1);
    // full-text: search term may have any type
    check("declare function db:x() {" + db + "//text()[. contains text '1'] }; db:x()", 1);
    check("declare function db:x($x) {" + db + "//text()[. contains text { $x }] }; db:x('1')", 1);

    // optimization in functions. GH-1553
    check("declare function db:f() { " + db + "//a[text() = '1'] }; db:f()", "<a>1</a>");
    check("declare function db:f() { collection('" + NAME + "')//text()[. = '1'] }; db:f()", "1");
  }

  /** Checks predicate tests for empty strings. */
  @Test public void emptyStrings() {
    createDoc();
    execute(new Open(NAME));
    query("//*[text() = '']", "");
    query("//text()[. = '']", "");
    query("//*[. = '']", "<a/>");
    query("//a[. = '']", "<a/>");
    query("//a[. = <x/>]", "<a/>");

    query("//a[not(text() = '')]/text()", "1\n2 3");
    query("//text()[not(. = '')]", "1\n2 3");
    query("//a[not(. = '')]/text()", "1\n2 3");
  }

  /** Checks the selective index feature. */
  @Test public void selectiveIndexTest() {
    try {
      // first run: use index; second run: no index
      for(final String include : new String[] { "a", "b" }) {
        set(MainOptions.TEXTINCLUDE, include);
        set(MainOptions.ATTRINCLUDE, include);
        execute(new CreateDB(NAME, "<xml><a a='1'>1</a></xml>"));

        final String test = exists(ValueAccess.class) + " = " + (include.equals("a") + "()");
        check("data(//*[a = '1'])", 1, test);
        check("data(//*[a/text() = '1'])", 1, test);
        check("data(//a[. = '1'])", 1, test);
        check("data(//a[text() = '1'])", 1, test);
        check("data(//*[*/@a = '1'])", 1, test);
        check("data(//*[@a = '1'])", 1, test);
        check("data(//@a[. = '1'])", 1, test);
      }
    } finally {
      set(MainOptions.TEXTINCLUDE, "");
      set(MainOptions.ATTRINCLUDE, "");
    }
  }

  /** Checks mixed downward and upward axes. */
  @Test public void upAndDown() {
    createDoc();
    execute(new Open(NAME));
    query("name(//@x/..[@x = 'y'])", "a");
    query("name(//@*/..[@* = 'y'])", "a");
    query("name(//@*/..[@x = 'y'])", "a");
    query("name(//@x/..[@* = 'y'])", "a");
  }

  /** Index access with nested predicates. */
  @Test public void gh1573() {
    execute(new CreateDB(NAME, "<xml><a>"
        + "<b>A</b></a><a>"
        + "<b>B</b><c>correct</c></a><a>"
        + "<b>B</b><c>wrong</c></a>"
        + "</xml>"));
    query("//c[../preceding-sibling::a[1]/b = 'A']", "<c>correct</c>");
  }

  /** Checks expressions with the pragma for enforcing index rewritings. */
  @Test public void pragma() {
    createDoc();
    final String pragma = "(# db:enforceindex #) { ";
    final String db = _DB_OPEN.args(" <x>" + NAME + "</x>");

    // rewritings possible
    check(pragma + db + "//a[text() = '1']/text() }", 1, exists(ValueAccess.class));
    check(pragma + db + "//a/text()[. = '1'] }", 1, exists(ValueAccess.class));
    check(pragma + db + "/*/@*[. = <_/> ] }", "", exists(ValueAccess.class));

    // no rewriting possible (database is referenced before index enforcement). GH-1696
    check(db + "/*/@*[" + pragma + ". = <_/> }]", "", empty(ValueAccess.class));
  }

  /** Optimizations of predicates that are changed by optimizations. */
  @Test public void gh1597() {
    execute(new CreateDB(NAME, "<x>A</x>"));
    check("let $s := 0 return *[if($s) then () else .//text() = 'A']", "<x>A</x>",
        exists(ValueAccess.class));
    check("let $s := 1 return *[if($s) then () else .//text() = 'A']", "", empty());
  }

  /**
   * Creates a test database.
   */
  private static void createDoc() {
    execute(new CreateDB(NAME, "<xml><a x='y' idref='id1 id2'>1</a><a>2 3</a><a/></xml>"));
    execute(new Close());
  }

  /**
   * Creates a test collection.
   */
  private static void createColl() {
    execute(new CreateDB(NAME));
    execute(new Add("one", "<xml><a>1</a><a>2 3</a></xml>"));
    execute(new Add("two", "<xml><a>4</a><a>5 6</a></xml>"));
    execute(new Optimize());
    execute(new Close());
  }

  /**
   * Check if specified query was rewritten for index access.
   * @param query query to be tested
   */
  private static void check(final String query) {
    check(query, null);
  }

  /**
   * Checks if specified query was rewritten for index access, and checks the query result.
   * @param query query to be tested
   * @param result result or {@code null} for no comparison
   */
  private static void check(final String query, final String result) {
    check(query, result, exists("*" +
        "[self::" + Util.className(ValueAccess.class) +
        "|self::" + Util.className(FTIndexAccess.class) + ']'));
  }
}
