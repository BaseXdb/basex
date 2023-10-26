package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.query.expr.constr.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * XQuery 4.0 tests.
 *
 * @author BaseX Team 2005-23, BSD License
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

    error("declare context value as xs:integer := (1, 2); .", INVTREAT_X_X_X);
    error("declare context value := 1; declare context value := 2; .", DUPLVALUE);
    error("declare context value := 1; declare context item := 2; .", DUPLVALUE);
    error("declare context item := 1; declare context item := 2; .", DUPLVALUE);
    error("declare context item := 1; declare context item := 2; .", DUPLVALUE);
  }

  /** Lookup operator. */
  @Test public void lookup() {
    query("map { } ? ''", "");
    query("map { '': 1 } ? ''", 1);

    query("let $m := map { '': 1 } return $m?''", 1);
    query("let $_ := '' return map { '': 1 } ? $_", 1);
    query("let $_ := '' let $m := map { '': 1 } return $m?$_", 1);

    query("declare variable $_ := 1; [ 9 ]?$_", 9);
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

    check("1 otherwise prof:void(2)", 1, root(Int.class));
    check("prof:void(1) otherwise 2", 2, root(Otherwise.class));
    check("prof:void(1) otherwise prof:void(2)", "", root(Otherwise.class));

    check("count(prof:void(1) otherwise prof:void(2))", 0, root(COUNT));
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
    check("() otherwise 1", 1, root(Int.class));
    check("1 otherwise ()", 1, root(Int.class));
    check("() otherwise <x/>", "<x/>", root(CElem.class));
    check("<x/> otherwise ()", "<x/>", root(CElem.class));

    check("(1, <_>2</_>[. = 3]) otherwise ()", 1, root(List.class));
    check("(2, <_>3</_>[. = 4]) otherwise <z/>", 2, root(List.class));

    check("(3, <_>4</_>)[. = 3] otherwise ()", 3, root(IterFilter.class));
    check("(4, <_>5</_>)[. = 4] otherwise <z/>", 4, root(Otherwise.class));

    check(VOID.args(1) + " otherwise 2", 2, root(Otherwise.class));
    check(VOID.args(2) + " otherwise " + VOID.args(3), "", root(Otherwise.class));

    check("<_>6</_>[. = 6] otherwise 7", "<_>6</_>", root(Otherwise.class));
  }

  /** Function item, arrow. */
  @Test public void fn() {
    query("fn() { }()", "");
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
    query("for key $k value $v in map { } return $k * $v", "");

    query("for key $k in map { 2: 3 } return $k", 2);
    query("for value $v in map { 2: 3 } return $v", 3);
    query("for key $k value $v in map { 2: 3 } return $k * $v", 6);

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

    check("for member $m in [ (3, 2), 1, () ] return sum($m)", "5\n1\n0", empty(_ARRAY_VALUES));
    check("for member $m in [ (3, 2), 1, () ] return count($m)", "2\n1\n0", exists(_ARRAY_VALUES));

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

    query("() =!> (function { })()", "");
    query("1 =!> (function { })()", "");
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
    query("1 =!> (map { 1: 'V' })()", "V");
    query("(1, 2) =!> (map { 1: 'W', 2: 'X' })()", "W\nX");
    query("let $map := map { 0: 'off', 1: 'on' } return 1 =!> $map()", "on");
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
    query("if(0) { }", "");
    query("if(1) { 2 }", 2);
    query("if(1) { 2 } else { 3 }", 2);
    query("if(0) { } else { 1 }", 1);
    query("if(0) { } else if(1) { 2 }", 2);
    query("if(0) { } else if(0) { } else { 1 }", 1);
    query("if(0) { } else if(0) { } else if(0) { } else if(0) { } else { 1 }", 1);

    error("if() { }", NOIF);
    error("if(0) { } else", WRONGCHAR_X_X);
    error("if(0) { } else ()", WRONGCHAR_X_X);
    error("if(0) { } else if() { }", NOIF);
    error("if(0) { } else if(0) then", WRONGCHAR_X_X);
    error("if(0) { } else if(0) { } else", WRONGCHAR_X_X);
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
    query("1_2__3_________4.5______________6e7________________________8", 1234.56e78);

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
    query("try { 1 + <_/> } catch * { $err:map?code }", "err:FORG0001");
    query("try { 1 + <_/> } catch * { $err:map?line-number }", 1);
    query("try { 1 + <_/> } catch * { $err:map?additional }", "");
    query("try { 1 + <_/> } catch * { $err:map?value }", "");
    query("try { 1 + <_/> } catch * { map:size($err:map) }", 5);

    query("try { error((), (), 1) } catch * { $err:map?value }", 1);
    query("try { error(xs:QName('a')) } catch * { $err:map?code }", "a");
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
}
