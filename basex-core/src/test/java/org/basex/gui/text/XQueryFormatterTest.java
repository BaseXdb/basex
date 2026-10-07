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
    format("return $a and\n$b", "return $a and\n  $b");
    // ...but the operands of a logical expression in the first line of a bracket are aligned
    format("not(\n$a and\n$b or\n$c\n)", "not(\n  $a and\n  $b or\n  $c\n)");
    // comparison operators are derived from CmpOp, not listed literally
    format("$a\neq $b", "$a\n  eq $b");
    // a non-clause continuation does not break the alignment of a following clause
    format("let $x := ()\notherwise 0\nreturn $x", "let $x := ()\n  otherwise 0\nreturn $x");
    format("let $entries :=\nlet $sec := ()\notherwise 0\nreturn {}\nreturn $entries",
           "let $entries :=\n  let $sec := ()\n    otherwise 0\n  return {}\nreturn $entries");
    // ...as does a line that begins with an operator symbol, update or a predicate
    format("$a\n=> f()\n=> g()", "$a\n  => f()\n  => g()");
    format("$a\n! f(.)", "$a\n  ! f(.)");
    format("$a\n|| $b", "$a\n  || $b");
    format("$a\nupdate { }", "$a\n  update {}");
    format("$a\n[1]", "$a\n  [1]");
    // a line with comments is indented like the next line of code
    format("return if ($a) { 1 }\n(: c :)\nelse { 2 }",
           "return if ($a) { 1 }\n  (: c :)\n  else { 2 }");
    // conditional branches are aligned with a condition that starts a line
    format("if ($a) { 1 }\nelse if ($b) { 2 }\nelse { 3 }",
           "if ($a) { 1 }\nelse if ($b) { 2 }\nelse { 3 }");
    format("if ($a) then\n1\nelse\n2", "if ($a) then\n  1\nelse\n  2");
    // ...and continue a condition in the middle of a line
    format("let $x := if ($a) then 1\nelse 2\nreturn $x",
           "let $x := if ($a) then 1\n  else 2\nreturn $x");
    format("if ($a)\nthen 1\nelse 2", "if ($a)\n  then 1\n  else 2");
    // an alternative that follows a guard is not indented
    format("if ($a) then 1 else\nlet $b := 2\nreturn $b",
           "if ($a) then 1 else\nlet $b := 2\nreturn $b");
    // ...but a conditional expression that is bound to a variable is no guard
    format("let $h := if ($a) then $b else\nerror()\nreturn $h",
           "let $h := if ($a) then $b else\n  error()\nreturn $h");
  }

  /** Branches of switch expressions. */
  @Test public void branches() {
    format("switch ($a)\ncase 1 return 2\ndefault return 3",
           "switch ($a)\n  case 1 return 2\n  default return 3");
    format("let $x := switch ($a)\ncase 1 return 2\ndefault return 3\nreturn $x",
           "let $x := switch ($a)\n  case 1 return 2\n  default return 3\nreturn $x");
    format("typeswitch ($a)\ncase xs:string return 2\ndefault return 3",
           "typeswitch ($a)\n  case xs:string return 2\n  default return 3");
    // the result of a branch that starts in the next line is indented
    format("switch ($a)\ncase 1\nreturn 2\ncase 2 return\nlet $b := 3\nreturn $b\ndefault return 4",
           "switch ($a)\n  case 1\n    return 2\n  case 2 return\n    let $b := 3\n"
           + "    return $b\n  default return 4");
    // branches in curly braces are indented by the braces
    format("switch ($a) {\ncase 1 return 2\ndefault return 3\n}",
           "switch ($a) {\n  case 1 return 2\n  default return 3\n}");
    // nested switch expressions
    format("switch ($a)\ncase 1 return (\nswitch ($b)\ncase 2 return 3\ndefault return 4\n)\n" +
           "default return 5",
           "switch ($a)\n  case 1 return (\n    switch ($b)\n      case 2 return 3\n" +
           "      default return 4\n  )\n  default return 5");
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
    format("local:function(" + string + ")", "local:function(\n  " + string + "\n)");
    // the arguments of a wrapped expression are placed on separate lines
    format("local:f(" + string + ", 'b')", "local:f(\n  " + string + ",\n  'b'\n)");
    // short expressions are not wrapped
    format("local:f('a')", "local:f('a')");
    format("local:f(\n" + string + ", 'b')", "local:f(\n  " + string + ",\n  'b'\n)");
    // the first expression of a line that is too long is wrapped
    format("declare function local:f($cmd as map(*), $state as map(*), $id as xs:integer) " +
        "as element(event) {\n()\n};",
        "declare function local:f(\n  $cmd as map(*),\n  $state as map(*),\n  $id as xs:integer\n" +
        ") as element(event) {\n  ()\n};");
    // conditions are not wrapped (but their operands are), strings and attribute values are not
    final String a = "'" + "a".repeat(30) + "'", b = "'" + "b".repeat(40) + "'";
    format("if ($a = (" + a + ", " + b + ")) then 1 else 2",
        "if ($a = (\n  " + a + ",\n  " + b + "\n)) then 1 else 2");
    format("`{ $aaaaaaaaaaaaaaaaaaaaaaa } { $bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb } "
        + "{ $cccccccccccc }`",
        "`{ $aaaaaaaaaaaaaaaaaaaaaaa } { $bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb } { $cccccccccccc }`");
    // expressions shorter than a quarter of the margin are not wrapped
    final String c = "$" + "c".repeat(60);
    format("$a ! " + c + " ! $err('short')", "$a ! " + c + " ! $err('short')");
    format("$a ! f(" + c + ") ! $err('short')", "$a ! f(\n  " + c + "\n) ! $err('short')");
    // conditions keep their lines
    format("if ($a and\n$b) then 1 else 2", "if ($a and\n  $b) then 1 else 2");
    format("if (\n$a and $b\n) then 1 else 2", "if (\n  $a and $b\n) then 1 else 2");
    // branches in curly braces: the braces are wrapped
    format("let $n := if ($y < $t) { $y + xs:yearMonthDuration('P1Y') } else { $y + " +
        "xs:dayTimeDuration('P1D') }\nreturn $n",
        "let $n := if ($y < $t) {\n  $y + xs:yearMonthDuration('P1Y')\n} else { $y + " +
        "xs:dayTimeDuration('P1D') }\nreturn $n");
    // inline functions: the body is wrapped, not the parameters
    format("let $show := fn($label, $value) { $label || ': ' || serialize($value, " +
        "{ 'method': 'adaptive' }) }\nreturn $show",
        "let $show := fn($label, $value) {\n  $label || ': ' || serialize($value, " +
        "{ 'method': 'adaptive' })\n}\nreturn $show");
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
    // the conditions of a window clause continue the clause
    format("for tumbling window $w in $r\nstart $s when true()\nend next $n when $n != $s\n" +
        "return $w",
        "for tumbling window $w in $r\n  start $s when true()\n  end next $n when $n != $s\n" +
        "return $w");
    // variable names are no keywords
    format("for $k in $as\nlet $l := $k\nreturn $l", "for $k in $as\nlet $l := $k\nreturn $l");
    // further operands of a clause are indented, and the following clause is still aligned
    format("let $a := {},\n$b := (),\n$c := 1\nreturn $a",
           "let $a := {},\n  $b := (),\n  $c := 1\nreturn $a");
    // the operands of a sequence are not indented
    format("1,\n2", "1,\n2");
  }

  /** Boundary whitespace is indented, all other element content is adopted unchanged. */
  @Test public void markup() {
    format("<a>\n<b>x</b>\n{\n1\n}\n</a>", "<a>\n  <b>x</b>\n  {\n    1\n  }\n</a>");
    // enclosed expressions are not wrapped if their line fits into the margin
    format("<a>\n<b>{ $c }</b>\n" + "<c/>\n".repeat(20) + "</a>",
           "<a>\n  <b>{ $c }</b>\n" + "  <c/>\n".repeat(20) + "</a>");
    // the content of an element is only indented if it starts in the next line
    format("<a>{\n1\n}</a>", "<a>{\n  1\n}</a>");
    // the content of an element in a continued line is indented relative to that line
    format("let $x :=\n<a>\n<b/>\n</a>\nreturn $x",
        "let $x :=\n  <a>\n    <b/>\n  </a>\nreturn $x");
    // text is no boundary whitespace: its indentation is significant
    format("<a>\n  text\n</a>", "<a>\n  text\n</a>");
    format("<a>x\n  <b/>\n</a>", "<a>x\n  <b/>\n</a>");
    // mixed content: the whitespace between the tags is significant as well
    format("<a>\n<b>x</b>\ntext\n{ 1 }\n</a>", "<a>\n<b>x</b>\ntext\n{ 1 }\n</a>");
    // its indentation is the reference for the lines that are nested in it
    format("<a>\ntext\n{\n1\n}\n</a>", "<a>\ntext\n{\n  1\n}\n</a>");
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
    // a FLWOR expression in a single line and names of record fields are no clauses
    format("(\nfor $i in $a return $i,\n$b\n)", "(\n  for $i in $a return $i,\n  $b\n)");
    format("record(\norder as xs:string,\ncount as xs:integer\n)",
           "record(\n  order as xs:string,\n  count as xs:integer\n)");
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
    // the members of an array constructor are enclosed in spaces, predicates are not
    format("[1, [2]]", "[ 1, [ 2 ] ]");
    format("$a[1]", "$a[1]");
    format("$a[ 1 ]", "$a[ 1 ]");
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
