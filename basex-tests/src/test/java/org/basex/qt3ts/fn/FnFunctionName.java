package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the function-name() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnFunctionName extends QT3TestSet {

  /**
   * Attempts to evaluate the "function-name" function with no arguments..
   */
  @org.junit.Test
  public void fnFunctionName001() {
    final XQuery query = new XQuery(
      "fn:function-name()",
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
   * Attempts to reference the "function-name" function with arity zero..
   */
  @org.junit.Test
  public void fnFunctionName002() {
    final XQuery query = new XQuery(
      "fn:function-name#0",
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
   * Attempts to evaluate the "function-name" function with two arguments..
   */
  @org.junit.Test
  public void fnFunctionName003() {
    final XQuery query = new XQuery(
      "fn:function-name( fn:dateTime#2, fn:dateTime#2 )",
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
   * Attempts to reference the "function-name" function with arity two..
   */
  @org.junit.Test
  public void fnFunctionName004() {
    final XQuery query = new XQuery(
      "fn:function-name#2",
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
   * Attempts to reference the "function-name" function with arity one..
   */
  @org.junit.Test
  public void fnFunctionName005() {
    final XQuery query = new XQuery(
      "exists(fn:function-name#1)",
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
   * Evaluates the "function-name" function with the argument set as follows: $func = () .
   */
  @org.junit.Test
  public void fnFunctionName006() {
    final XQuery query = new XQuery(
      "fn:function-name( () )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Evaluates the "function-name" function with an argument of type xs:anyAtomicType..
   */
  @org.junit.Test
  public void fnFunctionName007() {
    final XQuery query = new XQuery(
      "fn:function-name( 1 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Evaluates the "function-name" function with an argument of type node()..
   */
  @org.junit.Test
  public void fnFunctionName008() {
    final XQuery query = new XQuery(
      "fn:function-name( fn:analyze-string((), \"unused\") )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Tests the type checking of the argument..
   */
  @org.junit.Test
  public void fnFunctionName009() {
    final XQuery query = new XQuery(
      "fn:function-name( (fn:dateTime#2, fn:dateTime#2) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Tests the type checking of the argument..
   */
  @org.junit.Test
  public void fnFunctionName011() {
    final XQuery query = new XQuery(
      "( fn:function-name( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                then fn:dateTime#2\n" +
      "                                else 1 ),\n" +
      "              fn:function-name( if (current-date() eq xs:date('1900-01-01'))\n" +
      "                                then 1\n" +
      "                                else fn:dateTime#2 ) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * Tests the return type of the "function-name" function..
   */
  @org.junit.Test
  public void fnFunctionName012() {
    final XQuery query = new XQuery(
      "fn:function-name( fn:substring#2 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertType("xs:QName")
    );
  }

  /**
   * Tests the return type of the "function-name" function..
   */
  @org.junit.Test
  public void fnFunctionName013() {
    final XQuery query = new XQuery(
      "fn:function-name( fn:substring(?, 1) )",
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
        assertType("empty-sequence()")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   * Tests the return type of the "function-name" function..
   */
  @org.junit.Test
  public void fnFunctionName014() {
    final XQuery query = new XQuery(
      "fn:function-name( function($node){count($node/*)} )",
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
        assertType("empty-sequence()")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   * Evaluates the "function-name" function with the argument set as follows: $func = dateTime#2 .
   */
  @org.junit.Test
  public void fnFunctionName015() {
    final XQuery query = new XQuery(
      "fn:function-name( dateTime#2 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("fn:QName(\"http://www.w3.org/2005/xpath-functions\", \n                              \"fn:dateTime\")")
    );
  }

  /**
   * Evaluates the "function-name" function with the argument set as follows: $func = math:pow#2 .
   */
  @org.junit.Test
  public void fnFunctionName016() {
    final XQuery query = new XQuery(
      "fn:function-name( math:pow#2 )",
      ctx);
    try {
      query.namespace("math", "http://www.w3.org/2005/xpath-functions/math");
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("fn:QName(\"http://www.w3.org/2005/xpath-functions/math\", \n                              \"math:pow\")")
    );
  }

  /**
   * Evaluates the "function-name" function with the argument set as follows: $func = concat#99 .
   */
  @org.junit.Test
  public void fnFunctionName017() {
    final XQuery query = new XQuery(
      "fn:function-name( concat#99 )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("fn:QName(\"http://www.w3.org/2005/xpath-functions\", \n                              \"fn:concat\")")
    );
  }

  /**
   * Evaluates the "function-name" function with the argument set as follows: $func = concat#340282366920938463463374607431768211456.
   */
  @org.junit.Test
  public void fnFunctionName018() {
    final XQuery query = new XQuery(
      "fn:function-name( concat#340282366920938463463374607431768211456 )",
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
        error("FOAR0002")
      ||
        assertEq("fn:QName(\"http://www.w3.org/2005/xpath-functions\", \n                              \"fn:concat\")")
      )
    );
  }

  /**
   * Evaluates the "function-name" function with the argument set as follows: function($node){name($node)} .
   */
  @org.junit.Test
  public void fnFunctionName019() {
    final XQuery query = new XQuery(
      "fn:function-name( function($node){name($node)} )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluates the "function-name" function with the argument set as follows: function($arg1, $arg2){subsequence($arg1, $arg2, 1)} .
   */
  @org.junit.Test
  public void fnFunctionName020() {
    final XQuery query = new XQuery(
      "fn:function-name( function($arg1, $arg2)\n" +
      "                               { subsequence($arg1, $arg2, 1) } )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluates the "function-name" function with the argument refereing to a partial function application..
   */
  @org.junit.Test
  public void fnFunctionName021() {
    final XQuery query = new XQuery(
      "let $initial := fn:substring(?, 1, 1) \n" +
      "            return fn:function-name( $initial )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   * Evaluates the "function-name" function with the argument refereing a global user-defined function..
   */
  @org.junit.Test
  public void fnFunctionName022() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare function local:add($arg1, $arg2, $arg3)\n" +
      "        {\n" +
      "           $arg1 + $arg2 + $arg3\n" +
      "        };\n" +
      "\n" +
      "\tfn:function-name( local:add#3 )\n" +
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
      assertEq("fn:QName(\"http://www.w3.org/2005/xquery-local-functions\",\n                              \"local:add\")")
    );
  }

  /**
   * Evaluates the "function-name" function with the argument refereing a partial application of a global user-defined function..
   */
  @org.junit.Test
  public void fnFunctionName023() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare function local:add($arg1, $arg2, $arg3)\n" +
      "        {\n" +
      "           $arg1 + $arg2 + $arg3\n" +
      "        };\n" +
      "\n" +
      "\tfn:function-name( local:add(1, 2, ?) )\n" +
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
      assertEmpty()
    );
  }

  /**
   * Evaluates the "function-name" function with the argument refereing to a function item returned by function coercion..
   */
  @org.junit.Test
  public void fnFunctionName024() {
    final XQuery query = new XQuery(
      "\n" +
      "\tdeclare function local:coerce($arg as function(item()*) as item()*)\n" +
      "          as function(item()*) as item()*\n" +
      "        {\n" +
      "           $arg\n" +
      "        };\n" +
      "        \n" +
      "        let $coerced := local:coerce(fn:abs#1)\n" +
      "        return if ($coerced instance of function(item()*) as item()*)\n" +
      "               then fn:function-name( local:coerce(fn:abs#1) )\n" +
      "               else \"error\"\n" +
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
      assertEq("fn:QName(\"http://www.w3.org/2005/xpath-functions\", \n                              \"fn:abs\")")
    );
  }
}
