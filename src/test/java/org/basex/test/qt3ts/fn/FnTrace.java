package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the trace() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnTrace extends QT3TestSet {

  /**
   *  A test whose essence is: `trace()`. .
   */
  @org.junit.Test
  public void kTraceFunc1() {
    final XQuery query = new XQuery(
      "trace()",
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
   *  A test whose essence is: `trace(.)`. .
   */
  @org.junit.Test
  public void kTraceFunc2() {
    final XQuery query = new XQuery(
      "trace(.)",
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
   *  A test whose essence is: `trace(., "message", "wrong parameter")`. .
   */
  @org.junit.Test
  public void kTraceFunc3() {
    final XQuery query = new XQuery(
      "trace(., \"message\", \"wrong parameter\")",
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
   *  A test whose essence is: `trace("a string", "trace message")`. .
   */
  @org.junit.Test
  public void kTraceFunc4() {
    final XQuery query = new XQuery(
      "trace(\"a string\", \"trace message\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a string")
    );
  }

  /**
   *  A test whose essence is: `empty(trace((), "trace message"))`. .
   */
  @org.junit.Test
  public void kTraceFunc5() {
    final XQuery query = new XQuery(
      "empty(trace((), \"trace message\"))",
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
        error("XPST0005")
      )
    );
  }

  /**
   *  A test whose essence is: `empty(trace(error(), "trace message"))`. .
   */
  @org.junit.Test
  public void kTraceFunc6() {
    final XQuery query = new XQuery(
      "empty(trace(error(), \"trace message\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FOER0000")
    );
  }

  /**
   *  Evaluation of "fn:trace" function with wrong arity. .
   */
  @org.junit.Test
  public void fnTrace1() {
    final XQuery query = new XQuery(
      "fn:trace()",
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
   *  Simple call of "fn:trace" function used with string manipulation (fn:concat). .
   */
  @org.junit.Test
  public void fnTrace10() {
    final XQuery query = new XQuery(
      "for $var in (\"aa\",\"bb\",\"cc\",\"dd\",\"ee\") return fn:trace(fn:concat($var,$var) ,\"The Value of concat($var,$var) is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "aaaa bbbb cccc dddd eeee")
    );
  }

  /**
   *  Simple call of "fn:trace" function used numbers manipulation queried from an xml file. .
   */
  @org.junit.Test
  public void fnTrace11() {
    final XQuery query = new XQuery(
      "for $var in (/works//hours) return fn:trace(($var div 2) ,\"The Value of hours div/2 is: \")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "20 35 10 40 10 20 10 15 6 20 40 10 10 10 20 40")
    );
  }

  /**
   *  Simple call of "fn:trace" function used numbers manipulation queried from an xml file and the entire query is given as argument to the function. .
   */
  @org.junit.Test
  public void fnTrace12() {
    final XQuery query = new XQuery(
      "fn:trace((for $var in (/works//hours) return $var + $var) ,\"The Value of the given expression is: \")",
      ctx);
    try {
      query.context(node(file("docs/works-mod.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "80 140 40 160 40 80 40 60 24 80 160 40 40 40 80 160")
    );
  }

  /**
   *  Simple call of "fn:trace" function used in a math expression involving the "avg" function. .
   */
  @org.junit.Test
  public void fnTrace13() {
    final XQuery query = new XQuery(
      "fn:trace((fn:avg((1,3,3,4,5)) * 2) ,\"The Value of 'fn:avg((1,3,3,4,5)) * 2' is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "6.4")
    );
  }

  /**
   *  Simple call of "fn:trace" function used in expression involving casting (floats to integer). .
   */
  @org.junit.Test
  public void fnTrace14() {
    final XQuery query = new XQuery(
      "fn:trace((for $var in (1.1,2.2,3.3,4.4,5.5) return xs:integer($var)) ,\"The Value of 'for $var in (1.1,2.2,3.3,4.4,5.5) return xs:float($var)' is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4 5")
    );
  }

  /**
   *  Simple call of "fn:trace" function that uses another fn-trace as argument. .
   */
  @org.junit.Test
  public void fnTrace15() {
    final XQuery query = new XQuery(
      "fn:trace((fn:trace((2+2),\"The value of '2 + 2' is:\" )) ,\"The Value of 'fn:trace(2+2)' is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4")
    );
  }

  /**
   *  Simple call of "fn:trace" function used in a math expression involving the "abs" function. .
   */
  @org.junit.Test
  public void fnTrace16() {
    final XQuery query = new XQuery(
      "fn:trace((fn:count((1,2,-3,-4,5)) * 2) ,\"The Value of 'fn:count(1,2,-3,-4,5)) * 2' is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "10")
    );
  }

  /**
   *  Simple call of "fn:trace" function used in a math expression involving boolean operations ("and" and fn:true()). .
   */
  @org.junit.Test
  public void fnTrace17() {
    final XQuery query = new XQuery(
      "fn:trace((for $var in (fn:true(),fn:false(),fn:true()) return $var and fn:true()) ,\"The value of 'for $var in (fn:true(),fn:false(),fn:true() return $var and fn:true()' is:\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true false true")
    );
  }

  /**
   *  Simple call of "fn:trace" function used in a math expression involving boolean operations ("or" and fn:true()). .
   */
  @org.junit.Test
  public void fnTrace18() {
    final XQuery query = new XQuery(
      "fn:trace((for $var in (fn:true(),fn:false(),fn:true()) return $var or fn:true()) ,\"The value of 'for $var in (fn:true(),fn:false(),fn:true() return $var or fn:true()' is:\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true true")
    );
  }

  /**
   *  Simple call of "fn:trace" function where the first argument is the empty sequence. Uses fn:count to maybe avoid empty file. .
   */
  @org.junit.Test
  public void fnTrace19() {
    final XQuery query = new XQuery(
      "fn:count(fn:trace(() ,\"The value of the empty sequence is:\"))",
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
        assertStringValue(false, "0")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Simple call of "fn:trace" function with integer value. .
   */
  @org.junit.Test
  public void fnTrace2() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $var := xs:integer(\"123\") \n" +
      "         return fn:trace($var,\"The Value of $var is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123")
    );
  }

  /**
   *  Simple call of "fn:trace" function where the first argument is the zero length string. Uses fn:count to maybe avoid empty file. .
   */
  @org.junit.Test
  public void fnTrace20() {
    final XQuery query = new XQuery(
      "fn:count(fn:trace(\"\" ,\"The value of the zero length string is:\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  Simple call of "fn:trace" function involving string manipulations with upper/lower case functions. .
   */
  @org.junit.Test
  public void fnTrace21() {
    final XQuery query = new XQuery(
      "fn:trace((for $var in (fn:upper-case(\"a\"),fn:lower-case(\"B\")) return (fn:lower-case($var),fn:upper-case($var))) ,\"The value of the complex expression on the other argument is:\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "a A b B")
    );
  }

  /**
   *  Simple call of "fn:trace" function used with an addition operation. .
   */
  @org.junit.Test
  public void fnTrace3() {
    final XQuery query = new XQuery(
      "for $var in (1,2,3,4,5) return fn:trace($var + 1,\"The Value of $var + 1 is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2 3 4 5 6")
    );
  }

  /**
   *  Simple call of "fn:trace" function used with a subtraction operation. .
   */
  @org.junit.Test
  public void fnTrace4() {
    final XQuery query = new XQuery(
      "for $var in (2,3,4,5) return fn:trace($var - 1,\"The Value of $var - 1 is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4")
    );
  }

  /**
   *  Simple call of "fn:trace" function used with a multiplication operation. .
   */
  @org.junit.Test
  public void fnTrace5() {
    final XQuery query = new XQuery(
      "for $var in (2,3,4,5) return fn:trace($var * 2 ,\"The Value of $var * 2 is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4 6 8 10")
    );
  }

  /**
   *  Simple call of "fn:trace" function used with a division (div operator) operation. .
   */
  @org.junit.Test
  public void fnTrace6() {
    final XQuery query = new XQuery(
      "for $var in (2,4,6,8) return fn:trace($var div 2 ,\"The Value of $var div 2 is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4")
    );
  }

  /**
   *  Simple call of "fn:trace" function used with a division (idiv operator) operation. .
   */
  @org.junit.Test
  public void fnTrace7() {
    final XQuery query = new XQuery(
      "for $var in (2,4,6,8) return fn:trace($var idiv 2 ,\"The Value of $var idiv 2 is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2 3 4")
    );
  }

  /**
   *  Simple call of "fn:trace" function used with a modulus operation. .
   */
  @org.junit.Test
  public void fnTrace8() {
    final XQuery query = new XQuery(
      "for $var in (2,4,6,8) return fn:trace($var mod 2 ,\"The Value of $var mod 2 is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0 0 0 0")
    );
  }

  /**
   *  Simple call of "fn:trace" function used with two variables, where the second one uses the first one in a more complex math expression. .
   */
  @org.junit.Test
  public void fnTrace9() {
    final XQuery query = new XQuery(
      "for $var1 in (2,4,6,8), $var2 in (3 + $var1) - (4 * $var1) \n" +
      "        return fn:trace($var1 + $var2 ,\"The Value of $var 1 + $var2 is: \")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-1 -5 -9 -13")
    );
  }
}
