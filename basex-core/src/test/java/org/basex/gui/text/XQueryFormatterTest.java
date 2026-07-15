package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the code {@link Formatter}, driven by the XQuery syntax.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQueryFormatterTest {
  /** Indentation of a single level. */
  private static final byte[] SPACES = Token.token("  ");
  /** Line margin. */
  private static final int MARGIN = 80;

  /** Lines are indented by the nesting depth of their brackets. */
  @Test public void indent() {
    format("declare function local:f() {\nfor $i in 1 to 3\nreturn $i\n};",
           "declare function local:f() {\n  for $i in 1 to 3\n  return $i\n};");
    // wrong indentation is corrected
    format("if(true()) {\n        1\n}", "if(true()) {\n  1\n}");
    format("[\n1,\n[\n2\n]\n]", "[\n  1,\n  [\n    2\n  ]\n]");
    // the bracket of an expression that spans several lines starts a new line
    format("local:f(\n1)", "local:f(\n  1\n)");
    // code without line breaks is left alone
    format("count($x)", "count($x)");
    format("(1, 2)", "(1, 2)");
  }

  /** Lines that continue an expression are indented by one more level. */
  @Test public void continued() {
    format("let $x :=\n1\nreturn $x", "let $x :=\n  1\nreturn $x");
    format("1 +\n2", "1 +\n  2");
    format("if($x) then\n1\nelse\n2", "if($x) then\n  1\nelse\n  2");
    // an expression that is complete is not continued
    format("let $x := 1\nreturn $x", "let $x := 1\nreturn $x");
    format("let $x := <a/>\nreturn $x", "let $x := <a/>\nreturn $x");
    // reserved words that do not expect an operand
    format("for $x in (1, 2)\norder by $x descending\nreturn $x",
           "for $x in (1, 2)\norder by $x descending\nreturn $x");
    // a line that begins with a binary operator continues the previous one
    format("()\notherwise 0", "()\n  otherwise 0");
    format("$a\nand $b", "$a\n  and $b");
    // comparison operators are derived from CmpOp, not listed literally
    format("$a\neq $b", "$a\n  eq $b");
    // a non-clause continuation does not break the alignment of a following clause
    format("let $x := ()\notherwise 0\nreturn $x", "let $x := ()\n  otherwise 0\nreturn $x");
    format("let $entries :=\nlet $sec := ()\notherwise 0\nreturn {}\nreturn $entries",
           "let $entries :=\n  let $sec := ()\n  otherwise 0\n  return {}\nreturn $entries");
  }

  /** Only brackets in code are indented. */
  @Test public void code() {
    // brackets in element content are literal text: indenting them changes the result
    format("<a>(x)</a>", "<a>(x)</a>");
    format("<a>\n  text\n</a>", "<a>\n  text\n</a>");
    // brackets in strings, comments and string constructors are no code either
    format("'('", "'('");
    format("(: ( :)", "(: ( :)");
    format("``[ ( ]``", "``[ ( ]``");
    // enclosed expressions are code, even if they occur in constructors
    format("<a>{\nfor $i in 1 to 3\nreturn $i\n}</a>", "<a>{\n" +
        "  for $i in 1 to 3\n  return $i\n}</a>");
    format("<a b=\"{\n1\n}\"/>", "<a b=\"{\n  1\n}\"/>");
  }

  /** Expressions that exceed the line margin are wrapped. */
  @Test public void wrap() {
    final String string = "'" + "a".repeat(80) + "'";
    format("local:f(" + string + ")", "local:f(\n  " + string + "\n)");
    // the arguments of a wrapped expression are placed on separate lines
    format("local:f(" + string + ", 'b')", "local:f(\n  " + string + ",\n  'b'\n)");
    // short expressions are not wrapped
    format("local:f('a')", "local:f('a')");
    format("local:f(\n" + string + ", 'b')", "local:f(\n  " + string + ",\n  'b'\n)");
    // without margin, no expression is wrapped
    final String query = "local:f(" + string + ")";
    assertEquals(query,
      Token.string(new SyntaxXQuery().format(Token.token(query), SPACES, 0)), query);
  }

  /** Line breaks in nested expressions do not break the enclosing one. */
  @Test public void nested() {
    // the body of a function argument is broken, the argument list is not
    format("local:f($a, fn() {\n1\n})", "local:f($a, fn() {\n  1\n})");
    // a tag is nested as well: its line breaks do not break the argument list
    format("local:f($a, <b x='1'\ny='2'/>)", "local:f($a, <b x='1'\n  y='2'/>)");
    // a line break before the closing bracket separates no operands
    format("local:f($a, $b\n)", "local:f($a, $b\n)");
  }

  /** Clauses of a FLWOR expression are indented alike. */
  @Test public void clauses() {
    format("let $x :=\nfor $i in 1 to 3\nreturn $i\nreturn $x",
           "let $x :=\n  for $i in 1 to 3\n  return $i\nreturn $x");
    // an expression that is opened in a continued line adopts its indentation
    format("let $x :=\nfor $i in 1 to 3\nreturn {\n'a': 1\n}\nreturn $x",
           "let $x :=\n  for $i in 1 to 3\n  return {\n    'a': 1\n  }\nreturn $x");
    // annotations continue a declaration
    format("declare\n%updating\nfunction local:f() {\n()\n};",
           "declare\n  %updating\nfunction local:f() {\n  ()\n};");
    // an empty line ends a continuation
    format("if ($x) then 1 else\n\n2", "if ($x) then 1 else\n\n2");
    // further operands of a clause are indented, and the following clause is still aligned
    format("let $a := {},\n$b := (),\n$c := 1\nreturn $a",
           "let $a := {},\n  $b := (),\n  $c := 1\nreturn $a");
    // the operands of a sequence are not indented
    format("1,\n2", "1,\n2");
  }

  /** Boundary whitespace is indented, all other element content is adopted unchanged. */
  @Test public void markup() {
    format("<a>\n<b>x</b>\n{\n1\n}\n</a>", "<a>\n  <b>x</b>\n  {\n    1\n  }\n</a>");
    // the content of an element is only indented if it starts in the next line
    format("<a>{\n1\n}</a>", "<a>{\n  1\n}</a>");
    // text is no boundary whitespace: its indentation is significant
    format("<a>\n  text\n</a>", "<a>\n  text\n</a>");
    format("<a>x\n  <b/>\n</a>", "<a>x\n  <b/>\n</a>");
    // escaped curly braces are literal text, not enclosed expressions
    format("<a>\n{{ }}\n</a>", "<a>\n{{ }}\n</a>");
    // preserved boundary whitespace is significant
    format("declare boundary-space preserve;\n<a>\n    <b/>\n</a>",
           "declare boundary-space preserve;\n<a>\n    <b/>\n</a>");
    // the declaration is also found if it is not the first occurrence of the keyword
    format("(: boundary-space :)\ndeclare boundary-space preserve;\n<a>\n    <b/>\n</a>",
           "(: boundary-space :)\ndeclare boundary-space preserve;\n<a>\n    <b/>\n</a>");
    // the attributes of a tag are indented, and their whitespace is collapsed (as in XML)
    format("<a\nb=\"1\"\nc=\"2\"/>", "<a\n  b=\"1\"\n  c=\"2\"/>");
    format("<a   b=\"1\"/>", "<a b=\"1\"/>");
    // the content of an element is no continuation of the attributes of its tag
    format("<a b='1'\nc='2'>{\n'x'\n}</a>", "<a b='1'\n  c='2'>{\n  'x'\n}</a>");
    format("<a b='1'\nc='2'>\n{ 'x' }\n</a>", "<a b='1'\n  c='2'>\n  { 'x' }\n</a>");
  }

  /** If a list is broken, all its operands are placed on separate lines. */
  @Test public void lists() {
    format("local:f($a,\n$b, $c)", "local:f(\n  $a,\n  $b,\n  $c\n)");
    format("[1,\n2, 3]", "[\n  1,\n  2,\n  3\n]");
    // lists that fit into a line are left alone, even in a broken expression
    format("local:f(\n(1, 2), $b)", "local:f(\n  (1, 2),\n  $b\n)");
    // curly braces enclose no lists: their commas may separate let clauses
    format("declare function local:f() {\nlet $a := 1, $b := 2\nreturn $a\n};",
           "declare function local:f() {\n  let $a := 1, $b := 2\n  return $a\n};");
    // the commas of a clause separate its own operands, not those of the enclosing list
    format("(\nlet $a := 1, $b := 2\norder by $a, $b\nreturn $a\n)",
           "(\n  let $a := 1, $b := 2\n  order by $a, $b\n  return $a\n)");
  }

  /** Whitespace is collapsed, commas are followed by a single space. */
  @Test public void spaces() {
    format("let $a   := 1\nreturn $a", "let $a := 1\nreturn $a");
    format("local:f(1 ,2)", "local:f(1, 2)");
    // empty brackets are collapsed
    format("local:f( )", "local:f()");
    format("declare function local:f() {  };", "declare function local:f() {};");
    format("[ ]", "[]");
    // colons are no separators
    format("map { 'a': 1 }", "map { 'a': 1 }");
    format("$x/child::node()", "$x/child::node()");
    // whitespace in strings, comments and element content is untouched
    format("'a  b'", "'a  b'");
    format("(:  c  :)", "(:  c  :)");
    format("<a>x  y</a>", "<a>x  y</a>");
  }

  /** Empty lines and trailing whitespace. */
  @Test public void whitespace() {
    format("1   \n2", "1\n2");
    // a single empty line is retained
    format("1\n\n\n2", "1\n\n2");
    // an indentation of the first line is adopted by all lines (selected text is formatted)
    format("  (\n1\n)", "  (\n    1\n  )");
  }

  /** Brackets without counterpart. */
  @Test public void unbalanced() {
    format("{", "{");
    format("}", "}");
    format("(1", "(1");
  }

  /**
   * Compares a formatted query with the expected result, and checks that formatting is idempotent.
   * @param query query string
   * @param expected expected result
   */
  private static void format(final String query, final String expected) {
    final Syntax syntax = new SyntaxXQuery();
    final byte[] formatted = syntax.format(Token.token(query), SPACES, MARGIN);
    assertEquals(expected, Token.string(formatted), query);
    assertEquals(expected, Token.string(syntax.format(formatted, SPACES, MARGIN)),
      "reformatted: " + query);
  }
}
