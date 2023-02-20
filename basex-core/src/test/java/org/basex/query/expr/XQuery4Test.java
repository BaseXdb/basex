package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.ast.*;
import org.basex.query.value.item.*;
import org.junit.jupiter.api.*;

/**
 * XQuery 4.0 tests.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class XQuery4Test extends QueryPlanTest {
  /** Version declaration. */
  @Test public void version40() {
    query("xquery version '1.0'; ()", "");
    query("xquery version '3.0'; ()", "");
    query("xquery version '3.1'; ()", "");
    query("xquery version '4.0'; ()", "");
    error("xquery version '0.0'; ()", XQUERYVER_X);
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
  }

  /** Function item, arrow. */
  @Test public void functionItemArrow() {
    query("->() { }()", "");
    query("->($a) { $a }(())", "");
    query("->($a) { $a }(1)", 1);
    query("->($a) { $a }(1 to 2)", "1\n2");
    query("->($a, $b) { $a + $b }(1, 2)", 3);
    query("sum((1 to 6) ! ->($a) { $a * $a }(.))", 91);
    query("sum(for $i in 1 to 6  return ->($a) { $a * $a }($i))", 91);

    query("- ->(){ 1 }()", -1);
    query("--->(){ 1 }()", 1);
    query("1-->(){ 2 }()", -1);
    query("1--->(){ 2 }()", 3);
  }

  /** Function item, no parameter list. */
  @Test public void functionContextItem() {
    query("function { . + 1 }(1)", 2);
    query("-> { . + 1 }(1)", 2);
    query("-> { . + . }(1)", 2);
    query("sum((1 to 6) ! -> { . * . }(.))", 91);
    query("sum(for $i in 1 to 6  return -> { . * . }($i))", 91);

    query("- ->{ . }(1)", -1);
    query("--->{ . }(1)", 1);

    error("-> { . }(())", INVPROMOTE_X_X_X);
    error("-> { . }(1 to 5)", INVPROMOTE_X_X_X);

    error("function() { . + $i }", VARUNDEF_X);
    error("-> { . + $i }", VARUNDEF_X);
  }

  /** Thin arrow operator. */
  @Test public void thinArrow() {
    query("() -> { }", "");
    query("1 -> { }", "");
    query("() -> { . }", "");
    query("1 -> { . }", 1);
    query("(1, 2) -> { . }", "1\n2");
    query("0 -> { 1, 2 }", "1\n2");
    query("(0 to 5) -> { . + 1 }", "1\n2\n3\n4\n5\n6");

    query("'abc' -> upper-case() -> tokenize('\\s+')", "ABC");
    query("(1, 4, 9, 16, 25, 36) -> math:sqrt() -> { . + 1 } => sum()", 27);

    query("('$' -> concat(?))('x')", "$x");
    query("'$' -> concat('x')", "$x");

    final String eqname = "Q{http://www.w3.org/2005/xpath-functions}";
    query("'xyz' -> " + eqname + "contains('x')", true);
    query("('a', 'b') -> (" + eqname + "contains('abc', ?))()", "true\ntrue");

    query("('no', 'yes') -> identity()", "no\nyes");
    query("('no', 'yes') -> identity() => identity()", "no\nyes");
    query("('no', 'yes') => identity() -> identity()", "no\nyes");

    query("(1 to 9) -> count() => count()", 9);
    query("(1 to 9) => count() -> count()", 1);

    query("2 > 3 -> { 1 }", true);
    query("1-->{.}(2)", -1);
    query("1 - -> { . }(2)", -1);
    query("1--->{.}(2)", 3);
    query("1 - - -> { . }(3)", 4);
    query("-5 -> abs()", 5);
    query("(-6) -> abs()", 6);

    query("'abc' -> ((starts-with#2, ends-with#2) => head())('a')", true);
    query("'abc' -> ((starts-with#2, ends-with#2) => tail())('a')", false);

    query("(-5 to 0) -> (abs#1)() => sum()", 15);
    query("let $x := abs#1 return (-5 to 0) -> $x() => sum()", 15);
    query("for $x in abs#1 return (-5 to 0) -> $x() => sum()", 15);
    query("declare variable $ABS := abs#1; (-5 to 0) -> $ABS() => sum()", 15);

    query("1 -> ([ 'A' ])()", "A");
    query("1 -> (array { 'B' })()", "B");
    query("(1, 2) -> (array { 'C', 'D' })()", "C\nD");
    query("1 -> (map { 1: 'V' })()", "V");
    query("(1, 2) -> (map { 1: 'W', 2: 'X' })()", "W\nX");
    query("let $map := map { 0: 'off', 1: 'on' } return 1 -> $map()", "on");
    query("(1, 2) -> ([ 3[2], 1[0] ])()", "");

    query("256 ! 2 -> xs:byte()", 2);
    query("(256 ! 2) -> xs:byte()", 2);
    query("256 ! (2 -> xs:byte())", 2);

    query("'Jemand musste Josef K. verleumdet haben.'"
        + "=> tokenize() -> string-length() -> { . + 1 } => sum()",
        41);
    query("'Happy families are all alike; every unhappy family is unhappy in its own way.'"
      + "=> tokenize()"
      + "-> { upper-case(substring(., 1, 1)) || lower-case(substring(., 2)) }"
      + "=> string-join(' ')",
      "Happy Families Are All Alike; Every Unhappy Family Is Unhappy In Its Own Way.");

    error("2 ! 256 -> xs:byte()", FUNCCAST_X_X_X);
    error("1 -> if()", RESERVED_X);
    error("0 -> unknown()", WHICHFUNC_X);
    error("0 -> unknown(?)", WHICHFUNC_X);
    error("0 -> local:unknown()", WHICHFUNC_X);
    error("0 -> local:unknown(?)", WHICHFUNC_X);
    error("0 -> Q{}unknown()", WHICHFUNC_X);
    error("0 -> Q{}unknown(?)", WHICHFUNC_X);
    error("let $_ := 0 return 0 -> $_()", INVFUNCITEM_X_X);
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
        "x", exists(If.class));
    check("(1 to 6) ! (switch(.) case 6 to 8 return 'x' case 6 to 8 return '' default return ())",
        "x", exists(If.class));
  }
}
