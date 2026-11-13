package org.basex.query.parse;

import static org.basex.core.MainOptions.QueryParserType.*;
import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.scope.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the parser that is driven by the syntax tree ({@link RExQueryParser}): version
 * declarations, the mapping of syntax errors of the generated parser to query errors, and the
 * constructs that are not part of the grammar.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class RExQueryTest {
  /**
   * A long chain of "else if" branches is parsed without recursion, as by the manual parser.
   * @throws Exception exception
   */
  @Test public void elseIfChain() throws Exception {
    final String query = "if(0) {0} else ".repeat(100_000) + "{1}";
    final Context context = new Context();
    try {
      for(final MainOptions.QueryParserType type : MainOptions.QueryParserType.values()) {
        try(QueryContext qc = new QueryContext(context)) {
          qc.queryParser = type;
          assertNotNull(qc.parse(query, null), type.toString());
        }
      }
    } finally {
      context.close();
    }
  }

  /**
   * Full-text tests have "otherwise" expressions as operands, and are operands of comparisons.
   * @throws Exception exception
   */
  @Test public void ftContainsOperators() throws Exception {
    assertEquals("true", run("'x' otherwise 'y' contains text 'x'"));
    assertEquals("true", run("() otherwise 'y' contains text 'y'"));
    assertEquals("false", run("() otherwise 'x' contains text 'y'"));
    assertEquals("true", run("'ab' || 'c' contains text 'abc'"));
    assertEquals("true", run("'a' contains text 'a' = true()"));
    assertEquals("true", run("() otherwise 'a' contains text 'a' = (() otherwise true())"));
    // a full-text test cannot be the operand of "otherwise"
    assertThrows(QueryException.class, () -> run("'x' contains text 'y' otherwise false()"));
  }

  /**
   * The operands of "transform with" and "update" expressions can be pipelines and arrows.
   * @throws Exception exception
   */
  @Test public void transformWithPipeline() throws Exception {
    assertEquals("<a><b/></a>", run("<a/> -> (.) update { insert node <b/> into . }"));
    assertEquals("<a><b/></a>",
        run("<a/> => (fn($n) { $n })() transform with { insert node <b/> into . }"));
    assertEquals("2", run("(<a/> transform with { }, 1) =!> count() => sum()"));
    // the operand of an arrow expression cannot be a "transform with" expression
    assertThrows(QueryException.class, () -> run("<a/> update {} => count()"));
  }

  /** Validate expressions: whitespace and comments are allowed after the type keyword. */
  @Test public void validate() {
    for(final String query : new String[] { "validate { 1 }", "validate lax { 1 }",
        "validate type xs:int { 1 }", "validate  type  xs:int  {  1  }",
        "validate (: c :) type (: c :) xs:int (: c :) { 1 }" }) {
      final QueryException ex = assertThrows(QueryException.class, () -> run(query), query);
      assertSame(QueryError.IMPLVAL, ex.error(), query);
    }
  }

  /**
   * Extension expressions with pragmas, and simple map expressions.
   * @throws Exception exception
   */
  @Test public void extensionAndMap() throws Exception {
    assertEquals("2", run("(# xs:b c #) { 1 + 1 }"));
    assertEquals("3", run("(# local:a #)  (#  local:b  x y  #)  { 3 }"));
    assertEquals("2,4", run("string-join(((# xs:b c #) { (1, 2) ! (. * 2) }) ! string(), ',')"));
    assertEquals("true,0", run("string-join(((1, 2) != (1, 3) ! 1, 0) ! string(), ',')"));
    assertThrows(QueryException.class, () -> run("(# xs:b c #) { }"));
    assertThrows(QueryException.class, () -> run("(# db:unknown x #) { 1 }"));
  }

  /**
   * Path expressions, steps and node tests.
   * @throws Exception exception
   */
  @Test public void paths() throws Exception {
    final String doc = "<a><b id='1'><c/>t</b><b id='2'/></a>";
    assertEquals("<c/>", run(doc + "/b/c"));
    assertEquals("<c/>", run("(" + doc + "/b/c)"));
    assertEquals("<c/>", run(doc + " ! (b[1]/c)"));
    assertEquals("1,2", run("string-join(" + doc + "/b/@id/string(), ',')"));
    assertEquals("<c/>", run(doc + "//c"));
    assertEquals("<c/>", run(doc + "/descendant-or-self::node()/c"));
    assertEquals("2", run("string(" + doc + "/b[last()]/@id)"));
    assertEquals("2", run("count(" + doc + "/b/(c | text()))"));
    assertEquals("2", run("count(" + doc + "/child::(b | c))"));
    assertEquals("2", run("count(" + doc + "/b/@{ 'id' })"));
    assertEquals("<a/>", run("document { <a/> }/a"));
    assertEquals("<a/>", run("document { <a/> }/(/a)"));
    assertEquals("1", run("count(document { <a/> }//a)"));
    assertEquals("<a/>", run("(<a/>, <b/>)/self::a"));
    assertThrows(QueryException.class, () -> run(doc + "/1/b"));
    assertEquals("http://www.w3.org/XML/1998/namespace",
        run("distinct-values(" + doc + "/b/namespace::* ! string())"));
  }

  /**
   * Sequence types, kind tests, name tests and cast targets.
   * @throws Exception exception
   */
  @Test public void types() throws Exception {
    final String doc = "<a xmlns:p='u'><b id='1'><p:c/>t</b><!--x--><?pi y?></a>";
    for(final String type : new String[] { "xs:integer", "xs:integer?", "item()+", "node()*",
        "(xs:integer | xs:string)", "map(xs:string, item()*)", "array(*)",
        "record(a as xs:integer)",
        "enum('a', 'b')", "function(xs:integer) as xs:integer", "fn(*)", "jnode(*)" }) {
      assertNotNull(run("() instance of " + type), type);
    }
    assertEquals("true", run("1 instance of  (: c :) xs:integer (: c :) ?"));
    assertEquals("true", run("() instance of empty-sequence ( )"));
    assertEquals("true", run("map { 'a': 1 } instance of map(*)"));
    assertEquals("true", run("map { 1: 2 } instance of map(xs:integer, xs:integer)"));
    assertEquals("true", run("'a' instance of enum('a', 'b')"));
    assertEquals("true", run("1 instance of (xs:integer | xs:string)"));
    assertEquals("true", run("fn($a) { $a } instance of function(item()*) as item()*"));
    assertEquals("true", run("(<a/>/self::element(a, xs:untyped)) instance of element(a)"));
    assertEquals("1", run("count(" + doc + "/b)"));
    assertEquals("1", run("count(" + doc + "/element(b))"));
    assertEquals("1", run("count(" + doc + "/*:b)"));
    assertEquals("1", run("count(" + doc + "/b/Q{u}*)"));
    assertEquals("1", run("count(" + doc + "/b/Q{u}c)"));
    assertEquals("1", run("count(" + doc + "/b/@id)"));
    assertEquals("1", run("count(" + doc + "/b/@*:id)"));
    assertEquals("1", run("count(" + doc + "/comment())"));
    assertEquals("1", run("count(" + doc + "/processing-instruction(pi))"));
    assertEquals("1", run("count(" + doc + "/processing-instruction('pi'))"));
    assertEquals("1", run("count(document { " + doc + " }/self::document-node(element(a)))"));
    assertEquals("2", run("('1' cast as xs:integer?) + 1"));
    assertEquals("true", run("'1' castable as (xs:integer | xs:string)?"));
    assertEquals("1", run("try { 1 div 0 } catch err:FOAR0001 | *:x { 1 }"));
    assertEquals("2", run("typeswitch (1) case $v as xs:string | xs:integer return $v + 1 "
        + "default return 0"));
    assertEquals("3", run("declare type local:t as (xs:integer | xs:string); "
        + "declare function local:f($a as local:t) as local:t { $a }; local:f(3)"));
    assertEquals("1", run("declare record local:r(a as xs:integer); local:r(1)?a"));
    assertThrows(QueryException.class, () -> run("1 instance of xs:foo"));
    assertThrows(QueryException.class, () -> run("1 instance of record(a, a)"));
    assertThrows(QueryException.class, () -> run("1 instance of map(item(), item())"));
    assertThrows(QueryException.class, () -> run("1 instance of enum()"));
    assertThrows(QueryException.class, () -> run("'1' cast as xs:anyAtomicType"));
    assertThrows(QueryException.class, () -> run("1 instance of element(a, foo:b)"));
    assertThrows(QueryException.class, () -> run("1 instance of schema-element(a)"));
  }

  /**
   * Function, type, record and context value declarations.
   * @throws Exception exception
   */
  @Test public void declarations() throws Exception {
    assertEquals("3", run("declare function local:f($a, $b := 2) as xs:integer { $a + $b }; "
        + "local:f(1)"));
    assertEquals("6", run("declare function local:f($a as xs:integer := 1, $b := 5) { $a + $b }; "
        + "local:f()"));
    assertEquals("1", run("declare %private function local:f() external; 1"));
    assertEquals("1", run("declare function local:f() (: c :) external (: d :) ; 1"));
    assertEquals("true", run("declare type local:t as (xs:integer | xs:string); "
        + "1 instance of local:t"));
    assertEquals("2", run("declare record local:r(a as xs:integer := 1, b := 1); "
        + "local:r()?a + local:r()?b"));
    assertEquals("3", run("declare record local:r(%fixed a := 3, b as xs:integer := 1); "
        + "local:r()?a"));
    assertEquals("5", run("declare context value as xs:integer := 5; ."));
    assertEquals("5", run("declare context item as (xs:integer | xs:string) := 5; ."));
    assertThrows(QueryException.class,
        () -> run("declare function local:f($a := 1, $b) { 1 }; 1"));
    assertThrows(QueryException.class,
        () -> run("declare function local:f($a as xs:foo) { 1 }; 1"));
    assertThrows(QueryException.class, () -> run("declare type local:t as local:t; 1"));
    assertThrows(QueryException.class, () -> run("declare record local:r(a, a); 1"));
    assertThrows(QueryException.class, () -> run("declare record local:r(a := 1, b); 1"));
    assertThrows(QueryException.class, () -> run("declare record local:r(a := $x); 1"));
    assertThrows(QueryException.class, () -> run("declare record local:r('a'); 1"));
  }

  /**
   * Sequence types of bound values are parsed with the entry point of the generated parser.
   * @throws Exception exception
   */
  @Test public void boundTypes() throws Exception {
    final Context context = new Context();
    try {
      for(final MainOptions.QueryParserType type : MainOptions.QueryParserType.values()) {
        for(final String seqType : new String[] { "xs:integer", " xs:integer ", "xs:integer?",
            "xs:integer*", "(: c :) xs:integer", "xs:integer (: c :)", "(xs:integer | xs:string)",
            "xs:integer ?" }) {
          assertEquals("1", bind(context, type, seqType), type + ": " + seqType);
        }
        assertEquals("", bind(context, type, "empty-sequence()"));
        assertThrows(QueryException.class, () -> bind(context, type, "xs:integer()"));
      }
      // unknown types raise query errors
      assertThrows(QueryException.class, () -> bind(context, REX, "xs:unknown"));
      // the generated parser rejects input after the type
      for(final String seqType : new String[] { "xs:integer foo", "xs:integer;", "xs:integer, 1",
          "xs:integer ?*" }) {
        assertThrows(QueryException.class, () -> bind(context, REX, seqType), seqType);
      }
    } finally {
      context.close();
    }
  }

  /**
   * Binds the string {@code 1} with a type to a variable.
   * @param context database context
   * @param parser query parser
   * @param type type
   * @return serialized value
   * @throws Exception exception
   */
  private static String bind(final Context context, final MainOptions.QueryParserType parser,
      final String type) throws Exception {
    try(QueryProcessor qp = new QueryProcessor("declare variable $x external; $x", context)) {
      qp.qc.queryParser = parser;
      return qp.variable("x", "1", type).value().serialize().toString();
    }
  }

  /**
   * Update expressions.
   * @throws Exception exception
   */
  @Test public void updates() throws Exception {
    final String copy = "copy $a := <a><b/><c/></a> modify ";
    assertEquals("<a><b/><c/><x/></a>", run(copy + "insert node <x/> into $a return $a"));
    assertEquals("<a><x/><b/><c/></a>",
        run(copy + "insert node <x/> as first into $a return $a"));
    assertEquals("<a><b/><x/><c/></a>", run(copy + "insert node <x/> after $a/b return $a"));
    assertEquals("<a><x/><b/><c/></a>", run(copy + "insert node <x/> before $a/b return $a"));
    assertEquals("<a><c/></a>", run(copy + "delete node $a/b return $a"));
    assertEquals("<a><z/><c/></a>", run(copy + "rename node $a/b as 'z' return $a"));
    assertEquals("<a><z/><c/></a>", run(copy + "replace node $a/b with <z/> return $a"));
    assertEquals("<a><b>t</b><c/></a>",
        run(copy + "replace value of node $a/b with 't' return $a"));
    assertEquals("<a><x/><c/></a>", run("copy $a := <a><b/><c/></a>, $b := <x/> modify "
        + "replace node $a/b with $b return $a"));
    // the variable is not visible in its own expression
    assertEquals("<x/>", run("let $c := <x/> return copy $c := $c modify () return $c"));
    assertThrows(QueryException.class, () -> run(copy + "insert node <x/> $a return $a"));
    assertThrows(QueryException.class, () -> run(copy + "$a return $a"));
  }

  /**
   * Computed constructors, string constructors and string templates.
   * @throws Exception exception
   */
  @Test public void constructors() throws Exception {
    assertEquals("<e xmlns:n=\"u\" a=\"1\"><!--c--><?p q?>t</e>",
        run("element e { attribute a { 1 }, namespace n { 'u' }, comment { 'c' }, "
        + "processing-instruction p { 'q' }, text { 't' } }"));
    assertEquals("<a:b xmlns:a=\"u\"/>", run("declare namespace a = 'u'; element #a:b {}"));
    assertEquals("<x/>", run("element { 'x' } {}"));
    assertEquals("xmlns:a=\"u\"", run("namespace #a { 'u' }"));
    assertEquals("<?a 1?>", run("processing-instruction #a { 1 }"));
    assertEquals("<x/>", run("document { element x {} }/*"));
    assertEquals("a1b{c}`d", run("`a{1}b{{c}}``d`"));
    assertEquals("x1 2y", run("``[x`{1, 2}`y]``"));
    assertEquals("", run("``[]``"));
    assertThrows(QueryException.class, () -> run("element a:b {}"));
    assertThrows(QueryException.class, () -> run("processing-instruction { 'a b' } {}"));
  }

  /**
   * Direct constructors.
   * @throws Exception exception
   */
  @Test public void directConstructors() throws Exception {
    assertEquals("<a b=\"x1y&amp;{\"/>", run("<a b='x{1}y&amp;{{'/>"));
    assertEquals("<a b=\"1 2\"/>", run("<a b='1\n2'/>"));
    assertEquals("<a>t<b/>&lt;<!--c--><?p q?>x</a>",
        run("<a>t<b/>&lt;<!--c--><?p q?><![CDATA[x]]></a>"));
    assertEquals("<a>{ }</a>", run("<a>{{ }}</a>"));
    assertEquals("<a><b/><c/></a>", run("<a> <b/> <c/> </a>"));
    assertEquals("<a> </a>", run("declare boundary-space preserve; <a> </a>"));
    assertEquals("<a>  </a>", run("<a>&#32;&#32;</a>"));
    assertEquals("<a xmlns:p=\"u\"><p:b/></a>", run("<a xmlns:p='u'><p:b/></a>"));
    assertEquals("<p:a xmlns:p=\"u\" p:b=\"1\"/>", run("<p:a xmlns:p='u' p:b='1'/>"));
    assertEquals("<a>1<b>2</b></a>", run("<a>{1}<b>{2}</b></a>"));
    // the namespaces of a tag apply to the enclosed expressions of its attributes, in any order
    assertEquals("<a xmlns:p=\"u\" b=\"u\"/>", run("<a xmlns:p='u' b='{namespace-uri(<p:c/>)}'/>"));
    assertEquals("<a xmlns:p=\"u\" b=\"u\"/>", run("<a b='{namespace-uri(<p:c/>)}' xmlns:p='u'/>"));
    assertThrows(QueryException.class, () -> run("<a b='{namespace-uri(<p:c/>)}'/>"));
    assertThrows(QueryException.class, () -> run("<a></b>"));
    assertThrows(QueryException.class, () -> run("<a b='1' b='2'/>"));
    assertThrows(QueryException.class, () -> run("<a xmlns:p='{1}'/>"));
    assertThrows(QueryException.class, () -> run("<a>&foo;</a>"));
    assertThrows(QueryException.class, () -> run("<?xml x?>"));
  }

  /**
   * Full-text expressions.
   * @throws Exception exception
   */
  @Test public void fullText() throws Exception {
    final String text = "'The quick brown fox jumps over the lazy dog' contains text ";
    assertEquals("true", run(text + "'fox' ftand 'dog'"));
    assertEquals("false", run(text + "'fox' ftand 'cat'"));
    assertEquals("true", run(text + "'cat' ftor 'dog'"));
    assertEquals("true", run(text + "ftnot 'cat'"));
    assertEquals("true", run(text + "'dog' not in 'lazy dog'  ftor 'quick'"));
    assertEquals("true", run(text + "'quick fox' all words"));
    assertEquals("true", run(text + "{ 'quick', 'fox' } any"));
    assertEquals("true", run(text + "'the' occurs exactly 2 times"));
    assertEquals("true", run(text + "'the' occurs from 1 to 3 times"));
    assertEquals("true", run(text + "'foxes' using stemming"));
    assertEquals("true", run(text + "'FOX' using case insensitive using wildcards"));
    assertEquals("true", run(text + "'f.x' using wildcards"));
    assertEquals("true", run(text + "'fox' ftand 'dog' ordered"));
    assertEquals("false", run(text + "'dog' ftand 'fox' ordered"));
    assertEquals("true", run(text + "'fox' ftand 'dog' window 8 words"));
    assertEquals("false", run(text + "'fox' ftand 'dog' window 2 words"));
    assertEquals("true", run(text + "'fox' ftand 'dog' distance at most 5 words"));
    assertEquals("true", run(text + "'fox' ftand 'dog' same sentence"));
    assertEquals("true", run(text + "'The' at start"));
    assertEquals("true", run(text + "('quick' ftand 'fox') weight { 2 }"));
    assertEquals("true", run(text + "(# db:casesens true #) { 'fox' }"));
    assertEquals("true", run("declare ft-option using case sensitive; 'Fox' contains text 'Fox'"));
    // integers with separators
    assertEquals("true", run("'fox' contains text 'cat' using fuzzy 1_0 errors"));
    assertEquals("false", run("'fox' contains text 'cat' using fuzzy 1 errors"));
    assertThrows(QueryException.class, () -> run(text + "'fox' using stemming using stemming"));
    assertThrows(QueryException.class, () -> run(text + "'fox' using wildcards using fuzzy"));
    assertThrows(QueryException.class, () -> run(text + "'fox' using language 'xx'"));
    assertThrows(QueryException.class, () -> run(text + "'fox' ftand ftnot 'a' not in 'b'"));
  }

  /**
   * The grammar is ambiguous if a keyword such as {@code div} is used as the name of a computed
   * constructor, or if {@code updating} is followed by two parenthesized expressions. The generated
   * parser picks one alternative, which must be the one of the manual parser.
   */
  @Test public void ambiguities() {
    assertEquals("<?div x?>", outcome("processing-instruction div { 'x' }", MANUAL));
    assertEquals("xmlns:div=\"u\"", outcome("namespace div { 'u' }", MANUAL));
    assertEquals("<div>1</div>", outcome("element div { 1 }", MANUAL));
    assertEquals("div=\"1\"", outcome("attribute div { 1 }", MANUAL));
    for(final String query : new String[] { "processing-instruction div { 'x' }",
        "namespace div { 'u' }", "element div { 1 }", "attribute div { 1 }",
        "updating ('a') ('b')", "invoke updating ('a') ('b')" }) {
      assertEquals(outcome(query, MANUAL), outcome(query, REX), query);
    }
  }

  /**
   * Postfix expressions, lookups, constructors and updating function calls.
   * @throws Exception exception
   */
  @Test public void postfix() throws Exception {
    assertEquals("2", run("(1, 2, 3)[. > 1][1]"));
    assertEquals("5", run("let $m := map { 'a': [4, 5] } return $m ? a ? 2"));
    assertEquals("1,2", run("string-join(map { 'a': 1, 'b': 2 }?*, ',')"));
    assertEquals("6", run("let $o := map { 'm': fn($s, $x) { $x * 2 } } return $o =?> m(3)"));
    assertEquals("3", run("(fn() { fn() { 3 } })()()"));
    assertEquals("2", run("array { 1, 2 }?2"));
    assertEquals("1", run("count(`a{1}b`)"));
    assertEquals("true", run("(1, 2) ! ?. = (1, 2) or true()"));
    assertThrows(QueryException.class, () -> run("map { 1: 2, 1: 3 }"));
    assertThrows(QueryException.class, () -> run("updating abs#1(1)"));
  }

  /**
   * A long chain of postfix operations is parsed without recursion, as by the manual parser.
   * @throws Exception exception
   */
  @Test public void postfixChain() throws Exception {
    final String query = "declare variable $f external; $f" + "[1]".repeat(100_000);
    final Context context = new Context();
    try {
      for(final MainOptions.QueryParserType type : MainOptions.QueryParserType.values()) {
        try(QueryContext qc = new QueryContext(context)) {
          qc.queryParser = type;
          assertNotNull(qc.parse(query, null), type.toString());
        }
      }
    } finally {
      context.close();
    }
  }

  /**
   * Version declarations.
   * @throws Exception exception
   */
  @Test public void versionDecl() throws Exception {
    final String[] queries = {
      "xquery version \"4.0\"; 1",
      "xquery version '3.1' encoding 'UTF-8'; 1",
      "xquery encoding 'utf-8'; 1",
      "xquery version \"4.00\"; 1",
      "  xquery  (: c :)  version  (: d :)  '4.0'  (: e :)  ;  (: f :)  1",
      "(: before :) xquery version '4.0'; 1",
      "xquery version '4.0';1"
    };
    for(final String query : queries) assertEquals("1", run(query), query);
    // without declaration
    assertEquals("<xquery/>", run("<a><xquery/></a>/xquery"));
    assertEquals("2", run("let $xquery := 2 return $xquery"));
  }

  /**
   * XQDoc comments in a version declaration document the module, wherever they are.
   * @throws Exception exception
   */
  @Test public void versionDeclDoc() throws Exception {
    final String[] queries = {
      "xquery (:~module doc:) version '4.0'; 1",
      "xquery version (:~module doc:) '4.0'; 1",
      "xquery version '4.0' (:~module doc:) ; 1",
      "(:~module doc:) xquery version '4.0'; 1",
      "xquery version '4.0'; (:~module doc:) 1",
      "xquery version '4.0' encoding 'UTF-8' (:~module doc:) ; declare function local:f() { 1 }; 1",
      "xquery (:~module doc:) version '4.0'; module namespace m = 'm'; declare variable $m:x := 1;",
      "xquery version '4.0' (:~module doc:) ; module namespace m = 'm'; " +
          "declare variable $m:x := 1;",
      "(:~module doc:) module namespace m = 'm'; declare variable $m:x := 1;"
    };
    for(final String query : queries) {
      final TokenObjectMap<TokenList> doc = doc(query);
      assertNotNull(doc, query);
      final TokenList description = doc.get(token("description"));
      assertEquals(1, description.size(), query);
      assertEquals("module doc", string(description.get(0)), query);
    }
    // a comment in front of a declaration documents the declaration
    assertNull(doc("xquery version '4.0'; (:~doc:) declare variable $x := 1; $x"));
    // whitespace and comments elsewhere in a version declaration are no problem
    assertNull(doc("xquery (: a :) version (: b :) '4.0' (: c :) ; (: d :) 1"));
  }

  /**
   * Comments inside a declaration do not document the declaration that follows.
   * @throws Exception exception
   */
  @Test public void prologDoc() throws Exception {
    final String[] declarations = {
      "declare boundary-space (:~internal:) preserve",
      "declare boundary-space preserve (:~internal:)",
      "declare construction (:~internal:) strip",
      "declare ordering (:~internal:) ordered",
      "declare ordering ordered (:~internal:)",
      "declare copy-namespaces (:~internal:) no-preserve, (:~internal:) inherit",
      "declare default order (:~internal:) empty greatest",
      "declare revalidation (:~internal:) skip",
      "declare base-uri (:~internal:) 'http://b/'",
      "declare namespace (:~internal:) p (:~internal:) = (:~internal:) 'u'",
      "declare default element namespace (:~internal:) 'u'",
      "declare decimal-format (:~internal:) local:d decimal-separator (:~internal:) = '.'",
      "declare decimal-format local:d decimal-separator = '.' (:~internal:)",
      "declare option (:~internal:) local:o (:~internal:) 'v'"
    };
    for(final String declaration : declarations) {
      final String query = "(:~module:) " + declaration + "; declare variable $x := 1; $x";
      assertNull(varDoc(query, "x"), query);
    }
    // a comment in front of a declaration documents the declaration
    assertEquals("doc", varDoc("declare ordering ordered; (:~doc:) declare variable $x := 1; $x",
        "x"));
  }

  /**
   * The documentation of external variables includes the comments inside the declaration.
   * @throws Exception exception
   */
  @Test public void externalVariableDoc() throws Exception {
    // the first comment documents the variable, unless a comment inside the declaration is
    // followed by something else than a declaration: this comment is the module documentation,
    // and the first one is cleared
    for(final String query : new String[] {
      "(:~module:) declare variable $v (:~c:) external; $v",
      "(:~module:) declare variable $v external (:~c:); $v",
      "(:~module:) declare variable $v external := 1 (:~c:); $v"
    }) {
      assertEquals("c", string(doc(query).get(token("description")).get(0)), query);
      assertNull(varDoc(query, "v"), query);
    }
    // a context value declaration is not documented
    final String context = "(:~module:) declare context value external (:~c:); 1";
    assertEquals("module", string(doc(context).get(token("description")).get(0)), context);
  }

  /** Errors of version declarations. */
  @Test public void versionErrors() {
    error("xquery version '9.9'; 1", QueryError.XQUERYVER_X, 1, 21);
    error("xquery version '9.9' encoding 'UTF-8'; 1", QueryError.XQUERYVER_X, 1, 21);
    error("xquery encoding 'invalid-encoding'; 1", QueryError.XQUERYENC2_X, 1, 35);
    error("\n xquery version '9.9'; 1", QueryError.XQUERYVER_X, 2, 22);
    // library modules are processed in the same way
    error("xquery version '9.9'; module namespace m = 'm'; declare variable $m:x := 1;",
        QueryError.XQUERYVER_X, 1, 21);
  }

  /** Checks that are not part of the grammar are still performed by the traditional parser. */
  @Test public void semanticErrors() {
    error("<a></b>", QueryError.TAGWRONG_X_X, 1, 8);
    error("module namespace m = 'm'; declare variable $m:x := 1;", QueryError.MAINMOD, 1, 8);
  }

  /** Syntax errors are mapped to XPST0003, positioned at the offending token. */
  @Test public void syntaxErrors() {
    error("1 +", QueryError.INCOMPLETE, 1, 4);
    error("1 + , 2", QueryError.SYNTAX_X, 1, 5);
    error("(1", QueryError.WRONGCHAR_X_X, 1, 3);
    error("<a>", QueryError.INCOMPLETE, 1, 4);
    error("1\n  +\n  )", QueryError.SYNTAX_X, 3, 3);
    error("f(1,", QueryError.INCOMPLETE, 1, 5);
    // Elvis operator: key specifier expected
    error("1 ?: 2", QueryError.KEYSPEC_X, 1, 4);
    error("map { } ? :a", QueryError.KEYSPEC_X, 1, 11);
    // a character that cannot start a token
    error("1 + §", QueryError.SYNTAX_X, 1, 5);
    // non-BMP characters count once
    error("\"😀\" + , 2", QueryError.SYNTAX_X, 1, 7);
    // syntax errors are reported before semantic errors, even if these occur earlier in the query
    error("$p:x + , 2", QueryError.SYNTAX_X, 1, 8);
  }

  /** The error code is XPST0003 for all syntax errors. */
  @Test public void errorCode() {
    for(final String query : new String[] { "1 +", "(1", "<a>", "1 2", "for $x return 1" }) {
      final QueryException ex = assertThrows(QueryException.class, () -> run(query), query);
      assertEquals("XPST0003", ex.error().toString(), query);
    }
  }

  /**
   * Creates a context that uses the query parser that is driven by the syntax tree.
   * @return context
   */
  private static Context rexContext() {
    final Context context = new Context();
    context.options.set(MainOptions.QUERYPARSER, MainOptions.QueryParserType.REX);
    return context;
  }

  /**
   * Parses a query and returns the documentation of a variable.
   * @param query query
   * @param name variable name
   * @return description (can be {@code null})
   * @throws QueryException query exception
   */
  private static String varDoc(final String query, final String name) throws QueryException {
    final Context context = rexContext();
    try(QueryContext qc = new QueryContext(context)) {
      qc.parse(query, null);
      for(final StaticVar var : qc.vars) {
        if(string(var.name.local()).equals(name)) {
          final TokenObjectMap<TokenList> doc = var.doc();
          return doc == null ? null : string(doc.get(token("description")).get(0));
        }
      }
      throw new AssertionError("Variable not found: " + name);
    } finally {
      context.close();
    }
  }

  /**
   * Parses a query and returns the documentation of the module.
   * @param query query
   * @return documentation (can be {@code null})
   * @throws QueryException query exception
   */
  private static TokenObjectMap<TokenList> doc(final String query) throws QueryException {
    final Context context = rexContext();
    try(QueryContext qc = new QueryContext(context)) {
      final AModule module = qc.parse(query, null);
      return module.doc();
    } finally {
      context.close();
    }
  }

  /**
   * Checks that a query raises an error at the given position.
   * @param query query
   * @param error expected error
   * @param line expected line
   * @param column expected column
   */
  private static void error(final String query, final QueryError error, final int line,
      final int column) {
    final QueryException ex = assertThrows(QueryException.class, () -> run(query), query);
    assertEquals(error, ex.error(), query + ": " + ex.getMessage());
    assertEquals(line, ex.line(), query + ": " + ex.getMessage());
    assertEquals(column, ex.column(), query + ": " + ex.getMessage());
  }

  /**
   * Evaluates a query with the specified parser.
   * @param query query
   * @param type query parser
   * @return serialized result, or error code
   */
  private static String outcome(final String query, final MainOptions.QueryParserType type) {
    final Context context = new Context();
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      qp.qc.queryParser = type;
      return qp.value().serialize().toString();
    } catch(final QueryException ex) {
      return ex.error().toString();
    } catch(final IOException ex) {
      throw new AssertionError(ex);
    } finally {
      context.close();
    }
  }

  /**
   * Evaluates a query.
   * @param query query
   * @return serialized result
   * @throws QueryException query exception
   */
  private static String run(final String query) throws QueryException {
    final Context context = rexContext();
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      return qp.value().serialize().toString();
    } catch(final IOException ex) {
      throw new AssertionError(ex);
    } finally {
      context.close();
    }
  }
}
