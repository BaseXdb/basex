package org.basex.test.query;

import static org.junit.Assert.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.test.*;
import org.basex.util.*;

/**
 * This class contains some methods for performing advanced query tests.
 *
 * @author BaseX Team 2005-13, BSD License
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
    qp.sc.baseURI(".");
    try {
      final ArrayOutput ao = new ArrayOutput();
      final Serializer ser = qp.getSerializer(ao);
      qp.execute().serialize(ser);
      ser.close();
      return ao.toString().replaceAll("(\\r|\\n)+ *", "");
    } catch(final Exception ex) {
      final AssertionError err = new AssertionError("Query failed:\n" + query);
      err.initCause(ex);
      throw err;
    } finally {
      qp.close();
    }
  }

  /**
   * Checks if a query yields the specified string.
   * @param query query string
   * @param result expected query result
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
    return
      "copy $input := " + input + ' ' +
      "modify (" + modification + ") " +
      "return (" + (ret.isEmpty() ? "$input" : ret) + ')';
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
    qp.sc.baseURI(".");
    try {
      final ArrayOutput ao = new ArrayOutput();
      final Serializer ser = qp.getSerializer(ao);
      qp.execute().serialize(ser);
      ser.close();
      final String res = ao.toString().replaceAll("(\\r|\\n)+ *", "");
      final StringBuilder sb = new StringBuilder("Query did not fail:\n");
      sb.append(query).append("\n[E]");
      for(final Err e : error) sb.append(' ').append(e);
      fail(sb.append("\n[F] ").append(res).toString());
    } catch(final QueryIOException ex) {
      check(query, ex.getCause(), error);
    } catch(final QueryException ex) {
      check(query, ex, error);
    } catch(final Exception ex) {
      Util.stack(ex);
      fail("Unexpected exception: " + ex);
    } finally {
      qp.close();
    }
  }

  /**
   * Checks if an exception yields one of the specified error codes.
   * @param query query
   * @param ex resulting query exception
   * @param error expected errors
   */
  protected static void check(final String query, final QueryException ex,
      final Err... error) {

    boolean found = false;
    final Err err = ex.err();
    for(final Err e : error) found |= err != null ? err == e : e.qname().eq(ex.qname());

    if(!found) {
      final TokenBuilder tb = new TokenBuilder("\n");
      if(query != null) tb.add("Query: ").add(query).add("\n");
      tb.add("Error(s): ");
      if(err != null) {
        int c = 0;
        for(final Err er : error) tb.add(c++ == 0 ? "" : "/").add(er.name());
        fail(tb.add("\nResult: ").add(err.name() + " (" + err.qname() + ')').toString());
      } else {
        int c = 0;
        for(final Err er : error) tb.add(c++ == 0 ? "" : "/").add(er.qname().local());
        fail(tb.add("\nResult: ").add(ex.qname().string()).toString());
      }
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
