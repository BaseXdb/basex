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
    query(rect + "=?>area()", 12);
    query(rect + "?area instance of fn() as item()*", false);
    query(rect + "('area') instance of fn(item()*) as item()*", true);

    query("declare record local:rect(height, width, area := fn { ?height × ?width }); "
        + "let $r := local:rect(3, 4) return local:rect(5, 6, $r?area)=?>area()", 30);
    query("{ 'self': fn { . } }=?>self() => map:keys()", "self");
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
    query("1_2__3_________4.5______________6e7________________________8", "1.23456e81");

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
    query("{} instance of record(*)", true);
    query("{} instance of record(x?, *)", true);
    query("{} instance of record(x?, y?, *)", true);
    query("{ 'x': 1 } instance of record(x, y?, *)", true);
    query("{ 'y': 1 } instance of record(x?, y, *)", true);
    query("{ 'x': 1 } instance of record(x as xs:integer)", true);
    query("{ 'x': 1 } instance of record(x as xs:integer, *)", true);

    query("{} instance of record(x, *)", false);
    query("{ 'x': 1 } instance of record(x?, y, *)", false);
    query("{ 'y': 1 } instance of record(x, y?, *)", false);
    query("{ 'x': 1 } instance of record(x as xs:string)", false);
    query("{ 'x': 1 } instance of record(x as xs:string, *)", false);
    query("{ 'x': 1 } instance of record(x? as xs:string)", false);
    query("{ 'x': 1 } instance of record(x? as xs:string, *)", false);

    error("{} instance of record(*, x)", WRONGCHAR_X_X);
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

  /** Structure filter. */
  @Test public void structFilter() {
    query("[]?[true()]", "[]");
    query("[]?[false()]", "[]");

    query("[ 1 ]?[true()]", "[1]");
    query("[ 1 ]?[false()]", "[]");

    query("[ 1, 2 ]?[true()]", "[1,2]");
    query("[ 1, 2 ]?[false()]", "[]");

    query("[ 1, 2 ]?[1]", "[1]");
    query("[ 1, 2 ]?[3]", "[]");
    query("[ 1, 2 ]?['a']", "[1,2]");
    query("[ 1, 2 ]?['']", "[]");

    query("[ 1, 2 ]?['a']?['a']?['a']", "[1,2]");
    query("[ 1, 2 ]?[1]?[1]?[1]", "[1]");

    query("{}?[true()]", "{}");
    query("{}?[false()]", "{}");

    query("{ 1: 2 }?[true()]", "{1:2}");
    query("{ 1: 2 }?[false()]", "{}");

    query("{ 1: 2, 3: 4}?[true()]", "{1:2,3:4}");
    query("{ 1: 2, 3: 4}?[false()]", "{}");

    query("{ 1: 2, 3: 4}?[?key = 1]", "{1:2}");
    query("{ 1: 2, 3: 4}?[?key = 8]", "{}");
    query("{ 1: 2, 3: 4}?[?value = 2]", "{1:2}");
    query("{ 1: 2, 3: 4}?[?value = 9]", "{}");
    query("{ 1: 2, 3: 4}?['a']", "{1:2,3:4}");
    query("{ 1: 2, 3: 4}?['']", "{}");

    query("{ 1: 2, 3: 4}?['a']?['a']?['a']", "{1:2,3:4}");
    query("{ 1: 2, 3: 4}?[?key = 1]?[?key = 1]?[?key = 1]", "{1:2}");
    query("{ 1: 2, 3: 4}?[?value = 2]?[?value = 2]?[?value = 2]", "{1:2}");
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

  /** Path extensions: get(). */
  @Test public void pathGet() {
    check("<a><b/></a>/self::get(#a)", "<a><b/></a>", root(CElem.class));
    check("<a><b/></a>/get(#b)", "<b/>", type(IterStep.class, "element(b)*"));
    check("<a><b/></a>/child::get(#b)", "<b/>", type(IterStep.class, "element(b)*"));
    check("<a><b/></a>/descendant-or-self::get(#b)", "<b/>", type(IterStep.class, "element(b)*"));
    check("<a><b/></a>/get(#c)", "", type(IterStep.class, "element(c)*"));
    check("<a><b/></a>/get(())", "", empty());

    check("<a><b/></a>/get((#b, 1))", "<b/>", exists(_UTIL_SELECT));
    check("<a><b/></a>/get((#c, 1))", "", exists(_UTIL_SELECT));

    check("<a><b/><c/></a>/get((#c, #b))", "<b/>\n<c/>",
        type(IterStep.class, "(element(c)|element(b))*"));
    check("let $names := (#c, #b) return <a><b/><c/></a>/get($names)", "<b/>\n<c/>",
        type(IterStep.class, "(element(c)|element(b))*"));

    check("<a><b/><c/></a>/(c | get(#b))", "<b/>\n<c/>",
        type(Union.class, "(element(c)|element(b))*"));
    check("<a><b/><c/></a>/(get(#c) | b)", "<b/>\n<c/>",
        type(Union.class, "(element(c)|element(b))*"));
    check("<a><b/><c/></a>/(get(xs:QName('c')))", "<c/>",
        type(IterStep.class, "element(c)*"));
    check("<a><b/><c/></a>/(get(xs:QName(<?_ c?>)))", "<c/>",
        exists(CmpSimpleG.class), exists(NODE_NAME));
    check("let $name := #b return <a><b/><c/></a>/(c | get($name))", "<b/>\n<c/>",
        type(Union.class, "(element(c)|element(b))*"));
  }

  /** Path extensions: type(). */
  @Test public void pathType() {
    query("<a><b/></a>/type(item())", "<b/>");
    query("<a><b/></a>/type(item()?)", "<b/>");
    query("<a><b/></a>/type(item()*)", "<b/>");
    query("<a><b/></a>/type(item()+)", "<b/>");
    query("<a><b/></a>/type(element())", "<b/>");
    query("<a><b/></a>/type(element()?)", "<b/>");
    query("<a><b/></a>/type(element()*)", "<b/>");
    query("<a><b/></a>/type(element()+)", "<b/>");
    query("<a><b/></a>/child::type(element())", "<b/>");
    query("<a><b/></a>/self::type(element())", "<a><b/></a>");
    query("<a><b/></a>/descendant-or-self::type(element())", "<a><b/></a>\n<b/>");
    query("<a><b/></a>/descendant-or-self::type(text())", "");
    query("<a><b/></a>/descendant-or-self::type(xs:integer)", "");
    query("<a><b/></a>/descendant-or-self::type(xs:integer?)", "");
    query("<a><b/></a>/descendant-or-self::type(xs:integer*)", "");
    query("<a><b/></a>/descendant-or-self::type(xs:integer+)", "");
    query("<a><b/></a>/descendant-or-self::type(empty-sequence())", "");
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
}
