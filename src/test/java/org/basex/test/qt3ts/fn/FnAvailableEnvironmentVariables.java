package org.basex.test.qt3ts.fn;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the available-environment-variables() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class FnAvailableEnvironmentVariables extends QT3TestSet {

  /**
   * Check that the function exists.
   */
  @org.junit.Test
  public void fnAvailableEnvironmentVariables001() {
    final XQuery query = new XQuery(
      "fn:exists(fn:available-environment-variables#0)",
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
  public void fnAvailableEnvironmentVariables002() {
    final XQuery query = new XQuery(
      "not(fn:empty(fn:function-lookup(\n" +
      "\t  fn:QName('http://www.w3.org/2005/xpath-functions',\n" +
      "\t  'available-environment-variables'), 0)))\n" +
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
   * Check that it has arity 0, not 1.
   */
  @org.junit.Test
  public void fnAvailableEnvironmentVariables003() {
    final XQuery query = new XQuery(
      "fn:available-environment-variables#1",
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
   * Check that it has arity 0, not 2.
   */
  @org.junit.Test
  public void fnAvailableEnvironmentVariables004() {
    final XQuery query = new XQuery(
      "fn:available-environment-variables#2",
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
   * Check that we get back () or a sequence that accepts a predicate.
   */
  @org.junit.Test
  public void fnAvailableEnvironmentVariables005() {
    final XQuery query = new XQuery(
      "let $all := fn:available-environment-variables()\n" +
      "\t  return empty($all) or ($all[1] ne \"\")\n" +
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
   * Check that no returned values are non-strings.
   */
  @org.junit.Test
  public void fnAvailableEnvironmentVariables006() {
    final XQuery query = new XQuery(
      "\n" +
      "\t   (for $e in fn:available-environment-variables()\n" +
      "\t   return fn:environment-variable($e) instance of xs:string) = (false())\n" +
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
      assertBoolean(false)
    );
  }

  /**
   * Check that we can call environment-variable on each returned result.
   */
  @org.junit.Test
  public void fnAvailableEnvironmentVariables007() {
    final XQuery query = new XQuery(
      "\n" +
      "\t  let $all := fn:available-environment-variables(), \n" +
      "\t      $n := count(fn:available-environment-variables())\n" +
      "\t  return\n" +
      "\t      count(\n" +
      "\t\t  for $e in $all return fn:environment-variable($e)\n" +
      "\t      ) eq $n\n" +
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
   * check there are no duplicates in the variable names.
   */
  @org.junit.Test
  public void fnAvailableEnvironmentVariables008() {
    final XQuery query = new XQuery(
      "\n" +
      "\t  let $all := fn:available-environment-variables()\n" +
      "\t  return count($all) eq count(distinct-values($all))\n" +
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
   * check that the result is at least moderately stable.
   */
  @org.junit.Test
  public void fnAvailableEnvironmentVariables009() {
    final XQuery query = new XQuery(
      "\n" +
      "          \n" +
      "\t  let $first := <all>{\n" +
      "\t\t  for $e in fn:available-environment-variables()\n" +
      "\t\t  order by $e\n" +
      "\t\t  return <v name=\"$i\">{ fn:environment-variable($e) }</v>\n" +
      "\t      }</all>,\n" +
      "\t      $second := <all>{\n" +
      "\t\t  for $e in fn:available-environment-variables()\n" +
      "\t\t  order by $e\n" +
      "\t\t  return <v name=\"$i\">{ fn:environment-variable($e) }</v>\n" +
      "\t      }</all>\n" +
      "\t  return deep-equal($first, $second)\n" +
      "\t  \n" +
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
   * check that requesting a not-available variable returns ().
   */
  @org.junit.Test
  public void fnAvailableEnvironmentVariables010() {
    final XQuery query = new XQuery(
      "\n" +
      "\t  let $all := fn:available-environment-variables()\n" +
      "\t  return if (fn:empty($all)) then true()\n" +
      "\t  else fn:empty(fn:environment-variable(fn:string-join($all)))\n" +
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
   * check that if any env var is available, QTTEST is a string, "42".
   */
  @org.junit.Test
  public void fnAvailableEnvironmentVariables011() {
    final XQuery query = new XQuery(
      "\n" +
      "\t  let $all := fn:available-environment-variables()\n" +
      "\t  return if (fn:empty($all)) then true()\n" +
      "\t  else if ($all = \"QTTEST\")\n" +
      "\t  then fn:environment-variable(\"QTTEST\") eq \"42\"\n" +
      "\t  else false()\n" +
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
