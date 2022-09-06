package org.basex.query.ast;

import static org.basex.util.Prop.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Abstract test class for properties on the Query Plan.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
public abstract class QueryPlanTest extends SandboxTest {
  /**
   * Checks the query plan and the result.
   * @param query query
   * @param expected result or {@code null} for no comparison
   * @param tests queries on the query plan
   */
  protected static void check(final String query, final Object expected, final String... tests) {
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      qp.optimize();
      final FElem plan = qp.toXml();
      // compare result
      if(expected != null) {
        compare(query, qp.value().serialize().toString(), expected, plan);
      }
      // check syntax tree
      for(final String test : tests) {
        try(QueryProcessor qp2 = new QueryProcessor(test, context).context(new FDoc().add(plan))) {
          if(qp2.value() != Bln.TRUE) fail(NL + "QUERY: " + query + NL +
              "OPTIMIZED: " + qp.qc.main + NL + "TEST: " + test + NL + serialize(plan));
        }
      }
    } catch(final Exception ex) {
      Util.stack(ex);
      throw ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
    }
  }

  /**
   * Returns a test to check if the specified expression or path does not occur in the query plan.
   * @param expr expression
   * @return test string
   */
  protected static String empty(final String expr) {
    return "empty(//" + expr + ')';
  }

  /**
   * Returns a test to check if the specified expression does not occur in the query plan.
   * @param clazz name of expression
   * @return test string
   */
  protected static String empty(final Class<?> clazz) {
    return empty(Util.className(clazz));
  }

  /**
   * Returns a test to check if the specified function does not occur in the query plan.
   * @param func function
   * @return test string
   */
  protected static String empty(final Function func) {
    return empty(func.className());
  }

  /**
   * Returns a test to check if the specified expression or path occurs in the query plan.
   * @param expr expression
   * @return test string
   */
  protected static String exists(final String expr) {
    return "exists(//" + expr + ')';
  }

  /**
   * Returns a test to check if the specified expression occurs in the query plan.
   * @param clazz expression class
   * @return test string
   */
  protected static String exists(final Class<?> clazz) {
    return exists(Util.className(clazz));
  }

  /**
   * Returns a test to check if the specified function occurs in the query plan.
   * @param func function
   * @return test string
   */
  protected static String exists(final Function func) {
    return exists(func.className());
  }

  /**
   * Returns a test to check if the query plan is empty.
   * @return test string
   */
  protected static String empty() {
    return root("Empty");
  }

  /**
   * Returns a test to check if the root is an instance of the specified expression.
   * @param expr expression
   * @return test string
   */
  protected static String root(final String expr) {
    return "name(QueryPlan/*) = '" + expr + "'";
  }

  /**
   * Returns a test to check if the root is an instance of the specified expression.
   * @param func function
   * @return test string
   */
  protected static String root(final Function func) {
    return root(func.className());
  }

  /**
   * Returns a test to check if the root is an instance of the specified expression.
   * @param clazz name of expression
   * @return test string
   */
  protected static String root(final Class<?> clazz) {
    return root(Util.className(clazz));
  }

  /**
   * Counts the number of results.
   * @param clazz expression class
   * @param count number of results
   * @return test string
   */
  protected static String count(final Class<?> clazz, final int count) {
    return count(Util.className(clazz), count);
  }

  /**
   * Counts the number of results.
   * @param func function
   * @param count number of results
   * @return test string
   */
  protected static String count(final Function func, final int count) {
    return count(func.className(), count);
  }

  /**
   * Counts the number of results.
   * @param expr expression
   * @param count number of results
   * @return test string
   */
  protected static String count(final String expr, final int count) {
    return "count(//" + expr + ") = " + count;
  }

  /**
   * Returns a test to check the expression type.
   * @param name name of expression
   * @param type type
   * @return test string
   */
  protected static String type(final String name, final String type) {
    return "string(//" + name + "/@type) = '" + type + "'";
  }

  /**
   * Returns a test to check the expression type.
   * @param clazz expression class
   * @param type type
   * @return test string
   */
  protected static String type(final Class<?> clazz, final String type) {
    return type(Util.className(clazz), type);
  }

  /**
   * Returns a test to check the function type.
   * @param func function
   * @param type type
   * @return test string
   */
  protected static String type(final Function func, final String type) {
    return type(func.className(), type);
  }
}
