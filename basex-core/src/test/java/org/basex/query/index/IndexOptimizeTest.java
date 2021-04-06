package org.basex.query.index;

import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.ast.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.index.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests if queries are rewritten for index access.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IndexOptimizeTest extends QueryPlanTest {
  /** Creates a test database. */
  @BeforeAll public static void start() {
    execute(new DropDB(NAME));
    set(MainOptions.FTINDEX, true);
    set(MainOptions.TOKENINDEX, true);
    set(MainOptions.QUERYINFO, true);
  }

  /** Resets optimizations. */
  @AfterEach public void init() {
    inline(false);
    unroll(false);
  }

  /** Checks the open command. */
  @Test public void openDocTest() {
    createDoc();
    execute(new Open(NAME));
    indexCheck("//*[text() = '1']");
    indexCheck("data(//*[@* = 'y'])", 1);
    indexCheck("data(//@*[. = 'y'])", "y");
    indexCheck("//*[text() contains text '1']");
    indexCheck("//a[. = '1']");
    indexCheck("//xml[a = '1']");
    indexCheck(".[.//text() contains text '1']");
    indexCheck("for $s in ('x', '') return //*[text() = $s]", "");
  }

  /** Checks the open command. */
  @Test public void openCollTest() {
    createColl();
    execute(new Open(NAME));
    indexCheck("//*[text() = '1']");
    indexCheck("//*[text() contains text '1']");
    indexCheck("//a[. = '1']");
    indexCheck("//xml[a = '1']");
    indexCheck(".[.//text() contains text '1']");
    indexCheck("for $s in ('x', '') return //*[text() = $s]", "");
  }

  /** Checks the XQuery doc() function. */
  @Test public void docTest() {
    createDoc();
    final String func = DOC.args(NAME);
    indexCheck(func + "//*[text() = '1']");
    indexCheck(func + "//*[text() contains text '2']");
    indexCheck(func + "//a[. = '1']");
    indexCheck(func + "//xml[a = '1']");
    indexCheck(func + "/.[.//text() contains text '1']");
    indexCheck(func + "[.//text() contains text '1']");
    indexCheck("for $s in ('x', '') return " + func + "//*[text() = $s]", "");
  }

  /** Checks the XQuery collection() function. */
  @Test public void collTest() {
    createColl();
    final String func = COLLECTION.args(NAME);
    indexCheck(func + "//*[text() = '1']");
    indexCheck(func + "//*[text() contains text '2']");
    indexCheck(func + "//a[. = '1']");
    indexCheck(func + "//xml[a = '1']");
    indexCheck(func + "/.[.//text() contains text '1']");
    indexCheck(func + "[.//text() contains text '1']");
    indexCheck("for $s in ('x', '', string-join((1 to 513) ! 'a'))"
        + "return " + func + "//*[text() = $s]", "");
  }

  /** Checks the XQuery db:open() function. */
  @Test public void dbOpenTest() {
    createColl();
    final String func = _DB_OPEN.args(NAME);
    indexCheck(func + "//*[text() = '1']");
    indexCheck(func + "//*[text() contains text '2']");
    indexCheck(func + "//a[. = '1']");
    indexCheck(func + "//xml[a = '1']");
    indexCheck("for $s in ('x', '', string-join((1 to 513) ! 'a'))"
        + "return " + func + "//*[text() = $s]", "");
  }

  /** Checks the XQuery db:open() function, using a specific path. */
  @Test public void dbOpenExtTest() {
    createColl();
    final String func = _DB_OPEN.args(NAME, "two");
    indexCheck(func + "//*[text() = '1']", "");
    indexCheck(func + "//*[text() contains text '2']", "");
    indexCheck(func + "//a[. = '1']", "");
    indexCheck(func + "//xml[a = '1']", "");
    indexCheck(func + "//*[text() = '4']", "<a>4</a>");
    indexCheck("for $s in ('x', '', string-join((1 to 513) ! 'a'))"
        + "return " + func + "//*[text() = $s]", "");
  }

  /** Checks token requests. */
  @Test public void tokenTest() {
    createDoc();
    execute(new Open(NAME));
    indexCheck("data(//*[tokenize(@idref) = 'id1'])", 1);
    indexCheck("data(//*[tokenize(@idref, '\\s+') = 'id1'])", 1);
    indexCheck("data(//@*[tokenize(.) = 'id1'])", "id1 id2");
    indexCheck("for $s in ('id2', 'id3') return data(//*[tokenize(@idref) = $s])", 1);
    indexCheck("for $s in ('id2', 'id3') return data(//@*[tokenize(.) = $s])", "id1 id2");

    indexCheck("data(//*[contains-token(@idref, 'id1')])", 1);
    indexCheck("data(//*[contains-token(@idref, '   id1  ')])", 1);
    indexCheck("data(//@*[contains-token(., 'id1')])", "id1 id2");
    indexCheck("for $s in ('id2', 'id3') return data(//*[contains-token(@idref, $s)])", 1);
    indexCheck("for $s in ('id2', 'id3') return data(//@*[contains-token(., $s)])", "id1 id2");
  }

  /** Checks full-text requests. */
  @Test public void ftTest() {
    createDoc();
    execute(new Open(NAME));
    indexCheck("data(//*[text() contains text '1'])", 1);
    indexCheck("data(//*[text() contains text '1 2' any word])", "1\n2 3");
    indexCheck("//*[text() contains text {'2','4'} all]", "");
    indexCheck("//*[text() contains text {'2','3'} all words]", "<a>2 3</a>");
    indexCheck("//*[text() contains text {'2','4'} all words]", "");
  }

  /** Checks if a full-text index with language option is used. */
  @Test public void ftTestLang() {
    set(MainOptions.LANGUAGE, "de");
    createDoc();
    execute(new Open(NAME));
    indexCheck("//text()[. contains text '1']");
    indexCheck("//text()[. contains text '1' using language 'de']");
    indexCheck("//text()[. contains text '1' using language 'German']");
  }

  /** Checks index optimizations inside functions. */
  @Test public void functionInlining() {
    createColl();
    inline(true);

    // document access after inlining
    indexCheck("declare function db:a($d) { collection($d)//text()[. = '1'] }; "
        + "db:a('" + NAME + "')", 1);
    indexCheck("declare function db:b($d, $s) { collection($d)//text()[. = $s] }; "
        + "db:b('" + NAME + "', '1')", 1);

    // text: search term must be string
    final String db = _DB_OPEN.args(NAME);
    indexCheck("declare function db:c() {" + db + "//text()[. = '1'] }; "
        + "db:c()", 1);
    indexCheck("declare function db:d($x as xs:string) {" + db + "//text()[. = $x] }; "
        + "db:d('1')", 1);
    // full-text: search term may have any type
    indexCheck("declare function db:e() {" + db + "//text()[. contains text '1'] }; "
        + "db:e()", 1);
    indexCheck("declare function db:f($x) {" + db + "//text()[. contains text { $x }] }; "
        + "db:f('1')", 1);
  }

  /** Checks index optimizations inside functions. */
  @Test public void gh1553() {
    createColl();

    indexCheck("declare function db:a() { " + _DB_OPEN.args(NAME) + "//a[text() = '1'] }; "
        + "db:a()", "<a>1</a>");
    indexCheck("declare function db:b() { collection('" + NAME + "')//text()[. = '1'] }; "
        + "db:b()", 1);
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

  /** Combined kind tests. */
  @Test public void gh1737() {
    execute(new CreateDB(NAME, "<xml><a y='Y'>A</a><b z='Z'>B</b></xml>"));

    // texts

    indexCheck("//(a|b)[text() = 'A']/name()", "a");
    indexCheck("//*[(a|b)/text() = 'A']/name()", "xml");
    indexCheck("//(a|b)/text()[. = 'A']", "A");

    // attributes
    indexCheck("//*[(@y|@z) = 'Y']/name()", "a");
    indexCheck("//(@y|@z)[. = 'Y']/name()", "y");
  }

  /** Checks if expressions are rewritten for enforced index access. */
  @Test public void pragma() {
    createDoc();
    final String pragma = "(# db:enforceindex #) { ";
    final String db = _DB_OPEN.args(" <x>" + NAME + "</x>");

    indexCheck(pragma + db + "//a[text() = '1']/text() }", 1);
    indexCheck(pragma + db + "//a/text()[. = '1'] }", 1);
    indexCheck(pragma + db + "/*/@*[. = <_/> ] }", "");

    // no index access (root may not yield all documents of a database)
    check(db + "/*/@*[" + pragma + ". = <_/> }]", "", empty(ValueAccess.class));
  }

  /** Optimizations of predicates that are changed by optimizations. */
  @Test public void gh1597() {
    execute(new CreateDB(NAME, "<x>A</x>"));
    indexCheck("let $s := 0 return *[if($s) then () else .//text() = 'A']", "<x>A</x>");
    check("let $s := 1 return *[if($s) then () else .//text() = 'A']", "", empty());
  }

  /** Optimizations of predicates that are changed by optimizations. */
  @Test public void gh1738() {
    execute(new CreateDB(NAME, "<x a='A'/>"));
    check("(# db:enforceindex #) { <_>" + NAME + "</_> ! db:open(.)//*[comment() = 'A'] }", "",
        empty(ValueAccess.class));
  }

  /** Index access, unfiltered document test. */
  @Test public void gh1948() {
    final String doc = "<a b=\"c\"/>";
    execute(new CreateDB(NAME));
    execute(new Add("a/doc.xml", doc));
    execute(new Add("b/doc.xml", doc));
    execute(new Optimize());
    execute(new Close());
    indexCheck("let $db := db:open('" + NAME + "', 'a') return $db/a[@b = 'c']", doc);
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
  private static void indexCheck(final String query) {
    indexCheck(query, null);
  }

  /**
   * Checks if specified query was rewritten for index access, and checks the query result.
   * @param query query to be tested
   * @param expected result or {@code null} for no comparison
   */
  private static void indexCheck(final String query, final Object expected) {
    check(query, expected, exists("*" +
        "[self::" + Util.className(ValueAccess.class) +
        "|self::" + Util.className(FTIndexAccess.class) + ']'));
  }
}
