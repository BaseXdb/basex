package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.map.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.Test;

/**
 * XQuery 4.0 tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQuery4Test extends SandboxTest {
  /** Version declaration. */
  @Test public void version40() {
    query("xquery version '1.0'; ()", "");
    query("xquery version '3.0'; ()", "");
    query("xquery version '3.1'; ()", "");
    query("xquery version '4.0'; ()", "");
    error("xquery version '0.0'; ()", XQUERYVER_X);
  }

  /** Context value declaration. */
  @Test public void contextValue() {
    query("declare context value := 1; .", 1);
    query("declare context value := (1, 2); .", "1\n2");

    query("declare context value as item()* := (1, 2); .", "1\n2");
    query("declare context value as xs:integer* := (1, 2); .", "1\n2");

    error("declare context value as xs:integer := (1, 2); .", INVTYPE_X);
    error("declare context value := 1; declare context value := 2; .", DUPLVALUE);
    error("declare context value := 1; declare context item := 2; .", DUPLVALUE);
    error("declare context item := 1; declare context item := 2; .", DUPLVALUE);
    error("declare context item := 1; declare context item := 2; .", DUPLVALUE);
  }

  /** Lookup operator. */
  @Test public void lookup() {
    query("{} ? ''", "");
    query("{ '': 1 } ? ''", 1);

    query("let $m := { '': 1 } return $m?''", 1);
    query("let $_ := '' return { '': 1 } ? $_", 1);
    query("let $_ := '' let $m := { '': 1 } return $m?$_", 1);

    query("declare variable $_ := 1; [ 9 ]?$_", 9);
  }

  /** Method call. */
  @Test public void methodCall() {
    final String rect = "{ 'height': 3, 'width': 4, 'area': fn { ?height × ?width } }";
    query(rect + " =?> area()", 12);
    query(rect + "?area instance of fn() as item()*", false);
    query(rect + "('area') instance of fn(item()*) as item()*", true);

    query("declare record local:rect(height, width, area := fn { ?height × ?width }); "
        + "let $r := local:rect(3, 4) return local:rect(5, 6, $r?area) =?> area()", 30);
    query("{ 'self': fn { . } } =?> self() => map:keys()", "self");
    query("let $f := fn {trace(., \"context\")?i}\n"
        + "let $g := ({ 'i': 7, 'f': $f }, { 'i': 11, 'g': $f })\n"
        + "let $h := $g?('f', 'g')\n"
        + "return $h[1]($g[1]) * $h[2]($g[2])", 77);

    error("(" + rect + "=> map:get('area'))()", INVARITY_X_X);
    error(rect + "('area')()", INVARITY_X_X);
  }

  /** Otherwise expression. */
  @Test public void otherwise() {
    query("() otherwise ()", "");
    query("() otherwise 2", 2);
    query("1 otherwise ()", 1);
    query("1 otherwise 2", 1);
    query("1 otherwise 2 otherwise 3", 1);

    query("(1 to 10)[. = 0] otherwise (1 to 10)[. = 0]", "");
    query("(1 to 10)[. = 0] otherwise (1 to 10)[. = 2]", 2);
    query("(1 to 10)[. = 1] otherwise (1 to 10)[. = 0]", 1);
    query("(1 to 10)[. = 1] otherwise (1 to 10)[. = 2]", 1);

    query("(1 to 10)[. = 0] otherwise (1 to 10)[. = 0]", "");

    check("1 otherwise void(2)", 1, root(Itr.class));
    check("void(1) otherwise 2", 2, root(Otherwise.class));
    check("void(1) otherwise void(2)", "", root(Otherwise.class));

    check("count(void(1) otherwise void(2))", 0, root(COUNT));
    query("count((1 to 10)[. = 0] otherwise (1 to 10)[. = 2])", 1);
    query("count((1 to 10)[. = 1] otherwise (1 to 10)[. = 2])", 1);

    query("(1 to 10)[. = 0] otherwise (1 to 6) ! string()", "1\n2\n3\n4\n5\n6");
    query("count((1 to 10)[. = 0] otherwise (1 to 6) ! string())", 6);
    query("count((1 to 10)[. = 0] otherwise sort((1 to 6)[. = 3]) otherwise 1)", 1);

    query("1 otherwise 2", 1);
    query("<x/> otherwise 2", "<x/>");
    query("(1 to 2)[. = 1] otherwise 2", 1);
    // test if second branch will be evaluated
    query("(1 to 2)[. != 0] otherwise (1 to 1000000000000)[. != 0]", "1\n2");

    query("() otherwise 2", 2);
    query("() otherwise <x/>", "<x/>");
    query("(1 to 2)[. = 0] otherwise <x/>", "<x/>");

    query("tokenize(<a/>) otherwise 2", 2);
    query("tokenize(<a>1</a>) otherwise 2", 1);
    query("sort(tokenize(<a>1</a>) otherwise 2)", 1);
    query("sort(tokenize(<a/>) otherwise 2)", 2);

    query("count(<_>1</_>[. = 1] otherwise 2)", 1);
    query("count((1, 2)[. = 1] otherwise 3)", 1);
    query("count((1, 2, 3)[. = 1] otherwise 4)", 1);
    query("count((1, 2, 3)[. = 4] otherwise 4)", 1);
    query("count((1, 2, 3)[. = 4] otherwise (4, 5))", 2);

    check("() otherwise ()", "", empty());
    check("() otherwise 1", 1, root(Itr.class));
    check("1 otherwise ()", 1, root(Itr.class));
    check("() otherwise <x/>", "<x/>", root(CElem.class));
    check("<x/> otherwise ()", "<x/>", root(CElem.class));

    check("(1, <_>2</_>[. = 3]) otherwise ()", 1, root(List.class));
    check("(2, <_>3</_>[. = 4]) otherwise <z/>", 2, root(List.class));

    check("(3, <_>4</_>)[. = 3] otherwise ()", 3, root(IterFilter.class));
    check("(4, <_>5</_>)[. = 4] otherwise <z/>", 4, root(Otherwise.class));

    check("void(1) otherwise 2", 2, root(Otherwise.class));
    check("void(2) otherwise void(3)", "", root(Otherwise.class));

    check("<_>6</_>[. = 6] otherwise 7", "<_>6</_>", root(Otherwise.class));
  }

  /** Function items (fn syntax). */
  @Test public void fn() {
    query("fn() {}()", "");
    query("fn($a) { $a }(())", "");
    query("fn($a) { $a }(1)", 1);
    query("fn($a) { $a }(1 to 2)", "1\n2");
    query("fn($a, $b) { $a + $b }(1, 2)", 3);
    query("sum((1 to 6) ! fn($a) { $a * $a }(.))", 91);
    query("sum(for $i in 1 to 6  return fn($a) { $a * $a }($i))", 91);

    query("- fn() { 1 }()", -1);
    query("--fn() { 1 }()", 1);
    query("1-fn() { 2 }()", -1);
    query("1--fn() { 2 }()", 3);
  }

  /** GFLWOR: for key/value. */
  @Test public void forKeyValue() {
    query("for key $k value $v in {} return $k * $v", "");

    query("for key $k in { 2: 3 } return $k", 2);
    query("for value $v in { 2: 3 } return $v", 3);
    query("for key $k value $v in { 2: 3 } return $k * $v", 6);

    error("for key $k allowing empty in 1 return ()", WRONGCHAR_X_X);
    error("for value $v allowing empty in 1 return ()", WRONGCHAR_X_X);
    error("for key $k value $v allowing empty in 1 return ()", WRONGCHAR_X_X);
  }

  /** GFLWOR: for member. */
  @Test public void forMember() {
    query("for member $m in [] return $m", "");
    query("for member $m in [ 5 ] return $m", 5);
    query("for member $m in [ 5, 6 ] return $m", "5\n6");

    query("for member $m at $p in [ 3, 4 ] return $m", "3\n4");
    query("for member $m at $p in [ (3, 2), 1, () ] return count($m)", "2\n1\n0");
    query("for member $m at $p in [ 3, 4 ] return $p", "1\n2");
    query("for member $m at $p in [ (3, 2), 1, () ] return count($p)", "1\n1\n1");

    check("for member $m in [ (3, 2), 1, () ] return sum($m)", "5\n1\n0", empty(_ARRAY_ITEMS));
    check("for member $m in [ (3, 2), 1, () ] return count($m)", "2\n1\n0", exists(_ARRAY_ITEMS));

    error("for member $m allowing empty in 1 return $m", WRONGCHAR_X_X);
  }

  /** Function item, no parameter list. */
  @Test public void functionContextItem() {
    query("function { . + 1 }(1)", 2);
    query("function { . + 1 }(1)", 2);
    query("function { . + . }(1)", 2);
    query("sum((1 to 6) ! function { . * . }(.))", 91);
    query("sum(for $i in 1 to 6  return function { . * . }($i))", 91);

    query("- function{ . }(1)", -1);
    query("--function{ . }(1)", 1);

    query("() =!> (function {})()", "");
    query("1 =!> (function {})()", "");
    query("() =!> (function { . })()", "");
    query("1 =!> (function { . })()", 1);
    query("(1, 2) =!> (function { . })()", "1\n2");
    query("0 =!> (function { 1, 2 })()", "1\n2");
    query("(0 to 5) =!> (function { . + 1 })()", "1\n2\n3\n4\n5\n6");

    query("2 > 3 => (function { 1 })()", true);
    query("2 > 3 =!> (function { 1 })()", true);

    query("function { . }(())", "");
    query("function { . }(1 to 2)", "1\n2");

    error("function() { . + $i }", VARUNDEF_X);
    error("function { . + $i }", VARUNDEF_X);
  }

  /** Generalized arrow operator. */
  @Test public void arrow() {
    query("'x' => {}()", "");
    query("'x' => { 'x': 8 }()", 8);
    query("1 => [ 8 ]()", 8);
    query("8 => fn { . }()", 8);
    query("8 => fn($n) { $n }()", 8);
    query("declare variable $v := 42 => f(); declare function f($x) {$x}; $v", 42);
  }

  /** Mapping arrow operator. */
  @Test public void mappingArrow() {
    query("'abc' =!> upper-case() =!> tokenize('\\s+')", "ABC");
    query("(1, 4, 9, 16, 25, 36) =!> math:sqrt() =!> (function{ . + 1 })() => sum()", 27);

    query("('$' =!> concat(?))('x')", "$x");
    query("'$' =!> concat('x')", "$x");

    final String eqname = "Q{http://www.w3.org/2005/xpath-functions}";
    query("'xyz' =!> " + eqname + "contains('x')", true);
    query("('a', 'b') =!> (" + eqname + "contains('abc', ?))()", "true\ntrue");

    query("('no', 'yes') =!> identity()", "no\nyes");
    query("('no', 'yes') =!> identity() => identity()", "no\nyes");
    query("('no', 'yes') => identity() =!> identity()", "no\nyes");

    query("(1 to 9) =!> count() => count()", 9);
    query("(1 to 9) => count() =!> count()", 1);

    query("-5 =!> abs()", 5);
    query("(-6) =!> abs()", 6);

    query("'abc' =!> ((starts-with#2, ends-with#2) => head())('a')", true);
    query("'abc' =!> ((starts-with#2, ends-with#2) => tail())('a')", false);

    query("(-5 to 0) =!> (abs#1)() => sum()", 15);
    query("let $x := abs#1 return (-5 to 0) =!> $x() => sum()", 15);
    query("for $x in abs#1 return (-5 to 0) =!> $x() => sum()", 15);
    query("declare variable $ABS := abs#1; (-5 to 0) =!> $ABS() => sum()", 15);

    query("1 =!> ([ 'A' ])()", "A");
    query("1 =!> (array { 'B' })()", "B");
    query("(1, 2) =!> (array { 'C', 'D' })()", "C\nD");
    query("1 =!> ({ 1: 'V' })()", "V");
    query("(1, 2) =!> ({ 1: 'W', 2: 'X' })()", "W\nX");
    query("let $map := { 0: 'off', 1: 'on' } return 1 =!> $map()", "on");
    query("(1, 2) =!> ([ 3[2], 1[0] ])()", "");

    query("256 ! 2 =!> xs:byte()", 2);
    query("(256 ! 2) =!> xs:byte()", 2);
    query("256 ! (2 =!> xs:byte())", 2);

    query("'Jemand musste Josef K. verleumdet haben.'"
        + "=> tokenize() =!> string-length() =!> (function{ . + 1 })() => sum()",
        41);
    query("'Happy families are all alike; every unhappy family is unhappy in its own way.'"
      + "=> tokenize()"
      + "=!> fn { upper-case(substring(., 1, 1)) || lower-case(substring(., 2)) }()"
      + "=> string-join(' ')",
      "Happy Families Are All Alike; Every Unhappy Family Is Unhappy In Its Own Way.");

    error("2 ! 256 =!> xs:byte()", FUNCCAST_X_X_X);
    error("1 =!> if()", RESERVED_X);
    error("0 =!> unknown()", WHICHFUNC_X);
    error("0 =!> unknown(?)", WHICHFUNC_X);
    error("0 =!> local:unknown()", WHICHFUNC_X);
    error("0 =!> local:unknown(?)", WHICHFUNC_X);
    error("0 =!> Q{}unknown()", WHICHFUNC_X);
    error("0 =!> Q{}unknown(?)", WHICHFUNC_X);
    error("let $_ := 0 return 0 =!> $_()", INVFUNCITEM_X_X);
  }

  /** Generalized element/attribute tests. */
  @Test public void elemAttrTest() {
    String prefix = "<xml:a/> instance of ";
    query(prefix + "element()", true);
    query(prefix + "element(*)", true);
    query(prefix + "element(*:a)", true);
    query(prefix + "element(xml:*)", true);
    query(prefix + "element(xml:a)", true);
    query(prefix + "element(Q{http://www.w3.org/XML/1998/namespace}a)", true);
    query(prefix + "element(Q{http://www.w3.org/XML/1998/namespace}*)", true);

    query(prefix + "xs:string", false);
    query(prefix + "text()", false);
    query(prefix + "element(*:b)", false);
    query(prefix + "element(xsi:*)", false);
    query(prefix + "element(xml:b)", false);
    query(prefix + "element(xsi:a)", false);
    query(prefix + "element(Q{X}a)", false);
    query(prefix + "element(Q{http://www.w3.org/XML/1998/namespace}b)", false);

    query(prefix + "element(*, xs:untyped)", true);
    query(prefix + "element(*, xs:anyType)", true);
    error(prefix + "element(*, xs:string)", STATIC_X);
    error(prefix + "element(*, xs:untypedAtomic)", STATIC_X);
    error(prefix + "element(*, xs:xyz)", TYPEUNDEF_X);

    prefix = "<_ xml:a=''/>/@* instance of ";
    query(prefix + "attribute()", true);
    query(prefix + "attribute(*)", true);
    query(prefix + "attribute(*:a)", true);
    query(prefix + "attribute(xml:*)", true);
    query(prefix + "attribute(xml:a)", true);
    query(prefix + "attribute(Q{http://www.w3.org/XML/1998/namespace}a)", true);
    query(prefix + "attribute(Q{http://www.w3.org/XML/1998/namespace}*)", true);

    query(prefix + "xs:string", false);
    query(prefix + "text()", false);
    query(prefix + "attribute(*:b)", false);
    query(prefix + "attribute(xsi:*)", false);
    query(prefix + "attribute(xml:b)", false);
    query(prefix + "attribute(xsi:a)", false);
    query(prefix + "attribute(Q{X}a)", false);
    query(prefix + "attribute(Q{http://www.w3.org/XML/1998/namespace}b)", false);

    query(prefix + "attribute(*, xs:untyped)", true);
    query(prefix + "attribute(*, xs:anyType)", true);
    query(prefix + "attribute(*, xs:anySimpleType)", true);
    query(prefix + "attribute(*, xs:anyAtomicType)", true);
    query(prefix + "attribute(*, xs:untypedAtomic)", true);
    error(prefix + "attribute(*, xs:string)", STATIC_X);
    error(prefix + "attribute(*, xs:xyz)", TYPEUNDEF_X);
  }

  /** New if syntax. */
  @Test public void iff() {
    query("if(0) {}", "");
    query("if(1) { 2 }", 2);
    query("if(1) { 2 } else { 3 }", 2);
    query("if(0) {} else { 1 }", 1);
    query("if(0) {} else if(1) { 2 }", 2);
    query("if(0) {} else if(0) {} else { 1 }", 1);
    query("if(0) {} else if(0) {} else if(0) {} else if(0) {} else { 1 }", 1);

    error("if() {}", NOIF);
    error("if(0) {} else", WRONGCHAR_X_X);
    error("if(0) {} else ()", WRONGCHAR_X_X);
    error("if(0) {} else if() {}", NOIF);
    error("if(0) {} else if(0) then", WRONGCHAR_X_X);
    error("if(0) {} else if(0) {} else", WRONGCHAR_X_X);
  }

  /** Constructor functions. */
  @Test public void constructors() {
    query("xs:string(value := 1)", 1);
    query("1 ! xs:string()", 1);
    query("(1 to 6) ! xs:string()", "1\n2\n3\n4\n5\n6");
  }

  /** Keyword arguments. */
  @Test public void keywords() {
    query("count(input := 1)", 1);
    query("declare function local:inc($x) { $x + 1 }; local:inc(x := 1)", 2);
    query("declare function local:inc($x, $y) { $x + $y }; local:inc(x := 1, y := 1)", 2);
    query("declare function local:inc($x, $y) { $x + $y }; local:inc(1, y := 1)", 2);
    // GH-2463
    error("declare function local:inc($x) { }; local:inc(,)", FUNCARG_X);
    error("declare function local:inc($x) { }; local:inc(x := 0,)", FUNCARG_X);
    error("declare function local:inc($x, $y) { }; local:inc(x := 0, y)", FUNCARG_X);
    error("declare function local:inc($x, $y) { }; local:inc(x := 0, y := )", FUNCARG_X);
    error("declare function local:inc($x, $y) { }; local:inc(x := 0, y := 0,)", FUNCARG_X);
  }

  /** String templates. */
  @Test public void stringTemplates() {
    query("``", "");
    query("`1`", 1);
    query("`{}`", "");
    query("`{{``}}`", "{`}");
    query("``[]``", "");

    error("`{`", INCOMPLETE);
    error("```", INCOMPLETE);
    error("`}`", WRONGCHAR_X_X);
  }

  /** Switch expression. */
  @Test public void switchh() {
    query("switch(<?_ _?>) case '_' return 1 default return 2", 1);
    query("switch(2) case 1 to 10 return 1 default return 2", 1);
    query("switch(()) case 1 return 1 case () return 0 default return 2", 0);
    query("switch(<?_ _?>) case '_', '?' return 1 default return 2", 1);
    query("switch(<?_ _?>) case ('?', '!') return 1 default return 2", 2);

    check("(1 to 6) ! (switch(.) case 6 to 8 return 'x' default return ())",
        "x", empty(Switch.class), exists(IterFilter.class));
    check("(1 to 6) ! (switch(.) case 6 to 8 return 'x' case 6 to 8 return '' default return ())",
        "x", empty(Switch.class), exists(IterFilter.class));
  }

  /** Numeric literals. */
  @Test public void literals() {
    query("0000000000000001", 1);
    query("1", 1);
    query("15", 15);
    query("9223372036854775807", Long.MAX_VALUE);
    query("-9223372036854775807", -Long.MAX_VALUE);

    query("0x0000000000000001", 1);
    query("0x1", 1);
    query("0xF", 15);
    query("0x7FFFFFFFFFFFFFFF", Long.MAX_VALUE);
    query("-0x7FFFFFFFFFFFFFFF", -Long.MAX_VALUE);

    query("0b0000000000000001", 1);
    query("0b1", 1);
    query("0b1111", 15);
    query("0b111111111111111111111111111111111111111111111111111111111111111", Long.MAX_VALUE);
    query("-0b111111111111111111111111111111111111111111111111111111111111111", -Long.MAX_VALUE);

    // underscores
    query("0b0_1", 1);
    query("0x2_3", 35);
    query("4_5", 45);
    query("67.89", 67.89);
    query("1_000_000", 1_000_000);
    query("1_2__3_________4.5______________6e7________________________8", "1.23456e+81");

    error("_1", NOCTX_X);
    error("2._3", NUMBER_X);
    error("4e_5", NUMBER_X);
    error("6.7e_8", NUMBER_X);

    error("0b_0", NUMBER_X);
    error("0b1_", NUMBER_X);
    error("0x_2", NUMBER_X);
    error("0x3_", NUMBER_X);
    error("0x3_", NUMBER_X);
    error("0x4_", NUMBER_X);
    error("5.6_", NUMBER_X);
    error("7.8e9_", NUMBER_X);
  }

  /** Try/catch expression. */
  @Test public void tryy() {
    // $err:map
    query("try { 1 + <_/> } catch * { $err:map?code }", "#err:FORG0001");
    query("try { 1 + <_/> } catch * { $err:map?line-number }", 1);
    query("try { 1 + <_/> } catch * { boolean($err:map?stack-trace) }", true);
    query("try { 1 + <_/> } catch * { $err:map?value }", "");
    query("try { 1 + <_/> } catch * { map:size($err:map) }", 6);

    query("try { error((), (), 1) } catch * { $err:map?value }", 1);
    query("try { error(xs:QName('a')) } catch * { $err:map?code }", "#a");

    query("declare function local:f($a) {"
        + "  try { 1 div 0 } catch * { if($a > 0) then local:f($a - 1) else $a } "
        + "}; local:f(10000)", 0);
    query("declare function local:f($a) {"
        + "  try { if($a > 0) then local:f($a - 1) else $a } catch * { 1 } "
        + "}; local:f(10000)", 0);
    query("declare function local:f($a) {"
        + "  try { 0 } catch * { 1 } "
        + "}; local:f(10000)", 0);
    query("declare function local:f($a) {"
        + "  try {} catch * { if($a > 0) { local:f($a - 1) } } "
        + "}; local:f(10000)", "");

    // finally
    check("try { 1 } finally {}", 1, root(Itr.class));
    check("try { 1 } catch * { 2 } finally {}", 1, root(Itr.class));
    check("try { 1 div 0 } catch * { 2 } finally {}", 2, root(Itr.class));

    check("try { 1 } finally { (1 to 1000)[. = 0] }", 1, root(Try.class));
    check("try { 1 } catch * { 2 } finally { (1 to 1000)[. = 0] }", 1, root(Try.class));
    check("try { 1 div 0 } catch * { 2 } finally { (1 to 1000)[. = 0] }", 2, root(Try.class));

    check("try { 1 } catch * { error() } finally { message('done') }", 1, empty(ERROR));
    check("try { 1 } catch err:FOER0 { 2 } finally {}", 1, root(Itr.class), count(Itr.class, 1));
    check("try { 1 } catch err:FOER0 { 2 } catch err:FOER1 { 3 } finally {}", 1,
        root(Itr.class), count(Itr.class, 1));

    check("let $a := 1 return try { $a } finally { $a[. = 0] }", 1, root(Itr.class));
    check("let $a := 1 return try { $a } catch * { $a } finally { $a[. = 0] }", 1, root(Itr.class));

    error("try { 1 } catch * { 2 } finally { 2 div 0 }", DIVZERO_X);
    error("try { error() } catch * { 2 } finally { 2 div 0 }", DIVZERO_X);
    error("try { 1 } catch * { error() } finally { 2 div 0 }", DIVZERO_X);
    error("try { 1 div 0 } catch err:FOER0 { 2 } finally {}", DIVZERO_X);
    error("try { 1 div 0 } catch err:FOER0 { 2 } catch err:FOER1 { 2 } finally {}", DIVZERO_X);

    error("try { 1 } finally { 1 }", FINALLY_X);
    error("try { error() } finally { 1 }", FINALLY_X);
    error("try { 1 } catch * { 2 } finally { 1 }", FINALLY_X);
    error("try { 1 } catch * { 2 } finally { 1 }", FINALLY_X);
    error("try { error() } catch * { 2 } finally { 1 }", FINALLY_X);
    error("try { error() } catch * { error() } finally { 1 }", FINALLY_X);
    error("try { 1 } finally { (1 to 1000)[. > 0] }", FINALLY_X);
    error("try { 1 } catch * { 2 } finally { (1 to 1000)[. > 0] }", FINALLY_X);
    error("try { 1 div 0 } catch * { 2 } finally { (1 to 1000)[. > 0] }", FINALLY_X);
    error("let $a := 1 return try { $a } finally { $a[. = 1] }", FINALLY_X);
    error("let $a := 1 return try { $a } catch * { $a } finally { $a[. = 1] }", FINALLY_X);

    // non-catchable errors (e.g. from lazy variable evaluation) must not be caught
    error("declare %basex:lazy variable $x := error(); try { $x } catch * { 1 }", FUNERR1);
    error("declare %basex:lazy variable $x := error(); try { $x } catch err:FOER0000 { 1 }",
        FUNERR1);
    // finally block must still run even for non-catchable errors
    error("declare %basex:lazy variable $x := error(); try { $x } finally { 1 }", FINALLY_X);
  }

  /** Eager vs. lazy evaluation of prolog variables. */
  @Test public void staticVariables() {
    // non-deterministic global variable, referenced but folded away: must not be evaluated
    query("declare variable $x := error(); if(<a/>/b) then $x else 1", 1);
    query("declare variable $x := prof:sleep(1); if(<a/>/b) then $x else 1", 1);
    // referenced only through a function whose call is folded away: still not evaluated
    query("declare variable $x := error();"
        + "declare function local:f() { $x };"
        + "if(<a/>/b) then local:f() else 1", 1);

    // referenced non-deterministic global variable: still evaluated (errors surface)
    error("declare variable $x := error(); $x", FUNERR1);
    error("declare variable $x := error(); if(<a/>/self::a) then $x else 1", FUNERR1);
    error("declare variable $x := error();"
        + "declare function local:f() { $x };"
        + "if(<a/>/self::a) then local:f() else 1", FUNERR1);
    // a lazy non-deterministic variable is evaluated only once (value is cached)
    query("declare variable $x := random:integer(); $x = $x", true);
    // a non-deterministic variable referenced twice keeps a single, shared value
    query("declare variable $x := random:integer(); ($x, $x)[1] = ($x, $x)[2]", true);

    // deterministic global variables stay eager: still pre-evaluated even if folded away
    error("declare variable $x := 1 idiv 0; if(<a/>/b) then $x else 1", DIVZERO_X);
  }

  /** Window expression. */
  @Test public void window() {
    query("for tumbling window $w in 1 to 2 "
        + "return sum($w)", "1\n2");
    query("for tumbling window $w in 1 to 2 "
        + "start "
        + "return sum($w)", "1\n2");
    query("for tumbling window $w in 1 to 2 "
        + "end "
        + "return sum($w)", "1\n2");
    query("for tumbling window $w in 1 to 2 "
        + "only end "
        + "return sum($w)", "1\n2");
    query("for tumbling window $w in 1 to 2 "
        + "start end "
        + "return sum($w)", "1\n2");
    query("for tumbling window $w in 1 to 2 "
        + "start only end "
        + "return sum($w)", "1\n2");
    query("for tumbling window $w in 1 to 2 "
        + "start when true() end "
        + "return sum($w)", "1\n2");
    query("for tumbling window $w in 1 to 2 "
        + "start when true() only end "
        + "return sum($w)", "1\n2");
    query("for tumbling window $w in 1 to 2 "
        + "start only end when true()"
        + "return sum($w)", "1\n2");
    query("for tumbling window $w in 1 to 2 "
        + "start end when true()"
        + "return sum($w)", "1\n2");
    query("for tumbling window $w in 1 to 2 "
        + "start $s at $sa previous $sp next $sn "
        + "end $e at $ea previous $ep next $en "
        + "return sum($w)", "1\n2");

    error("for sliding window $w in 1 to 2 "
        + "return sum($w)", WRONGCHAR_X_X);
    error("for sliding window $w in 1 to 2 "
        + "start "
        + "return sum($w)", WRONGCHAR_X_X);
    query("for sliding window $w in 1 to 2 "
        + "end "
        + "return sum($w)", "1\n2");
    query("for sliding window $w in 1 to 2 "
        + "only end "
        + "return sum($w)", "1\n2");
    query("for sliding window $w in 1 to 2 "
        + "start end "
        + "return sum($w)", "1\n2");
    query("for sliding window $w in 1 to 2 "
        + "start only end "
        + "return sum($w)", "1\n2");
    query("for sliding window $w in 1 to 2 "
        + "start when true() end "
        + "return sum($w)", "1\n2");
    query("for sliding window $w in 1 to 2 "
        + "start when true() only end "
        + "return sum($w)", "1\n2");
    query("for sliding window $w in 1 to 2 "
        + "start end when true()"
        + "return sum($w)", "1\n2");
    query("for sliding window $w in 1 to 2 "
        + "start only end when true()"
        + "return sum($w)", "1\n2");

    query("for tumbling window $w in 1 to 4 "
        + "end $e when $e = 3 "
        + "return sum($w)", "6\n4");
    query("for tumbling window $w in 1 to 4 "
        + "only end $e when $e = 3 "
        + "return sum($w)", "6");

    query("for sliding window $w in 1 to 4 "
        + "end $e when $e = 3 "
        + "return sum($w)", "6\n5\n3\n4");
    query("for sliding window $w in 1 to 4 "
        + "only end $e when $e = 3 "
        + "return sum($w)", "6\n5\n3");
  }

  /** Record tests. */
  @Test public void recordTest() {
    query("{} instance of record()", true);
    query("{ 'x': 1 } instance of record(x)", true);
    query("{ 'x': 1, 'y': 2 } instance of record(x, y)", true);
    query("{ 'x': 1 } instance of record(x as xs:integer)", true);

    query("{} instance of record(x)", true);
    query("{ 'x': 1 } instance of record(x, y)", true);
    query("{} instance of record(x as xs:integer)", false);
    query("{ 'x': 1 } instance of record(x as xs:string)", false);
    query("{ 'x': 1, 'y': 2 } instance of record(x)", false);

    // record(*) matches any record (a map with string keys)
    query("{} instance of record(*)", true);
    query("{ 'x': 1 } instance of record(*)", true);
    query("{ 1: 2 } instance of record(*)", false);
  }

  /** While clause. */
  @Test public void whilee() {
    query("for $i in 1 to 4 while $i > 2 return $i", "");
    query("for $i in 1 to 4 while $i < 3 return $i", "1\n2");
    query("for $i in 1 to 4 while $i return $i", "1\n2\n3\n4");
    query("for $i in 1 to 4 order by $i descending while $i > 2 return $i", "4\n3");
    query("for $i allowing empty in () while count($i) = 0 return 1", 1);
    query("for $i allowing empty in () while count($i) != 0 return 1", "");
  }

  /** Array coercion. */
  @Test public void arrayCoercion() {
    query("fn($a as array(xs:integer)) { $a }([ 1.0 ])", "[1]");
    query("fn($a as array(xs:double)) { $a }([ 1, 2 ])", "[1,2]");
    query("fn($a as array(xs:byte)) { $a }([ 1, 2 ])", "[1,2]");

    query("fn($a as array(xs:double)) { $a }([ 1, 2 ]) instance of array(xs:double)", true);
    query("fn($a as array(xs:byte)) { $a }([ 1, 2 ]) instance of array(xs:byte)", true);
    query("fn($a as array(xs:double)) { $a }([ 1, 2 ]) instance of array(xs:integer)", false);

    error("fn($a as array(xs:integer)) { $a }([ 1.2 ])", INVTYPE_X);
  }

  /** Map coercion. */
  @Test public void mapCoercion() {
    query("fn($a as map(xs:double, item())) { $a }({ 1.2: 0 })", "{1.2:0}");

    query("fn($a as map(xs:double, item())) { $a }({ 1.2: 0 }) instance of map(xs:double, item())",
        true);
    query("fn($a as map(xs:double, item())) { $a }({ 1.2: 0 }) instance of map(xs:integer, item())",
        false);

    error("fn($a as map(xs:float, item())) { $a }({ 1.2: 0, 1.2000000001: 0 })", INVTYPE_X);
  }

  /** Map constructor. */
  @Test public void mapConstructor() {
    check("{}", "{}", root(XQTrieMap.class));
    check("{ 1: 2 }", "{1:2}", root(XQSingletonMap.class));
    check("{ 1: 2, 3: 4 }", "{1:2,3:4}", root(XQIntMap.class));

    check("{ () }", "{}", root(XQTrieMap.class));
    check("{ {} }", "{}", root(XQTrieMap.class));
    check("{ { 1: 2 } }", "{1:2}", root(XQSingletonMap.class));
    check("{ { 1: 2 }[?1 = 2] }", "{1:2}", root(XQSingletonMap.class));
    check("{ { 1: 2 }[?1 = 0] }", "{}", root(XQTrieMap.class));
    check("{ (), { 1: 2 }[?1 = 0] }", "{}", root(XQTrieMap.class));

    check("{ map:build((1 to 6) ! xs:int()) }", "{1:1,2:2,3:3,4:4,5:5,6:6}",
        root(MapBuild.class));
    check("{ 0: 0, map:build((1 to 6) ! xs:int()), () }",
        "{0:0,1:1,2:2,3:3,4:4,5:5,6:6}", root(CMap.class));
    check("{ (1 to 6) ! { .: . } }", "{1:1,2:2,3:3,4:4,5:5,6:6}", root(CMap.class));

    check("{ 'one': 1, { 'two': 2 } }", "{\"one\":1,\"two\":2}", root(XQRecordMap.class));
    check("{ 'one': 1, { 2: 'two' } }", "{\"one\":1,2:\"two\"}", root(XQItemValueMap.class));

    check("{ 0: <a/>, 1: <b/> } => map:size()", 2, root(Itr.class));
    check("{ 0: 0, { 1: 1 } } => map:size()", 2, root(Itr.class));
    check("{ 0: 0, (1 to 6) ! { .: . } } => map:size()", 7, root(_MAP_SIZE));

    query("{ 'a': 'b' }/. ! { . }", "{\"a\":\"b\"}");
    query("{ 'a': 'b' }/a ! { . }", "{\"a\":\"b\"}");

    check("{ 1: 1, <a/>[. = 'b'] }", "{1:1}",
        root(CMap.class), type(CMap.class, "map(*)"));
    check("{ 1: <a/>[. = 'b'], 2: 1 }", "{1:(),2:1}",
        root(CMap.class), type(CMap.class, "map(xs:integer, item()?)"));
    check("{ 1: (<a/>, <b/>)[. = 'b'], 2: 1 }", "{1:(),2:1}",
        root(CMap.class), type(CMap.class, "map(xs:integer, item()*)"));
    check("{ (1 to 6)[. = 1]: (<a/>, <b/>)[. = 'b'], 2: 1 }", "{1:(),2:1}",
        root(CMap.class), type(CMap.class, "map(xs:integer, item()*)"));
    error("{ 1: 1, <a/>[. = ''] }", INVTYPE_X);

    error("{ 1: 1, 1: 1 }", MAPDUPLKEY_X);
    error("{ 1: <a/>, 1: 1 }", MAPDUPLKEY_X);
    error("{ 1: <a/>, { 1: 1 } }", MAPDUPLKEY_X);
    error("{ 1: <a/>, (1 to 6) ! { .: . } }", MAPDUPLKEY_X);
  }

  /** Path extensions: dynamic node test {expr}. */
  @Test public void pathSelector() {
    check("<a><b/></a>/self::{#a}", "<a><b/></a>", root(CElem.class));
    check("<a><b/></a>/child::{#b}", "<b/>", type(IterStep.class, "element(b)*"));
    check("<a><b/></a>/descendant-or-self::{#b}", "<b/>", type(IterStep.class, "element(b)*"));
    check("<a><b/></a>/child::{#c}", "", type(IterStep.class, "element(c)*"));
    check("<a><b/></a>/child::{()}", "", empty());

    check("<a><b/></a>/child::{#b, 1}", "<b/>", exists(SelectorStep.class));
    check("<a><b/></a>/child::{#c, 1}", "", exists(SelectorStep.class));

    check("<a><b/><c/></a>/child::{#c, #b}", "<b/>\n<c/>",
        type(IterStep.class, "(element(c)|element(b))*"));
    check("let $names := (#c, #b) return <a><b/><c/></a>/child::{$names}", "<b/>\n<c/>",
        type(IterStep.class, "(element(c)|element(b))*"));

    check("<a><b/><c/></a>/(c | child::{#b})", "<b/>\n<c/>",
        type(IterStep.class, "(element(c)|element(b))*"));
    check("<a><b/><c/></a>/child::{xs:QName('c')}", "<c/>",
        type(IterStep.class, "element(c)*"));
    check("<a><b/><c/></a>/child::{xs:QName(<?_ c?>)}", "<c/>",
        exists(SelectorStep.class));
    check("let $name := #b return <a><b/><c/></a>/child::{$name}", "<b/>",
        type(IterStep.class, "element(b)*"));

    // match local name against string values
    check("<a><b/></a>/child::{'b'}", "<b/>", exists(SelectorStep.class));
    check("<a><b/></a>/child::{'c'}", "", exists(SelectorStep.class));
    check("<a><b/></a>/child::{'b', 'c'}", "<b/>", exists(SelectorStep.class));
    check("declare namespace x = 'u'; <x:a><x:b/></x:a>/child::{'b'}",
        "<x:b xmlns:x=\"u\"/>", exists(SelectorStep.class));

    // attribute axis
    check("<x a='1' b='2'/>/attribute::{'b'}", "b=\"2\"", exists(SelectorStep.class));
    check("<x a='1' b='2'/>/@{#a}", "a=\"1\"", type(IterStep.class, "attribute(a)?"));
    check("string(<x a='1' b='2'/>/@{#a})", "1", root(STRING));

    // selector must survive step merging (descendant-or-self::node()/self::{E});
    // a constant atomic selector on JNodes folds to a JNodeTest step (no SelectorStep remains)
    check("let $m := { 'x': 1, 'y': 2, 'z': 3 } return $m//self::{ 'z' } =!> jvalue()", 3,
        empty(SelectorStep.class));
    check("let $m := { 'a': { 'z': 9 } } return $m//self::{ 'z' } ! jvalue()", 9,
        empty(SelectorStep.class));
    // selector is never a redundant self step, and is never exactly-one (XML keeps SelectorStep)
    check("count(<a/>/self::{ 'x' })", 0, exists(SelectorStep.class));
    check("count(<a/>/self::{ 'a' })", 1, exists(SelectorStep.class));
    // name filter must survive predicate flattening and predicate merging
    check("let $m := { 'x': 3, 'z': 1 } return exists($m/child::{ 'z' }[jvalue() = 3])", false,
        empty(SelectorStep.class));
    check("let $m := { 'x': 1, 'y': 2, 'z': 3 } return "
        + "($m/child::{ ('y', 'z') })[jvalue() > 2] ! jvalue()", 3, empty(SelectorStep.class));
  }

  /** Path operator: JNode navigation with atomic step results (jkey matching). */
  @Test public void pathJNode() {
    // an atomic step result selects children whose jkey matches
    query("{ 'a': 1, 'b': 2 }/'b' ! jvalue()", "2");
    query("let $k := 'b' return { 'a': 1, 'b': 2 }/$k ! jvalue()", "2");
    query("{ 'a': 1, 'b': 2 }/('a', 'b') ! jvalue()", "1\n2");
    query("[ 10, 20, 30 ]/2 ! jvalue()", "20");
    query("{ 'a': 1 }/'x'", "");
    // numeric key matching follows atomic-equal (any integral, in-range numeric key)
    query("[ 10, 20, 30 ]/2.0e0 ! jvalue()", "20");
    query("[ 10, 20, 30 ]/xs:decimal(2) ! jvalue()", "20");
    // non-integral or out-of-range numeric array keys select nothing
    query("([ 10, 20, 30 ]/2.5, [ 10, 20, 30 ]/0, [ 10, 20, 30 ]/9, [ 10, 20, 30 ]/'2')", "");
    // result is in document order, duplicates removed
    query("{ 'a': 1, 'b': 2, 'c': 3 }/('c', 'a', 'c') ! jvalue()", "1\n3");
    // equivalent to the child::{ E } selector
    query("let $m := { 'a': 1, 'b': 2 } return deep-equal($m/'b', $m/child::{ 'b' })", "true");
    // a constant atomic selector on a JNode folds to a JNodeTest step (direct key lookup) ...
    check("let $m := { 'a': 1, 'b': 2 } return $m/'b' ! jvalue()", 2,
        exists(IterStep.class), empty(SelectorStep.class));
    check("let $a := [ 10, 20, 30 ] return $a/2 ! jvalue()", 20,
        exists(IterStep.class), empty(SelectorStep.class));
    check("{ 'x': 1 }/'x' ! jvalue()", 1, exists(IterStep.class), empty(SelectorStep.class));
    // ... a non-constant selector stays a SelectorStep ...
    check("declare function local:f($k) { { 'a': 1 }/child::{ $k } }; local:f('a') ! jvalue()", 1,
        exists(SelectorStep.class));
    // ... and the mixed-path form is preserved for XML nodes (result is the atomic value)
    check("<a/>/'X'", "X", root(Str.class), empty(SelectorStep.class));
    // node-returning steps are unaffected
    query("{ 'name': 'Alice' }/name ! jvalue()", "Alice");
    query("[ { 'c': 'London' }, { 'c': 'Berlin' } ]//c ! jvalue()", "London\nBerlin");
    // XNode paths with atomic results are unaffected
    query("<a><b/></a>/name()", "a");

    // a single-use let must not drop the JNode context when folding 'context/atomic' to 'atomic'
    query("let $x := if (current-date() lt xs:date('2000-01-01')) then parse-xml('<x/>') " +
        "else [ 4, 5 ] return $x/2", "5");
    query("let $x := [ 4, 5 ][current-date() ge xs:date('2000-01-01')] return $x/2", "5");
    // XML-node context: 'context/atomic' still folds to the atomic
    query("let $n := <a/> return $n/2", "2");

    // a selected JNode is a node and atomizes to its value (must not be statically typed atomic)
    query("[ 1 ] / 1 instance of xs:integer", "false");
    query("data([ 1 ] / 1)", "1");
    query("[ 1 ] / 1 + 0", "1");
    query("{ 'a': 2 } / 'a' + 3", "5");
    // predicates: an atomic-keyed selection must differ for matching vs. non-matching keys
    query("[ 1 ][./0]", "");
    query("[ 1 ][./1]", "[1]");
    query("[ 1 ][./1 = 1]", "[1]");

    // GH-2709: each map or array used as a path root yields a JNode with distinct identity
    // (data model, 4.0: "every operation that constructs a root JNode returns a JNode with
    // distinct identity"), even when the wrapped values are equal or the same object
    query("({ 'x': 1 }, { 'x': 1 })/x", "1\n1");
    query("count(({ 'x': 1 }, { 'x': 1 }, { 'x': 1 })/x)", "3");
    query("count(([ 1 ], [ 1 ])/?*)", "2");
    query("count(replicate({ 'x': 1 }, 2)/x)", "2");
    query("let $m := { 'x': 1 } return count(($m, $m)/x)", "2");
    query("(1 to 6) ! { 'a': 1 } -> (.[1]/a is .[2]/a)", "false");
    // each explicit or implicit JNode construction has a fresh identity: node-creating
    // expressions carry Flag.CNS, so equal ones are not merged into a single construction
    query("let $m := { 'a': 1 } return count(distinct-ordered-nodes((jtree($m), jtree($m))))", "2");
    query("let $m := { 'a': 1 } return count(distinct-ordered-nodes(($m/a, $m/a)))", "2");
    // a single construction bound to a variable keeps one identity, even if referenced twice
    query("let $m := { 'a': 1 } return count(distinct-ordered-nodes("
        + "let $y := $m/a return ($y, $y)))", "1");
    // fresh identity is kept even when the path result is not statically a JNode, but only
    // may be one: a heterogeneous XML/map root yields the union type (jnode(a)|element(a)),
    // which is not an instance of jnode() yet can still produce JNodes
    query("let $c := (<x><a/></x>, { 'a': 1 }) return "
        + "count(distinct-ordered-nodes(($c/a, $c/a))[. instance of jnode()])", "2");

    // JNode identity: distinct positions are kept even when key and value coincide
    query("count([ ['a'], ['a'] ]//1)", "3");
    query("count(distinct-ordered-nodes([ [['a'],['b']], [['c'],['d']] ]//1 ! .//1))", "4");
    // descendant-or-self over a JNode selection must keep the self node
    query("count([ [['a'],['b']] ]/1/descendant-or-self::jnode())", "5");
    query("count([ [['a'],['b']], [['c'],['d']] ]//1//1)", "4");
    query("[ [['a'],['b']], [['c'],['d']] ]//1//1 ! jvalue()[. instance of xs:string]",
        "a\nb\nc");
  }

  /** Destructuring let. */
  @Test public void destructuringLet() {
    String value = "1 to 3";
    query("let $($a) := " + value + " return [ $a ]", "[(1,2,3)]");
    query("let $($a, $b) := " + value + " return [ $a, $b ]", "[1,(2,3)]");
    query("let $($a, $b, $c) := " + value + " return [ $a, $b, $c ]", "[1,2,3]");
    query("let $($a, $b, $c, $d) := " + value + " return [ $a, $b, $c, $d ]", "[1,2,3,()]");

    value = "[ 1, 2, 3 ]";
    query("let $[$a] := " + value + " return [ $a ]", "[1]");
    query("let $[$a, $b] := " + value + " return [ $a, $b ]", "[1,2]");
    query("let $[$a, $b, $c] := " + value + " return [ $a, $b, $c ]", "[1,2,3]");
    error("let $[$a, $b, $c, $d] := " + value + " return [ $a, $b, $c, $d ]", ARRAYBOUNDS_X_X);

    value = "{ 'a': 1, 'b': 2, 'c': 3 }";
    query("let ${$a} := " + value + " return [ $a ]", "[1]");
    query("let ${$a, $b} := " + value + " return [ $a, $b ]", "[1,2]");
    query("let ${$a, $b, $c} := " + value + " return [ $a, $b, $c ]", "[1,2,3]");
    query("let ${$a, $b, $c, $d} := " + value + " return [ $a, $b, $c, $d ]", "[1,2,3,()]");

    query("declare record local:r(a as xs:integer, b as xs:integer); "
        + "let ${ $a, $b } := local:r(1, 2) return $a + $b", 3);
    error("declare record local:r(a as xs:integer, b as xs:integer); "
        + "let ${ $c } := local:r(1, 2) return $c", RECORDFIELD_X_X);
    query("let $m as map(xs:string, xs:integer) := map:merge(('a', 'b') ! map:entry(., 1)) "
        + "let ${ $z } := $m return empty($z)", true);

    // GH-2452
    query("""
      let $($a, $b) := (1 to 6) ! string()
      for $i in 1 to 3
      return [ $a, $b, $i ]
    """, """
      ["1",("2","3","4","5","6"),1]
      ["1",("2","3","4","5","6"),2]
      ["1",("2","3","4","5","6"),3]""");
  }

  /** EQNames: optional prefix syntax (qtspecs#2227) must not swallow a following ":=". */
  @Test public void eqName() {
    query("let $Q{}foo := 1 return $Q{}foo", 1);
    query("let $Q{u}foo := 1 return $Q{u}foo", 1);

    // optional prefix in EQName syntax (Q{uri}prefix:local) must still be parsed
    query("Q{http://www.w3.org/2005/xpath-functions}fn:abs(-1)", 1);
    query("1 instance of Q{http://www.w3.org/2001/XMLSchema}xs:integer", true);

    // gh-2701: URIQualifiedName variable directly followed by ":=" (no intervening whitespace)
    query("let $Q{}foo:=1 return $Q{}foo", 1);
  }

  /** Unprefixed function names (qtspecs#2569): explicit default function namespace wins. */
  @Test public void defaultFunctionNamespace() {
    // no declaration: no-namespace functions are preferred, fn functions serve as fallback
    query("declare function f() { 'no-namespace' }; f()", "no-namespace");
    query("declare function data($v) { 'shadowed' }; data(1)", "shadowed");
    query("declare function data($v) { 'shadowed' }; data#1(1)", "shadowed");
    query("abs(-1)", 1);

    // explicit declaration: no search for no-namespace functions
    final String decl = "declare default function namespace 'urn:test'; ";
    query(decl + "declare function f() { 'ns' }; declare function Q{}f() { 'no-ns' }; f()", "ns");
    query(decl + "declare function f() { 'ns' }; declare function Q{}f() { 'no-ns' }; f#0()", "ns");
    query(decl + "declare function Q{}f() { 'no-ns' }; Q{}f()", "no-ns");
    query("declare default function namespace 'http://www.w3.org/2005/xpath-functions'; abs(-1)",
        1);
    error(decl + "declare function Q{}f() { 'no-ns' }; f()", WHICHFUNC_X);
    error(decl + "declare function Q{}f() { 'no-ns' }; f#0()", WHICHFUNC_X);
    error(decl + "abs(-1)", WHICHFUNC_X);

    // empty string: unprefixed names refer to no-namespace functions only
    query("declare default function namespace ''; declare function f() { 'no-ns' }; f()", "no-ns");
    error("declare default function namespace ''; abs(-1)", WHICHFUNC_X);
  }

  /** Calls of dynamic function sequences. */
  @Test public void dynamicFunctionSequences() {
    // empty sequences
    query("()()", "");
    query("()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()()", "");
    query("(true#0[. instance of xs:integer])()", "");

    // non-empty sequences
    query("((), true#0)()", "true");
    query("(true#0, true#0)()", "true\ntrue");
    query("((true#0, 1, true#0)[. instance of fn(*)])()", "true\ntrue");
    query("(substring-before(?, 'b'), substring-before(?, 'c'))('abc')", "a\nab");
    query("(tokenize#1, string-length#1)('a b')", "a\nb\n3");

    // arrow expression
    query("'|' => (substring-before('ab|yz', ?), substring-after('ab|yz', ?))()", "ab\nyz");
    query("'|' =!> (substring-before('ab|yz', ?), substring-after('ab|yz', ?))()", "ab\nyz");
    query("'ab|yz' ! ('|' => (substring-before(., ?), substring-after(., ?))())", "ab\nyz");

    // maps and arrays
    query("{ 'a': (count#1, sum#1) }('a')(2)", "1\n2");
    query("{ 'a': (count#1, sum#1) }?a(2)", "1\n2");
    query("{ 'count': count#1, 'sum': sum#1  }?*(2)", "1\n2");
    query("[ (count#1, sum#1) ](1)(2)", "1\n2");
    query("[ (count#1, sum#1) ]?1(2)", "1\n2");

    // ensure that iterative evaluation will ignore invalid input
    query("head((true#0, 1)())", "true");

    // ensure that single functions are optimized when sequence is unrolled
    unroll(true);
    inline(true);
    check("(true#0, false#0)()", "true\nfalse", root(BlnSeq.class));
    check("((true#0, 1, false#0)[. instance of fn(*)])()", "true\nfalse", root(BlnSeq.class));
    check("(substring-before(?, 'b'), substring-before(?, 'c'))('abc')", "a\nab",
        root(StrSeq.class));
  }

  /** Tests involving type xs:error. */
  @Test public void xsError() {
    error("23 ! xs:error()", FUNCCAST_X_X);
    error("xs:error(23)", FUNCCAST_X_X);
    error("23 cast as xs:error", FUNCCAST_X_X);
    error("23 cast as xs:error?", FUNCCAST_X_X);
    error("() cast as xs:error", INVTYPE_X);
    error("(1, 2) cast as xs:error", INVTYPE_X);
    error("(1, 2) cast as xs:error?", INVTYPE_X);
    query("xs:error(())", "");
    query("() cast as xs:error?", "");
    query("23 instance of xs:error", false);
    query("23 castable as xs:error", false);

    // xs:error? (empty category) and xs:error (void category) as function return types
    query("xs:error#1 instance of function(xs:anyAtomicType?) as empty-sequence()", true);
    query("xs:error#1 instance of function(xs:anyAtomicType?) as xs:error?", true);
    query("xs:error#1 instance of function(xs:anyAtomicType?) as xs:string", false);
    query("fn() as xs:error { error() } instance of function() as empty-sequence()", true);

    // coercion to xs:error (function conversion, type declarations) is a type error, not a cast
    error("declare function local:f($a as xs:error) { $a }; local:f(1)", INVTYPE_X);
    error("fn($a as xs:error) { $a }(1)", INVTYPE_X);
    error("fn($a as xs:error) { $a }(xs:untypedAtomic('x'))", INVTYPE_X);
    error("let $x as xs:error := 1 return $x", INVTYPE_X);

    query("fn:error(code := ?, description := ?, value := ?) instance of function(xs:QName?, xs:str"
        + "ing?, item()*) as xs:error", true);

    // error() is void: optimizations must not discard it without evaluating it
    error("exactly-one((true(), error()))", FUNERR1);
    error("zero-or-one((true(), error()))", FUNERR1);
    error("remove(error(), 1)", FUNERR1);
    error("tail(error())", FUNERR1);
    error("trunk(error())", FUNERR1);
    error("error() castable as xs:error", FUNERR1);
    error("declare function local:f() as empty-sequence() { error() }; local:f()", FUNERR1);

    // functions whose result depends on the value or cardinality of error() must evaluate it
    error("empty(error())", FUNERR1);
    error("exists(error())", FUNERR1);
    error("count(error())", FUNERR1);
    error("boolean(error())", FUNERR1);
    error("sum(error())", FUNERR1);
    error("head(error())", FUNERR1);
    error("reverse(error())", FUNERR1);
    error("string(error())", FUNERR1);
    error("duplicate-values(error())", FUNERR1);
  }
}
