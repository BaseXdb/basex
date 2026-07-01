package org.basex.query.index;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.index.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.index.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests if queries are rewritten for index access.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IndexOptimizeTest extends SandboxTest {
  /** Creates a test database. */
  @BeforeAll public static void start() {
    execute(new DropDB(NAME));
    set(MainOptions.FTINDEX, true);
    set(MainOptions.TOKENINDEX, true);
    set(MainOptions.QUERYINFO, true);
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

  /** Checks the XQuery db:get() function. */
  @Test public void dbGetTest() {
    createColl();
    final String func = _DB_GET.args(NAME);
    indexCheck(func + "//*[text() = '1']");
    indexCheck(func + "//*[text() contains text '2']");
    indexCheck(func + "//a[. = '1']");
    indexCheck(func + "//xml[a = '1']");
    indexCheck("for $s in ('x', '', string-join((1 to 513) ! 'a'))"
        + "return " + func + "//*[text() = $s]", "");
  }

  /** Checks the XQuery db:get() function, using a specific path. */
  @Test public void dbGetExtTest() {
    createColl();
    final String func = _DB_GET.args(NAME, "two");
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
    indexCheck("for $s in ('   id1  ') return data(//*[contains-token(@idref, $s)])", 1);
  }

  /** Checks full-text requests. */
  @Test public void ftTest() {
    createDoc();
    execute(new Open(NAME));
    indexCheck("data(//*[text() contains text '1'])", 1);
    indexCheck("data(//*[text() contains text '1 2' any word])", "1\n2 3");
    indexCheck("//*[text() contains text { '2', '4' } all]", "");
    indexCheck("//*[text() contains text { '2', '3' } all words]", "<a>2 3</a>");
    indexCheck("//*[text() contains text { '2', '4' } all words]", "");
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
    final String db = _DB_GET.args(NAME);
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

    indexCheck("declare function db:a() { " + _DB_GET.args(NAME) + "//a[text() = '1'] }; "
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

        final String test = exists(ValueAccess.class) + " = " + include.equals("a") + "()";
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

    indexCheck("//(a | b)[text() = 'A']/name()", "a");
    indexCheck("//*[(a | b)/text() = 'A']/name()", "xml");
    indexCheck("//(a | b)/text()[. = 'A']", "A");

    // attributes
    indexCheck("//*[(@y | @z) = 'Y']/name()", "a");
    indexCheck("//(@y | @z)[. = 'Y']/name()", "y");
  }

  /** Checks if expressions are rewritten for enforced index access. */
  @Test public void pragma() {
    createDoc();
    final String pragma = "(# db:enforceindex #) { ";
    final String db = _DB_GET.args(wrap(NAME));

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
    check("(# db:enforceindex #) { "
        + "<_>" + NAME + "</_> ! " + _DB_GET.args(" .") + "//*[comment() = 'A'] }", "",
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
    indexCheck("let $db :=" + _DB_GET.args(NAME, "a") + " return $db/a[@b = 'c']", doc);
  }

  /** Index Rewritings: Mixture of text and attribute comparisons. */
  @Test public void gh2211() {
    final String first = "<a>A</a>";
    final String second = "<b b=\"A\"/>";
    execute(new CreateDB(NAME, "<x>" + first + second + "</x>"));
    query("//*[text() = 'A' or @b = 'A']", first + '\n' + second);
    query("//*[@b = 'A' or text() = 'A']", first + '\n' + second);
  }

  /** Bug on contains-token() with token index. */
  @Test public void gh2222() {
    final String xml = "<M v=\"a\">a</M>";
    execute(new CreateDB(NAME, xml));
    execute(new CreateIndex(IndexType.TOKEN));
    query("//M/descendant-or-self::M[@v = 'a']", xml);
    query("//M/descendant-or-self::M[text() = 'a']", xml);
    query("//M/descendant-or-self::M[contains-token(@v, 'a')]", xml);

    query("/M/descendant-or-self::M[@v = 'a']", xml);
    query("/M/descendant-or-self::M[text() = 'a']", xml);
    query("/M/descendant-or-self::M[contains-token(@v, 'a')]", xml);

    query("M/descendant-or-self::M[@v = 'a']", xml);
    query("M/descendant-or-self::M[text() = 'a']", xml);
    query("M/descendant-or-self::M[contains-token(@v, 'a')]", xml);
  }

  /** ENFORCEINDEX, collections. */
  @Test public void gh2442() {
    execute(new CreateDB(NAME));
    execute(new Add("1.xml", "<x>X</x>"));
    execute(new Add("2.xml", "<x>X</x>"));
    execute(new Optimize());
    query("head(db:get('" + NAME + "'))//*[text() = 'X']", "<x>X</x>");
    query("(# db:enforceindex #) { head(db:get('" + NAME + "'))//*[text() = 'X'] }", "<x>X</x>");
  }

  /**
   * Index access with namespaces (#1763).
   */
  @Test public void gh1763() {
    final String doc = "<a xmlns:x=\"X\"><b>B</b></a>";
    execute(new CreateDB(NAME, doc));

    // explicit text steps: rewritten for index access
    check("/a[.//text() = 'B']", doc, exists(ValueAccess.class));
    check("//a[.//text() = 'B']", doc, exists(ValueAccess.class));
    check("/a/b[text() = 'B']/..", doc, exists(ValueAccess.class));
    check("//b[text() = 'B']/..", doc, exists(ValueAccess.class));
    check("//*[text() = 'B']/..", doc, exists(ValueAccess.class));

    // no default namespace is declared anywhere: no-namespace element tests are rewritten
    check("/a[b = 'B']", doc, exists(ValueAccess.class));
    check("//a[b = 'B']", doc, exists(ValueAccess.class));
    check("/*[b = 'B']", doc, exists(ValueAccess.class));
    check("//*[b = 'B']", doc, exists(ValueAccess.class));

    // local-name wildcard tests are not rewritten (may match names in other namespaces)
    check("/a[*:b = 'B']", doc, empty(ValueAccess.class));
    check("//a[*:b = 'B']", doc, empty(ValueAccess.class));
    check("/*:a[*:b = 'B']", doc, empty(ValueAccess.class));
    check("//*:a[*:b = 'B']", doc, empty(ValueAccess.class));
    check("/*[*:b = 'B']", doc, empty(ValueAccess.class));
    check("//*[*:b = 'B']", doc, empty(ValueAccess.class));

    // nonexistent no-namespace names: statically discarded
    check("/a/nonexistent", "", empty());
    check("//nonexistent[. = 'B']", "", empty());

    // attribute values are rewritten for index access
    execute(new CreateDB(NAME, "<a xmlns:x=\"X\"><b id=\"7\">B</b></a>"));
    check("//b[@id = '7']", "<b xmlns:x=\"X\" id=\"7\">B</b>", exists(ValueAccess.class));
  }

  /**
   * Index access with namespaces: soundness with prefixed same-local names (#1763).
   */
  @Test public void gh1763Mixed() {
    // no-namespace <b> (leaf) and prefixed <x:b> (non-leaf) share the local name 'b'
    execute(new CreateDB(NAME, "<a xmlns:x=\"X\"><b>B</b><x:b><c>B</c></x:b></a>"));

    // full no-namespace test is rewritten and matches only the no-namespace element
    check("count(/a[b = 'B'])", "1", exists(ValueAccess.class));
    check("count(//b[. = 'B'])", "1", exists(ValueAccess.class));
    // local-name wildcard is evaluated sequentially and matches both elements
    check("count(//*:b[. = 'B'])", "2", empty(ValueAccess.class));

    // 'B' only occurs below the prefixed element: no-namespace test must not match
    execute(new CreateDB(NAME, "<a xmlns:x=\"X\"><b>b</b><x:b><c>B</c></x:b></a>"));
    check("count(/a[b = 'B'])", "0", exists(ValueAccess.class));
  }

  /**
   * Index access on databases with a default namespace (#1763).
   */
  @Test public void gh1763DefaultNs() {
    // single default namespace: a no-namespace element test never matches and is discarded
    execute(new CreateDB(NAME, "<a xmlns=\"Y\"><b>B</b></a>"));
    check("/a[b = 'B']", "", empty());
    // query default namespace matches the database default namespace: correct result
    check("declare default element namespace 'Y'; count(/a[b = 'B'])", "1");
  }

  /** Index access for integer comparisons (issue #2069). */
  @Test public void gh2069() {
    // attribute and text values are integers: rewrite equality comparisons for index access
    execute(new CreateDB(NAME, "<x><a n='10'>100</a><a n='20'>200</a><a n='30'>300</a></x>"));
    indexCheck("//a[@n = 20]", "<a n=\"20\">200</a>");
    indexCheck("//a[text() = 200]", "<a n=\"20\">200</a>");
    indexCheck("//a[@n = (10, 30)]", "<a n=\"10\">100</a>\n<a n=\"30\">300</a>");

    // mixed integer/double values: no integer rewrite (statistics report double)
    execute(new CreateDB(NAME, "<x><a n='10.5'/><a n='20'/></x>"));
    check("//a[@n = 20]", "<a n=\"20\"/>", empty(ValueAccess.class));

    // non-canonical integer lexical form: '005' parses as xs:integer 5,
    // so XQuery should return the element; the index lookup of token "5", however,
    // does not match "005" → demonstrates a correctness gap of the integer rewrite
    execute(new CreateDB(NAME, "<x><a n='005'/></x>"));
    check("//a[@n = 5]", "<a n=\"005\"/>", exists(ValueAccess.class));

    // namespace declarations on the database: statistics are not consulted, no integer rewrite
    execute(new CreateDB(NAME, "<x xmlns:ns='u'><a n='5'/></x>"));
    check("count(//a[@n = 5])", "1", empty(ValueAccess.class));

    // wildcard attribute test: no NameTest name available, no integer rewrite
    execute(new CreateDB(NAME, "<x><a n='5'/></x>"));
    check("count(//*[@* = 5])", "1", empty(ValueAccess.class));

    // access closed database
    execute(new Close());
    check("db:get('" + NAME + "')//a[@n = 5]", "<a n=\"5\"/>", exists(ValueAccess.class));

    // integer value not present in the indexed lexical forms: statically known to be empty
    execute(new CreateDB(NAME, "<x><a n='10'/><a n='20'/></x>"));
    check("//a[@n = 999]", "", empty());

    // sequence in which no item matches: same code path, must not throw
    check("//a[@n = (997, 999)]", "", empty());

    // integer range expressions are rewritten via CmpIR + value index
    execute(new CreateDB(NAME, "<x><a n='10'/><a n='20'/><a n='30'/></x>"));
    // `to` range with matches in [min..max]
    check("//a[@n = 10 to 25]", "<a n=\"10\"/>\n<a n=\"20\"/>", exists(ValueAccess.class));
    // ascending sequence (step 1) collapses to a RangeSeq → CmpIR; no matches → empty
    check("//a[@n = (998, 999)]", "", empty());

    // CmpR on untyped operand: integer-category statistics allow value-index rewriting
    check("//a[@n >= 25]", "<a n=\"30\"/>", exists(ValueAccess.class));
    check("//a[@n > 25 and @n < 35]", "<a n=\"30\"/>", exists(ValueAccess.class));
    check("//a[@n >= 100]", "", empty());

    // edge case: stats become DOUBLE (INF parses as Double.POSITIVE_INFINITY) → no integer
    // rewriting, falls back to sequential evaluation, semantics preserved
    execute(new CreateDB(NAME, "<x><a n='INF'/></x>"));
    check("//a[@n >= 25]", "<a n=\"INF\"/>", empty(ValueAccess.class));

    // edge case: stats become STRING (NaN parses as Double.NaN → fallback STRING) → no
    // integer rewriting; NaN is never >= 25 in XQuery semantics
    execute(new CreateDB(NAME, "<x><a n='NaN'/></x>"));
    check("//a[@n >= 25]", "", empty(ValueAccess.class));
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
