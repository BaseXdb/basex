package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.util.*;
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
    final TextEditor editor = new TextEditor(null);
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

  /**
   * Compares the colors that are assigned to a query with the expected legend.
   * @param query query string
   * @param expected expected legend ({@code .}: plain, {@code K}: keyword, {@code V}: variable,
   *   {@code N}: number, {@code C}: comment, {@code S}: string)
   */
  private static void check(final String query, final String expected) {
    final TextEditor editor = new TextEditor(null);
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
