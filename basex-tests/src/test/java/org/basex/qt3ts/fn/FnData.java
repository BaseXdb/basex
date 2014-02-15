package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the data() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnData extends QT3TestSet {

  /**
   *  A test whose essence is: `data()`. .
   */
  @org.junit.Test
  public void kDataFunc1() {
    xquery10();
    final XQuery query = new XQuery(
      "data()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `data(1, "wrong param")`. .
   */
  @org.junit.Test
  public void kDataFunc2() {
    final XQuery query = new XQuery(
      "data(1, \"wrong param\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A test whose essence is: `count(data((1, 2, 3, 4, 5))) eq 5`. .
   */
  @org.junit.Test
  public void kDataFunc3() {
    final XQuery query = new XQuery(
      "count(data((1, 2, 3, 4, 5))) eq 5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A test whose essence is: `empty(data( () ))`. .
   */
  @org.junit.Test
  public void kDataFunc4() {
    final XQuery query = new XQuery(
      "empty(data( () ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  fn:data() allows zero arguments in XP30/XQ30. .
   */
  @org.junit.Test
  public void k2DataFunc1() {
    final XQuery query = new XQuery(
      "1!data()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  fn:data() must be passed at most one argument. .
   */
  @org.junit.Test
  public void k2DataFunc2() {
    final XQuery query = new XQuery(
      "data(1, 2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  fn:data() allows zero arguments in XP30/XQ30. .
   */
  @org.junit.Test
  public void k2DataFunc3() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tlet $x := <e><f>1</f></e>\n" +
      "      \treturn $x/data()\n" +
      "      \t\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "1")
      &&
        assertType("xs:untypedAtomic")
      )
    );
  }

  /**
   *  fn:data() allows zero arguments in XP30/XQ30. No context item.
   */
  @org.junit.Test
  public void k2DataFunc4() {
    final XQuery query = new XQuery(
      "\n" +
      "      \tdata()\n" +
      "      \t\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  fn:data() allows zero arguments in XP30/XQ30. Context item is a function item.
   */
  @org.junit.Test
  public void k2DataFunc5() {
    final XQuery query = new XQuery(
      "\n" +
      "      \t(1, data#0)[data()]\n" +
      "      \t\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOTY0013")
    );
  }

  /**
   *  test fn:data on fn:error Author: Tim Mills .
   */
  @org.junit.Test
  public void cbclData001() {
    final XQuery query = new XQuery(
      "fn:data(fn:error()) instance of xs:integer",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(true)
      ||
        error("FOER0000")
      )
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:double(lower bound) .
   */
  @org.junit.Test
  public void fnDatadbl1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:double(\"-1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:double(mid range) .
   */
  @org.junit.Test
  public void fnDatadbl1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:double(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:double(upper bound) .
   */
  @org.junit.Test
  public void fnDatadbl1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:double(\"1.7976931348623157E308\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1.7976931348623157E308")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:decimal(lower bound) .
   */
  @org.junit.Test
  public void fnDatadec1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:decimal(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:decimal(mid range) .
   */
  @org.junit.Test
  public void fnDatadec1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:decimal(\"617375191608514839\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("617375191608514839")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:decimal(upper bound) .
   */
  @org.junit.Test
  public void fnDatadec1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:decimal(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:float(lower bound) .
   */
  @org.junit.Test
  public void fnDataflt1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:float(\"-3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-3.4028235E38")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:float(mid range) .
   */
  @org.junit.Test
  public void fnDataflt1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:float(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:float(upper bound) .
   */
  @org.junit.Test
  public void fnDataflt1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:float(\"3.4028235E38\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3.4028235E38")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:int(lower bound) .
   */
  @org.junit.Test
  public void fnDataint1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:int(\"-2147483648\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-2147483648")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:int(mid range) .
   */
  @org.junit.Test
  public void fnDataint1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:int(\"-1873914410\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1873914410")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:int(upper bound) .
   */
  @org.junit.Test
  public void fnDataint1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:int(\"2147483647\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("2147483647")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:integer(lower bound) .
   */
  @org.junit.Test
  public void fnDataintg1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:integer(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:integer(mid range) .
   */
  @org.junit.Test
  public void fnDataintg1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:integer(\"830993497117024304\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("830993497117024304")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:integer(upper bound) .
   */
  @org.junit.Test
  public void fnDataintg1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:integer(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:long(lower bound) .
   */
  @org.junit.Test
  public void fnDatalng1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:long(\"-92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-92233720368547758")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:long(mid range) .
   */
  @org.junit.Test
  public void fnDatalng1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:long(\"-47175562203048468\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-47175562203048468")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:long(upper bound) .
   */
  @org.junit.Test
  public void fnDatalng1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:long(\"92233720368547758\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("92233720368547758")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:negativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDatanint1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:negativeInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:negativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnDatanint1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:negativeInteger(\"-297014075999096793\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-297014075999096793")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:negativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDatanint1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:negativeInteger(\"-1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-1")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:nonNegativeInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDatanni1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:nonNegativeInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:nonNegativeInteger(mid range) .
   */
  @org.junit.Test
  public void fnDatanni1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:nonNegativeInteger(\"303884545991464527\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("303884545991464527")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:nonNegativeInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDatanni1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:nonNegativeInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:nonPositiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDatanpi1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:nonPositiveInteger(\"-999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-999999999999999999")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:nonPositiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnDatanpi1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:nonPositiveInteger(\"-475688437271870490\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-475688437271870490")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:nonPositiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDatanpi1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:nonPositiveInteger(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:positiveInteger(lower bound) .
   */
  @org.junit.Test
  public void fnDatapint1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:positiveInteger(\"1\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:positiveInteger(mid range) .
   */
  @org.junit.Test
  public void fnDatapint1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:positiveInteger(\"52704602390610033\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("52704602390610033")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:positiveInteger(upper bound) .
   */
  @org.junit.Test
  public void fnDatapint1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:positiveInteger(\"999999999999999999\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("999999999999999999")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:short(lower bound) .
   */
  @org.junit.Test
  public void fnDatasht1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:short(\"-32768\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-32768")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:short(mid range) .
   */
  @org.junit.Test
  public void fnDatasht1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:short(\"-5324\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("-5324")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:short(upper bound) .
   */
  @org.junit.Test
  public void fnDatasht1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:short(\"32767\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("32767")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:unsignedLong(lower bound) .
   */
  @org.junit.Test
  public void fnDataulng1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:unsignedLong(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:unsignedLong(mid range) .
   */
  @org.junit.Test
  public void fnDataulng1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:unsignedLong(\"130747108607674654\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("130747108607674654")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:unsignedLong(upper bound) .
   */
  @org.junit.Test
  public void fnDataulng1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:unsignedLong(\"184467440737095516\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("184467440737095516")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:unsignedShort(lower bound) .
   */
  @org.junit.Test
  public void fnDatausht1args1() {
    final XQuery query = new XQuery(
      "fn:data((xs:unsignedShort(\"0\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:unsignedShort(mid range) .
   */
  @org.junit.Test
  public void fnDatausht1args2() {
    final XQuery query = new XQuery(
      "fn:data((xs:unsignedShort(\"44633\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("44633")
    );
  }

  /**
   *  Evaluates The "data" function with the arguments set as follows: $arg = xs:unsignedShort(upper bound) .
   */
  @org.junit.Test
  public void fnDatausht1args3() {
    final XQuery query = new XQuery(
      "fn:data((xs:unsignedShort(\"65535\")))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("65535")
    );
  }
}
