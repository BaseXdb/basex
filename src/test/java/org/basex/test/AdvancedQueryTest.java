package org.basex.test;

import static org.junit.Assert.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * This class contains some methods for performing advanced query tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class AdvancedQueryTest extends SandboxTest {
  /**
   * Runs the specified query and normalizes newlines.
   * @param query query string
   * @return result
   */
  protected static String query(final String query) {
    final QueryProcessor qp = new QueryProcessor(query, context);
    qp.ctx.sc.baseURI(".");
    try {
      final ArrayOutput ao = new ArrayOutput();
      final Serializer ser = qp.getSerializer(ao);
      qp.execute().serialize(ser);
      ser.close();
      return ao.toString().replaceAll("(\\r|\\n)+ *", "");
    } catch(final Exception ex) {
      ex.printStackTrace();
      fail("Query failed:\n" + query + "\nMessage: " + ex);
      return null;
    } finally {
      qp.close();
    }
  }

  /**
   * Checks if a query yields the specified string.
   * @param query query string
   * @param result query result
   */
  protected static void query(final String query, final Object result) {
    final String res = query(query);
    final String exp = result.toString();
    if(!res.equals(exp))
      fail("Wrong result:\n[Q] " + query + "\n[E] \u00bb" + result +
          "\u00ab\n[F] \u00bb" + res + '\u00ab');
  }

  /**
   * Creates a transform expression from a given input, modification and return clause.
   *
   * @param input input XML fragment, target of the updating expression
   * @param modification updating expression, make sure to address all target nodes via
   * the $input variable, i.e. delete node $input/a
   * @param ret return clause
   * @return the query formulated with a transform expression
   */
  protected static String transform(final String input, final String modification,
      final String ret) {
    final String q =
        "copy $input := " + input + " " +
        "modify (" + modification + ") " +
        "return (" + (ret.length() == 0 ? "$input" : ret) + ")";
    return q;
  }

  /**
   * Creates a transform expression from a given input and modification clause.
   *
   * @param input input XML fragment, target of the updating expression
   * @param modification updating expression, make sure to address all target nodes via
   * the $input variable, i.e. delete node $input/a
   * @return the query formulated with a transform expression
   */
  protected static String transform(final String input, final String modification) {
    return transform(input, modification, "");
  }

  /**
   * Checks if a query yields the specified result.
   * @param query query string
   * @param result query result
   */
  protected static void contains(final String query, final String result) {
    final String res = query(query);
    if(!res.contains(result))
      fail("Result does not contain \"" + result + "\":\n" + query + "\n[E] " +
          result + "\n[F] " + res);
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query string
   * @param error expected error
   */
  protected static void error(final String query, final Err... error) {
    final QueryProcessor qp = new QueryProcessor(query, context);
    qp.ctx.sc.baseURI(".");
    try {
      final ArrayOutput ao = new ArrayOutput();
      final Serializer ser = qp.getSerializer(ao);
      qp.execute().serialize(ser);
      ser.close();
      final String res = ao.toString().replaceAll("(\\r|\\n)+ *", "");
      final StringBuilder sb = new StringBuilder("Query did not fail:\n");
      sb.append(query + "\n[E]");
      for(final Err e : error) sb.append(" " + e);
      fail(sb.append("\n[F] " + res).toString());
    } catch(final Exception ex) {
      check(query, ex, error);
    } finally {
      qp.close();
    }
  }

  /**
   * Checks if an exception yields one of the specified error codes.
   * @param query query
   * @param ex exception
   * @param error expected errors
   */
  protected static void check(final String query, final Exception ex,
      final Err... error) {

    if(error.length == 0) Util.notexpected("No error code specified");
    final byte[] msg = Token.token(ex.getMessage());
    boolean found = false;
    for(final Err e : error) found |= Token.contains(msg, e.qname().local());
    if(!found) {
      final TokenBuilder tb = new TokenBuilder("\n");
      if(query != null) tb.add("Query: ").add(query).add("\n");
      tb.add("Error(s): ");
      int c = 0;
      for(final Err er : error) {
        if(c++ != 0) tb.add('/');
        tb.add(er.qname().string());
      }
      fail(tb.add("\nResult: ").add(msg).toString());
    }
  }

  /**
   * Returns serialization parameters.
   * @param arg serialization arguments
   * @return parameter string
   */
  protected static String serialParams(final String arg) {
    return "<serialization-parameters " +
      "xmlns='http://www.w3.org/2010/xslt-xquery-serialization'>" + arg +
      "</serialization-parameters>";
  }
}
