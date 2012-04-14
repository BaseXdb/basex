package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the function-arity() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFunctionArity extends QT3TestSet {

  /**
   * Attempts to evaluate the "function-arity" function with no arguments..
   */
  @org.junit.Test
  public void fnFunctionArity001() {
    final XQuery query = new XQuery(
      "fn:function-arity()",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "function-arity" function with arity zero..
   */
  @org.junit.Test
  public void fnFunctionArity002() {
    final XQuery query = new XQuery(
      "fn:function-arity#0",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to evaluate the "function-arity" function with two arguments..
   */
  @org.junit.Test
  public void fnFunctionArity003() {
    final XQuery query = new XQuery(
      "fn:function-arity( fn:dateTime#2, fn:dateTime#2 )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "function-arity" function with arity two..
   */
  @org.junit.Test
  public void fnFunctionArity004() {
    final XQuery query = new XQuery(
      "fn:function-arity#2",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Attempts to reference the "function-arity" function with arity one..
   */
  @org.junit.Test
  public void fnFunctionArity005() {
    final XQuery query = new XQuery(
      "exists(fn:function-arity#1)",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }

  /**
   * Evaluates the "function-arity" function with the argument set as follows: $func = () .
   */
  @org.junit.Test
  public void fnFunctionArity006() {
    final XQuery query = new XQuery(
      "fn:function-arity( () )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Evaluates the "function-arity" function with an argument of type xs:anyAtomicType..
   */
  @org.junit.Test
  public void fnFunctionArity007() {
    final XQuery query = new XQuery(
      "fn:function-arity( 1 )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Evaluates the "function-arity" function with an argument of type node()..
   */
  @org.junit.Test
  public void fnFunctionArity008() {
    final XQuery query = new XQuery(
      "fn:function-arity( fn:analyze-string((), \"unused\") )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Tests the type checking of the argument..
   */
  @org.junit.Test
  public void fnFunctionArity009() {
    final XQuery query = new XQuery(
      "fn:function-arity( (fn:concat#2, fn:concat#3) )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Tests the type checking of the argument..
   */
  @org.junit.Test
  public void fnFunctionArity011() {
    final XQuery query = new XQuery(
      "( fn:function-arity( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then fn:dateTime#2\n" +
      "                                 else 1 ),\n" +
      "              fn:function-arity( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then 1\n" +
      "                                 else fn:dateTime#2 ) )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Tests the type checking of the argument..
   */
  @org.junit.Test
  public void fnFunctionArity013() {
    final XQuery query = new XQuery(
      "( fn:function-arity( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then fn:dateTime#2\n" +
      "                                 else () ),\n" +
      "              fn:function-arity( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                 then ()\n" +
      "                                 else fn:dateTime#2 ) )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPTY0004")
    );
  }

  /**
   * Tests the return type of the "function-arity" function..
   */
  @org.junit.Test
  public void fnFunctionArity014() {
    final XQuery query = new XQuery(
      "fn:function-arity( fn:substring#2 )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertType("xs:integer")
    );
  }

  /**
   * Evaluates the "function-arity" function with the argument set as follows: $func = fn:dateTime#2 .
   */
  @org.junit.Test
  public void fnFunctionArity015() {
    final XQuery query = new XQuery(
      "fn:function-arity( fn:dateTime#2 )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Evaluates the "function-arity" function with the argument set as follows: $func = fn:concat#99 .
   */
  @org.junit.Test
  public void fnFunctionArity016() {
    final XQuery query = new XQuery(
      "fn:function-arity( fn:concat#99 )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "99")
    );
  }

  /**
   * Evaluates the "function-arity" function with the argument set as follows: $func = fn:concat#340282366920938463463374607431768211456.
   */
  @org.junit.Test
  public void fnFunctionArity017() {
    final XQuery query = new XQuery(
      "fn:function-arity( fn:concat#340282366920938463463374607431768211456 )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("FOAR0002")
      ||
        assertStringValue(false, "340282366920938463463374607431768211456")
      )
    );
  }

  /**
   * Evaluates the "function-arity" function with the argument set as follows: function($node){name($node)} .
   */
  @org.junit.Test
  public void fnFunctionArity018() {
    final XQuery query = new XQuery(
      "fn:function-arity( function($node){name($node)} )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Evaluates the "function-arity" function with the argument set as follows: function($arg1, $arg2){subsequence($arg1, $arg2, 1)} .
   */
  @org.junit.Test
  public void fnFunctionArity019() {
    final XQuery query = new XQuery(
      "fn:function-arity( function($arg1, $arg2)\n" +
      "                               { subsequence($arg1, $arg2, 1) } )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   * Evaluates the "function-arity" function with the argument refereing to a partial function application..
   */
  @org.junit.Test
  public void fnFunctionArity020() {
    final XQuery query = new XQuery(
      "let $initial := fn:substring(?, 1, 1) \n" +
      "            return fn:function-arity( $initial )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Evaluates the "function-arity" function with the argument refereing to a partial function application..
   */
  @org.junit.Test
  public void fnFunctionArity021() {
    final XQuery query = new XQuery(
      "fn:function-arity( math:pow(?, 10) )",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   * Evaluates the "function-arity" function with the argument refereing a global user-defined function..
   */
  @org.junit.Test
  public void fnFunctionArity022() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare function local:add($arg1, $arg2, $arg3)\n" +
      "        {\n" +
      "           $arg1 + $arg2 + $arg3\n" +
      "        };\n" +
      "\n" +
      "\tfn:function-arity( local:add#3 )\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   * Evaluates the "function-arity" function with the argument refereing a partial application of a global user-defined function..
   */
  @org.junit.Test
  public void fnFunctionArity023() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare function local:add($arg1, $arg2, $arg3)\n" +
      "        {\n" +
      "           $arg1 + $arg2 + $arg3\n" +
      "        };\n" +
      "\n" +
      "\tfn:function-arity( local:add(1, 2, ?) )\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1")
    );
  }
}
