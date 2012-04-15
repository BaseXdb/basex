package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnNot extends QT3TestSet {

  /**
   * K-NotFunc-1  A test whose essence is: `not()`. .
   */
  @org.junit.Test
  public void kNotFunc1() {
    final XQuery query = new XQuery(
      "not()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * K-NotFunc-10  fn:not() combined with fn:boolean(). .
   */
  @org.junit.Test
  public void kNotFunc10() {
    final XQuery query = new XQuery(
      "not(fn:boolean((1, 2, 3, current-time())[1] treat as xs:integer)) eq false()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * K-NotFunc-2  A test whose essence is: `not(1, 2, 3, 4, 5, 6)`. .
   */
  @org.junit.Test
  public void kNotFunc2() {
    final XQuery query = new XQuery(
      "not(1, 2, 3, 4, 5, 6)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * K-NotFunc-3  A test whose essence is: `not(false() and false())`. .
   */
  @org.junit.Test
  public void kNotFunc3() {
    final XQuery query = new XQuery(
      "not(false() and false())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * K-NotFunc-4  A test whose essence is: `not(not(true()))`. .
   */
  @org.junit.Test
  public void kNotFunc4() {
    final XQuery query = new XQuery(
      "not(not(true()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * K-NotFunc-5  A test whose essence is: `not(false())`. .
   */
  @org.junit.Test
  public void kNotFunc5() {
    final XQuery query = new XQuery(
      "not(false())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * K-NotFunc-6  A test whose essence is: `not(0)`. .
   */
  @org.junit.Test
  public void kNotFunc6() {
    final XQuery query = new XQuery(
      "not(0)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * K-NotFunc-7  A test whose essence is: `not(())`. .
   */
  @org.junit.Test
  public void kNotFunc7() {
    final XQuery query = new XQuery(
      "not(())",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * K-NotFunc-8  A test whose essence is: `not(xs:anyURI(""))`. .
   */
  @org.junit.Test
  public void kNotFunc8() {
    final XQuery query = new XQuery(
      "not(xs:anyURI(\"\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * K-NotFunc-9  A test whose essence is: `not(not(xs:anyURI("example.com/")))`. .
   */
  @org.junit.Test
  public void kNotFunc9() {
    final XQuery query = new XQuery(
      "not(not(xs:anyURI(\"example.com/\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void fnNot1() {
    final XQuery query = new XQuery(
      "fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * comparison expression involving the "ge" operator..
   */
  @org.junit.Test
  public void fnNot10() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") ge fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * comparison expression involving the "=" operator..
   */
  @org.junit.Test
  public void fnNot11() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") = fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * comparison expression involving the "!=" operator..
   */
  @org.junit.Test
  public void fnNot12() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") != fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * comparison expression involving the "<" operator..
   */
  @org.junit.Test
  public void fnNot13() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") < fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * comparison expression involving the "<=" operator..
   */
  @org.junit.Test
  public void fnNot14() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") <= fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * comparison expression involving the ">" operator..
   */
  @org.junit.Test
  public void fnNot15() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") > fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * comparison expression involving the ">=" operator..
   */
  @org.junit.Test
  public void fnNot16() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") >= fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * to an "xs:boolean" function..
   */
  @org.junit.Test
  public void fnNot17() {
    final XQuery query = new XQuery(
      "xs:boolean(fn:not(\"true\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * to an "fn:string" function..
   */
  @org.junit.Test
  public void fnNot18() {
    final XQuery query = new XQuery(
      "fn:string(fn:not(\"true\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("'false'")
    );
  }

  /**
   * an "fn:concat" function..
   */
  @org.junit.Test
  public void fnNot19() {
    final XQuery query = new XQuery(
      "fn:concat(xs:string(fn:not(\"true\")),xs:string(fn:not(\"true\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("'falsefalse'")
    );
  }

  /**
   * fn:not function..
   */
  @org.junit.Test
  public void fnNot2() {
    final XQuery query = new XQuery(
      "fn:not(\"fn:not()\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * an "fn:contains" function..
   */
  @org.junit.Test
  public void fnNot20() {
    final XQuery query = new XQuery(
      "fn:contains(xs:string(fn:not(\"true\")),xs:string(fn:not(\"true\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * to an "fn:string-length" function..
   */
  @org.junit.Test
  public void fnNot21() {
    final XQuery query = new XQuery(
      "fn:string-length(xs:string(fn:not(\"true\")))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("5")
    );
  }

  /**
.
   */
  @org.junit.Test
  public void fnNot24() {
    final XQuery query = new XQuery(
      "not(xs:double('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void fnNot25() {
    final XQuery query = new XQuery(
      "not(xs:float('NaN'))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void fnNot26() {
    final XQuery query = new XQuery(
      "not(\"\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * boolean.
   */
  @org.junit.Test
  public void fnNot27() {
    final XQuery query = new XQuery(
      "not((true(), false()))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("FORG0006")
    );
  }

  /**
   * expression involving the "and" operator..
   */
  @org.junit.Test
  public void fnNot3() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") and fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * expression involving the "or" operator..
   */
  @org.junit.Test
  public void fnNot4() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") or fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * expression involving the "eq" operator..
   */
  @org.junit.Test
  public void fnNot5() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") eq fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * expression involving the "ne" operator..
   */
  @org.junit.Test
  public void fnNot6() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") ne fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * expression involving the "lt" operator..
   */
  @org.junit.Test
  public void fnNot7() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") lt fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * expression involving the "le" operator..
   */
  @org.junit.Test
  public void fnNot8() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") le fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * expression involving the "gt" operator..
   */
  @org.junit.Test
  public void fnNot9() {
    final XQuery query = new XQuery(
      "fn:not(\"true\") gt fn:not(\"true\")",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notdbl1args-1 The "not" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnNotdbl1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:double(\"-1.7976931348623157E308\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notdbl1args-2 The "not" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnNotdbl1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:double(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * notdbl1args-3 The "not" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnNotdbl1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:double(\"1.7976931348623157E308\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notdec1args-1 The "not" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnNotdec1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:decimal(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notdec1args-2 The "not" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnNotdec1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:decimal(\"617375191608514839\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notdec1args-3 The "not" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnNotdec1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:decimal(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notflt1args-1 The "not" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnNotflt1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:float(\"-3.4028235E38\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notflt1args-2 The "not" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnNotflt1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:float(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * notflt1args-3 The "not" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnNotflt1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:float(\"3.4028235E38\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notint1args-1 The "not" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnNotint1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:int(\"-2147483648\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notint1args-2 The "not" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnNotint1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:int(\"-1873914410\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notint1args-3 The "not" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnNotint1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:int(\"2147483647\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notintg1args-1 The "not" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnNotintg1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:integer(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notintg1args-2 The "not" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnNotintg1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:integer(\"830993497117024304\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notintg1args-3 The "not" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnNotintg1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:integer(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notlng1args-1 The "not" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnNotlng1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:long(\"-92233720368547758\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notlng1args-2 The "not" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnNotlng1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:long(\"-47175562203048468\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notlng1args-3 The "not" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnNotlng1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:long(\"92233720368547758\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notnint1args-1 The "not" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnNotnint1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:negativeInteger(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notnint1args-2 The "not" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnNotnint1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:negativeInteger(\"-297014075999096793\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notnint1args-3 The "not" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnNotnint1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:negativeInteger(\"-1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notnni1args-1 The "not" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnNotnni1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:nonNegativeInteger(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * notnni1args-2 The "not" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnNotnni1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:nonNegativeInteger(\"303884545991464527\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notnni1args-3 The "not" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnNotnni1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:nonNegativeInteger(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notnpi1args-1 The "not" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnNotnpi1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:nonPositiveInteger(\"-999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notnpi1args-2 The "not" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnNotnpi1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:nonPositiveInteger(\"-475688437271870490\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notnpi1args-3 The "not" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnNotnpi1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:nonPositiveInteger(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * notpint1args-1 The "not" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnNotpint1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:positiveInteger(\"1\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notpint1args-2 The "not" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnNotpint1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:positiveInteger(\"52704602390610033\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notpint1args-3 The "not" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnNotpint1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:positiveInteger(\"999999999999999999\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notsht1args-1 The "not" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnNotsht1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:short(\"-32768\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notsht1args-2 The "not" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnNotsht1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:short(\"-5324\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notsht1args-3 The "not" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnNotsht1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:short(\"32767\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notulng1args-1 The "not" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnNotulng1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:unsignedLong(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * notulng1args-2 The "not" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnNotulng1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:unsignedLong(\"130747108607674654\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notulng1args-3 The "not" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnNotulng1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:unsignedLong(\"184467440737095516\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notusht1args-1 The "not" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnNotusht1args1() {
    final XQuery query = new XQuery(
      "fn:not(xs:unsignedShort(\"0\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * notusht1args-2 The "not" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnNotusht1args2() {
    final XQuery query = new XQuery(
      "fn:not(xs:unsignedShort(\"44633\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }

  /**
   * notusht1args-3 The "not" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnNotusht1args3() {
    final XQuery query = new XQuery(
      "fn:not(xs:unsignedShort(\"65535\"))",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(false)
    );
  }
}
