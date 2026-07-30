package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.util.*;

import org.basex.gui.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the XQuery syntax highlighter ({@link SyntaxXQuery}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXQueryTest {
  /** Default color. */
  private static final Color PLAIN = new Color(1, 0, 0);

  /** Assigns distinctive highlighting colors. */
  @BeforeAll public static void beforeAll() {
    GUIConstants.blue = new Color(2, 0, 0);
    GUIConstants.green = new Color(3, 0, 0);
    GUIConstants.purple = new Color(4, 0, 0);
    GUIConstants.cyan = new Color(5, 0, 0);
    GUIConstants.brown = new Color(6, 0, 0);
  }

  /** Reserved words, built-in functions and user-defined names. */
  @Test public void names() {
    // hyphenated names and QNames are highlighted as a whole
    check("analyze-string('')", "KKKKKKKKKKKKKK.SS.");
    check("string:ngrams(1)", "KKKKKKKKKKKKK.N.");
    check("xs:integer", "KKKKKKKKKK");
    // a dollar sign marks a variable if a name follows, possibly after whitespace ('$ x')
    check("$x", "VV");
    check("$ x", "V.V");
    // a bare dollar sign is no variable marker
    check("$+", "..");
    // user-defined names are no keywords
    check("local:get-value()", ".................");
    check("for $x in 1", "KKK.VV.KK.N");
    check("let $x := 1", "KKK.VV....N");
    check("child::text()", "KKKKK..KKKK..");
    check("declare %updating function f() {}", "KKKKKKK.KKKKKKKKK.KKKKKKKK.......");
  }

  /** Path steps: names of built-in functions and reserved words are no keywords. */
  @Test public void steps() {
    // 'id', 'name' and 'count' are built-in functions, but here they are name tests
    check("@id", "...");
    check("//name", "......");
    check("$x/count", "VV......");
    // 'text' and 'item' are reserved words, but here they are name tests
    check("$x/text", "VV.....");
    check("$x/@item", "VV......");
    // map lookups and name tests of an axis step are no keywords either
    check("$m?key", "VV....");
    check("child::text", "KKKKK......");
    // function calls, node tests and axes are still highlighted
    check("count(1)", "KKKKK.N.");
    check("$x/text()", "VV.KKKK..");
    // the axis is highlighted, the name test is not
    check("$x/child::name", "VV.KKKKK......");
    check("count#1", "KKKKK.N");
    check("child::text()", "KKKKK..KKKK..");
    // a colon that does not belong to an axis must not suppress a keyword
    check("map { 'a': for $x in 1 }", "KKK...SSS..KKK.VV.KK.N..");
  }

  /** Numeric literals. */
  @Test public void numbers() {
    check("1.5 + 1e3", "NNN...NNN");
    check(".5", "NN");
    check("a.b", "...");
    // digit separators, exponents and hexadecimal digits belong to the literal
    check("10_000", "NNNNNN");
    check("1_000.000_1", "NNNNNNNNNNN");
    check("1.5e10", "NNNNNN");
    check("0xFF_FF", "NNNNNNN");
  }

  /** Comments. */
  @Test public void comments() {
    check("(: a :)1", "CCCCCCCN");
    // comments nest
    check("(: a (: b :) c :)1", "CCCCCCCCCCCCCCCCCN");
    // a comment is not closed by the colon of its own opening delimiter
    check("(:)1", "CCCC");
    check("(#p#)1", "CCCCCN");
  }

  /** EQNames: the URI must not be parsed as code. */
  @Test public void eqNames() {
    check("Q{http://x.com}local(1)", "SSSSSSSSSSSSSSS......N.");
    // an apostrophe in the URI must not open a string literal
    check("Q{a'b}c 1", "SSSSSS..N");
    // a character or entity reference in the URI is highlighted, as in string literals
    check("Q{&amp;}c", "SSNNNNNS.");
  }

  /** EQNames are resolved via their braced URI; a lexical prefix is ignored. */
  @Test public void eqNameKeywords() {
    final String xs = "Q{http://www.w3.org/2001/XMLSchema}";
    final String fn = "Q{http://www.w3.org/2005/xpath-functions}";
    final String uri = "Q{http://x.com}";
    // all four EQName forms of a type denote xs:integer
    check("xs:integer", "KKKKKKKKKK");
    check(xs + "integer", "S".repeat(xs.length()) + "KKKKKKK");
    check(xs + "xs:integer", "S".repeat(xs.length()) + "KKKKKKKKKK");
    // a built-in function, addressed by its namespace
    check(fn + "count(1)", "S".repeat(fn.length()) + "KKKKK.N.");
    // the local name of a user-defined namespace is no keyword, even if a built-in shares it
    check(uri + "count(1)", "S".repeat(uri.length()) + "......N.");
    check(uri + "f(1)", "S".repeat(uri.length()) + "..N.");
  }

  /** String literals. */
  @Test public void strings() {
    // keywords are not highlighted in strings
    check("'for'", "SSSSS");
    // doubled quotes are escaped
    check("\"a\"\"b\"1", "SSSSSSN");
    check("'a'1", "SSSN");
  }

  /** Direct constructors. */
  @Test public void constructors() {
    check("<p>Index of x</p>", "KKK..........KKKK");
    // quotes in element content do not open a string literal
    check("<a>don't</a>", "KKK.....KKKK");
    check("<p a=\"1\">x</p>", "KK.NKSSSK.KKKK");
    check("<a>{1}</a>", "KKK.N.KKKK");
    check("<a b=\"{$x}\"/>", "KK.NKS.VV.SKK");
    check("<a>{{}}</a>", "KKK....KKKK");
    check("<a><!-- c --></a>", "KKKCCCCCCCCCCKKKK");
    check("<a><![CDATA[<]]></a>", "KKKCCCCCCCCCCCCCKKKK");
    check("<a/>1", "KKKKN");
  }

  /** Deeply nested constructors: the mode stack grows on demand. */
  @Test public void nesting() {
    final int levels = 100;
    final StringBuilder query = new StringBuilder(), expected = new StringBuilder();
    for(int l = 0; l < levels; l++) {
      query.append("<a>");
      expected.append("KKK");
    }
    // the innermost content must still be recognized as element content, not as code
    query.append("for");
    expected.append("...");
    for(int l = 0; l < levels; l++) {
      query.append("</a>");
      expected.append("KKKK");
    }
    check(query.toString(), expected.toString());
  }

  /** Angle brackets: comparison operator or direct constructor. */
  @Test public void angleBrackets() {
    check("$a<$b", "VV.VV");
    // a name that ends an operand is followed by a comparison
    check("$a<b", "VV..");
    // a keyword is followed by an expression
    check("return <a/>", "KKKKKK.KKKK");
    check("1 < 2", "N...N");
  }

  /** Only brackets in code are paired (see {@link TextRenderer}). */
  @Test public void brackets() {
    // the closing bracket of the string must not be paired with the opening one
    brackets("(1, \")\")", "B......B");
    brackets("'('", "...");
    // the bracket in the comment is ignored; the comment delimiters pair with each other
    brackets("(: ( :)1", "B.....B.");
    // brackets in element content are literal text
    brackets("<a>(x)</a>", "..........");
    // brackets of an enclosed expression are code
    brackets("<a>{1}</a>", "...B.B....");
  }

  /**
   * Compares the brackets of a query that are recognized as code with the expected legend.
   * @param query query string
   * @param expected expected legend ({@code B}: bracket in code)
   */
  private static void brackets(final String query, final String expected) {
    final TextEditor editor = new TextEditor(EditorOptions.DEFAULTS);
    editor.text(Token.token(query));
    final TextIterator iter = new TextIterator(editor);
    final Syntax syntax = new SyntaxXQuery();
    syntax.init(PLAIN);

    final StringBuilder sb = new StringBuilder();
    while(iter.moreStrings(1000)) {
      syntax.getColor(iter);
      final boolean code = syntax.codeBefore() || syntax.codeAfter();
      for(int p = iter.pos(); p < iter.posEnd(); p++) {
        sb.append(code && "()[]{}".indexOf(query.charAt(p)) != -1 ? 'B' : '.');
      }
    }
    assertEquals(expected, sb.toString(), query);
  }

  /** Function and variable declarations. */
  @Test public void declarations() {
    declarations("declare function local:f() { 1 };", "local:f 1");
    declarations("declare variable $x := 1;", "$x 1");
    declarations("declare variable $local:X external;", "$local:X 1");
    // annotations and their arguments are skipped, including numeric ones
    declarations("declare %private function f() {};", "f 1");
    declarations("declare %rest:GET %rest:query-param('p', '{$p}', 1) function f($p) {};", "f 1");
    declarations("declare updating function f() {};", "f 1");
    // other prolog declarations are skipped
    declarations("declare namespace a = 'b'; declare option db:chop 'true';");
    declarations("declare default function namespace 'a';");
    declarations("declare context item as function(*) external;");
    // declarations in strings and comments are ignored
    declarations("'declare function f()'");
    declarations("(: declare function f() :)");
    // the line of each declaration is returned
    declarations("declare function a() {};\ndeclare\n  variable $b := 1;", "a 1", "$b 3");
  }

  /** Code completion snippets. */
  @Test public void snippets() {
    // main modules: no prefix
    snippet("", "declare function _() {};");
    snippet("declare variable $x := 1;", "declare function _() {};");
    snippet("import module namespace m = 'uri';", "declare function _() {};");
    // library modules: prefix of the module declaration
    snippet("module namespace m = 'uri';", "declare function m:_() {};");
    snippet("module namespace m='uri'; declare function m:f() {};",
      "declare function m:_() {};");
    // version declarations and comments are skipped
    snippet("xquery version '4.0'; module namespace m = 'uri';",
      "declare function m:_() {};");
    snippet("(: module namespace x = 'uri' :) module namespace m = 'uri';",
      "declare function m:_() {};");
  }

  /** Code completion for annotations. */
  @Test public void annotations() {
    // annotations of the XQuery namespace are proposed without prefix
    completion("%_", "public", "public");
    completion("declare %_", "updating", "updating");
    // the cursor is placed inside the parentheses of an annotation with arguments
    completion("declare %_", "rest:path", "rest:path(_)");
    completion("declare %_", "unit:test", "unit:test(_)");
    // annotations are only proposed after a percent sign
    completion("declare _", "rest:path", null);
    // other candidates are skipped after a percent sign
    completion("%_", "count", null);
  }

  /** Code completion for the keywords of a prolog declaration. */
  @Test public void prolog() {
    // the syntax of the declaration is supplied
    completion("declare _", "function", "function _() {};");
    completion("declare _", "namespace", "namespace _ = '';");
    completion("declare _", "base-uri", "base-uri '_';");
    completion("module namespace m = 'uri'; declare _", "function", "function m:_() {};");
    // the default keyword introduces further declarations
    completion("declare _", "default", "default collation '_';");
    completion("declare default _", "element", "element namespace '_';");
    completion("declare default _", "order", "order empty _;");
    completion("declare default _", "function", "function namespace '_';");
    // no candidates of the enclosing context
    completion("declare default _", "count", null);
    completion("declare default _", "variable", null);
    // the updating keyword and annotations are skipped
    completion("declare updating _", "function", "function _() {};");
    completion("declare %private _", "function", "function _() {};");
    completion("declare %rest:path('a;b') _", "function", "function _() {};");
    // snippets, functions and types are no candidates
    completion("declare _", "for", null);
    completion("declare _", "count", null);
    completion("declare _", "xs:string", null);
    // the name of a declaration is a new one
    completion("declare function _", "count", null);
    completion("declare variable _", "count", null);
    // the body of a function is no declaration
    completion("declare function f() { _ };", "count", "count(_)");
  }

  /** Code completion for the start of a prolog declaration. */
  @Test public void declare() {
    completion("_", "declare", "declare ");
    completion("xquery version '4.0'; _", "declare", "declare ");
    completion("declare variable $x := 1; _", "declare", "declare ");
    completion("declare function f() { 1 }; _", "declare", "declare ");
    // declarations are proposed with the keyword that introduces them
    completion("_", "contextvalue", "declare context value := _;");
    completion("_", "base-uri", "declare base-uri '_';");
    completion("_", "module", "module namespace _ = '';");
    // a declaration cannot follow an expression
    completion("let $a := 1 return _", "declare", null);
    completion("1, _", "contextvalue", null);
    completion("1, _", "module", null);
    // expression snippets are proposed in expressions
    completion("1, _", "for", "for $_ in \nreturn");
    completion("declare function f() { _ };", "declare", null);
    completion("declare _", "declare", null);
    completion("'a', _", "declare", null);
    // semicolons in strings and comments start no declaration
    completion("let $a := 'x;y' return 1, _", "declare", null);
    completion("let $a := 1 (: ; :) return 1, _", "declare", null);
  }

  /** Code completion for sequence types. */
  @Test public void types() {
    completion("declare variable $x as _", "xs:string", "xs:string");
    completion("1 instance of _", "element()", "element()");
    completion("1 cast as _", "xs:integer", "xs:integer");
    completion("for $x as _", "map", "map(_)");
    // no other candidates
    completion("declare variable $x as _", "count", null);
    completion("1 instance of _", "for", null);
    // a variable is no keyword
    completion("let $as := 1 return $as _", "count", "count(_)");
  }

  /** Code completion for lookups. */
  @Test public void lookups() {
    completion("$map?key, $map?_", "key", "key");
    // lookups are collected in the whole module
    completion("declare function f() { $map?key }; $m?_", "key", "key");
    // no other candidates
    completion("$map?_", "count", null);
    // lookups in strings and comments are ignored
    completion("'?key', $map?_", "key", null);
    completion("(: $map?key :) $map?_", "key", null);
  }

  /** Code completion in the tags of element constructors. */
  @Test public void tags() {
    // the angle bracket is part of the completed element name
    completion("<title/>, _<t", "<title", "<title");
    completion("<xhtml:div/>, _<x", "<xhtml:div", "<xhtml:div");
    // attribute names are completed after the element name
    completion("<a id='x'/>, <b _", "id", "id");
    // element names are no attribute names, and vice versa
    completion("<a id='x'/>, <b _", "<a", null);
    completion("<a id='x'/>, _<a", "id", null);
    // no other candidates in a tag
    completion("<a/>, _<a", "count", null);
    completion("<a id='x'/>, <b _", "count", null);
    // an angle bracket that starts no constructor is a comparison
    completion("<a/>, 1 _<a", "<a", null);
    // names in comments are ignored
    completion("<b/>, (: <a/> :) 1, _<b", "<a", null);
    completion("<b/>, (: <a/> :) 1, _<b", "<b", "<b");
  }

  /** Code completion for end tags. */
  @Test public void endTags() {
    completion("<a></_", "a", "a");
    completion("<a><b></_", "b", "b");
    completion("<a><b/></_", "a", "a");
    completion("<a><b></b></_", "a", "a");
    // no element is open
    completion("<a></a></_", "a", null);
    // no other candidates
    completion("<a></_", "count", null);
  }

  /** Code completion for documentation tags. */
  @Test public void docTags() {
    // a tag is followed by its description
    completion("(:~\n : @_", "param", "param ");
    completion("(:~\n : @_", "author", "author ");
    completion("(:~\n : @_", "return", "return ");
    // tags are also proposed in ordinary comments
    completion("(: @_ :) 1", "param", "param ");
    // no other candidates in comments
    completion("(:~\n : @_", "count", null);
    completion("(:~\n : _", "author", null);
    completion("(: _ :) 1", "count", null);
    // outside comments, no tags are proposed
    completion("@_", "author", null);
    // tags are proposed in lexicographic order
    assertEquals("author", proposals("(:~\n : @_").get(0));
  }

  /** Code completion for variables in scope. */
  @Test public void variables() {
    // global variables are in scope in the whole module
    completion("declare variable $g := 1; _", "$g", "$g");
    completion("declare variable $g := 1; declare function f() { _ };", "$g", "$g");
    // parameters and local variables of the current declaration are in scope
    completion("declare function f($p) { _ };", "$p", "$p");
    completion("declare function f() { let $a := 1 return _ };", "$a", "$a");
    // local variables of other declarations are out of scope
    completion("declare function f() { let $a := 1 return $a }; _", "$a", null);
    completion("declare function f() { let $a := 1 return $a };\n" +
      "declare function g() { _ };", "$a", null);
    // variables that are declared after the completed string are out of scope
    completion("_ let $a := 1 return $a", "$a", null);
    // semicolons in strings and comments end no declaration
    completion("let $a := 'x;y' return _", "$a", "$a");
    completion("let $a := 1 (: ; :) return _", "$a", "$a");
  }

  /** Candidates that are already typed are not proposed. */
  @Test public void typed() {
    assertFalse(proposed("declare variable $x := 1; _declare", "declare"));
    assertTrue(proposed("declare variable $x := 1; _decl", "declare"));
    // the only element name is the one that is being typed
    assertFalse(proposed("<a/>, _<a", "<a"));
    assertTrue(proposed("<abc/>, _<a", "<abc"));
    // annotations
    assertFalse(proposed("declare %_public", "public"));
    assertTrue(proposed("declare %_publi", "public"));
  }

  /** Full names are proposed before abbreviated ones. */
  @Test public void ranking() {
    final StringList proposals = proposals("declare %_pu");
    assertEquals("public", proposals.get(0));
    assertTrue(proposals.contains("rest:put"));
  }

  /**
   * Checks if a candidate is proposed for the completed string, which ends with the query.
   * @param query query string, in which an underscore marks the completed string
   * @param match name to be matched
   * @return result of check
   */
  private static boolean proposed(final String query, final String match) {
    return proposals(query).contains(match);
  }

  /**
   * Returns the candidates that are proposed for the completed string, which ends with the query.
   * @param query query string, in which an underscore marks the completed string
   * @return matched names, ordered by relevance
   */
  private static StringList proposals(final String query) {
    final int pos = query.indexOf('_');
    final StringList names = new StringList();
    for(final Completion completion :
        Completions.candidates(query.substring(pos + 1), lists(query))) {
      if(completion != null) names.add(completion.match());
    }
    return names;
  }

  /**
   * Returns the candidate lists for the completed string of a query.
   * @param query query string, in which an underscore marks the completed string
   * @return candidates, ordered by relevance
   */
  private static ArrayList<ArrayList<Completion>> lists(final String query) {
    final Syntax syntax = new SyntaxXQuery();
    syntax.init(PLAIN);

    final int pos = query.indexOf('_');
    return syntax.completions(Token.token(query.substring(0, pos) + query.substring(pos + 1)), pos);
  }

  /**
   * Compares the function snippet of a query with the expected one.
   * @param query query string
   * @param expected expected snippet
   */
  private static void snippet(final String query, final String expected) {
    completion(query + '_', "function", expected);
  }

  /**
   * Compares the completion value of a query with the expected one.
   * @param query query string, in which an underscore marks the completed string
   * @param match name to be matched
   * @param expected expected value (can be {@code null})
   */
  private static void completion(final String query, final String match, final String expected) {
    String value = null;
    for(final ArrayList<Completion> list : lists(query)) {
      for(final Completion completion : list) {
        if(value == null && match.equals(completion.match())) value = completion.value();
      }
    }
    assertEquals(expected, value, query);
  }

  /**
   * Compares the declarations of a query with the expected ones.
   * @param query query string
   * @param expected expected declarations (name, followed by the line)
   */
  private static void declarations(final String query, final String... expected) {
    final Syntax syntax = new SyntaxXQuery();
    syntax.init(PLAIN);

    final StringList names = new StringList();
    for(final Declaration declaration : syntax.declarations(Token.token(query))) {
      names.add(declaration.name() + ' ' + declaration.line());
    }
    assertArrayEquals(expected, names.finish(), query);
  }

  /**
   * Compares the colors that are assigned to a query with the expected legend.
   * @param query query string
   * @param expected expected legend ({@code .}: plain, {@code K}: keyword, {@code V}: variable,
   *   {@code N}: number, {@code C}: comment, {@code S}: string)
   */
  private static void check(final String query, final String expected) {
    final TextEditor editor = new TextEditor(EditorOptions.DEFAULTS);
    editor.text(Token.token(query));
    final TextIterator iter = new TextIterator(editor);
    final Syntax syntax = new SyntaxXQuery();
    syntax.init(PLAIN);

    final StringBuilder sb = new StringBuilder();
    while(iter.moreStrings(1000)) {
      final char color = legend(syntax.getColor(iter));
      for(int p = iter.pos(); p < iter.posEnd(); p++) sb.append(color);
    }
    assertEquals(expected, sb.toString(), query);
  }

  /**
   * Returns the legend character for a color.
   * @param color color
   * @return character
   */
  private static char legend(final Color color) {
    if(color.equals(GUIConstants.blue)) return 'K';
    if(color.equals(GUIConstants.green)) return 'V';
    if(color.equals(GUIConstants.purple)) return 'N';
    if(color.equals(GUIConstants.cyan)) return 'C';
    if(color.equals(GUIConstants.brown)) return 'S';
    return '.';
  }
}
