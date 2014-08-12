package org.basex.qt3ts.fn;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the environment-variable() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnEnvironmentVariable extends QT3TestSet {

  /**
   * Check that the function exists.
   */
  @org.junit.Test
  public void environmentVariable001() {
    final XQuery query = new XQuery(
      "fn:exists(environment-variable#1)",
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
   * Check that we can look the function up.
   */
  @org.junit.Test
  public void environmentVariable002() {
    final XQuery query = new XQuery(
      "not(fn:empty(fn:function-lookup(\n" +
      "\t  fn:QName('http://www.w3.org/2005/xpath-functions',\n" +
      "\t  'environment-variable') , 1)))",
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
   * Check that it has arity 1, not 0.
   */
  @org.junit.Test
  public void environmentVariable003() {
    final XQuery query = new XQuery(
      "fn:environment-variables#0",
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
   * Check that it has arity 1, not 2.
   */
  @org.junit.Test
  public void environmentVariable004() {
    final XQuery query = new XQuery(
      "fn:environment-variables#2",
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
   * Check that we get back () or $QTTEST = 42.
   */
  @org.junit.Test
  public void environmentVariable005() {
    final XQuery query = new XQuery(
      "let $all := fn:available-environment-variables()\n" +
      "\t  return empty($all) or ($all = \"QTTEST\" and fn:environment-variable(\"QTTEST\") eq \"42\")\n" +
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
      assertBoolean(true)
    );
  }

  /**
   * Check that $QTTEST2 is "other".
   */
  @org.junit.Test
  public void environmentVariable006() {
    final XQuery query = new XQuery(
      "let $all := fn:available-environment-variables()\n" +
      "\t  return empty($all) or\n" +
      "\t      ($all = \"QTTEST2\"\n" +
      "\t       and (fn:environment-variable(\"QTTEST2\") eq \"other\")\n" +
      "\t       and (not(\"other\" eq \"42\")))\n" +
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
      assertBoolean(true)
    );
  }

  /**
   * Check we can distinguish an unset variable from an empty one.
   */
  @org.junit.Test
  public void environmentVariable007() {
    final XQuery query = new XQuery(
      "let $all := fn:available-environment-variables()\n" +
      "\t  return empty($all) or ($all = \"QTTESTEMPTY\" and fn:environment-variable(\"QTTESTEMPTY\") eq \"\")\n" +
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
      assertBoolean(true)
    );
  }

  /**
   * environment-variable arg must be supplied.
   */
  @org.junit.Test
  public void environmentVariable008() {
    final XQuery query = new XQuery(
      "\n" +
      "\t  fn:environment-variable()\n" +
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
      error("XPST0017")
    );
  }

  /**
   * environment-variable arg must be a string.
   */
  @org.junit.Test
  public void environmentVariable009() {
    final XQuery query = new XQuery(
      "\n" +
      "\t  fn:environment-variable(1)\n" +
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
      error("XPTY0004")
    );
  }

  /**
   * environment-variable arg must be a string.
   */
  @org.junit.Test
  public void environmentVariable010() {
    final XQuery query = new XQuery(
      "\n" +
      "\t  fn:environment-variable(())\n" +
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
      error("XPTY0004")
    );
  }

  /**
   * environment-variable arg must be a string.
   */
  @org.junit.Test
  public void environmentVariable011() {
    final XQuery query = new XQuery(
      "\n" +
      "\t  fn:environment-variable(true())\n" +
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
      error("XPTY0004")
    );
  }

  /**
   * check for overflow by constructing a one-megabyte name.
   */
  @org.junit.Test
  public void environmentVariable012() {
    final XQuery query = new XQuery(
      "\n" +
      "\tlet $var := string-join(\n" +
      "\t  for $i in (1 to (1024 * 1024)) return \"x\"\n" +
      "\t) \n" +
      "\treturn empty(fn:environment-variable($var))\n" +
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
      assertBoolean(true)
    );
  }
}
