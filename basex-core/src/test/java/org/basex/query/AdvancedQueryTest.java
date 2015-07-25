package org.basex.query;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.util.*;

/**
 * This class contains some methods for performing advanced query tests.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class AdvancedQueryTest extends SandboxTest {
  /** Base uri. */
  private static final String BASEURI = new File(".").getAbsolutePath();

  /**
   * Runs the specified query and normalizes newlines.
   * @param query query string
   * @return result
   */
  protected static String query(final String query) {
    try {
      return run(query);
    } catch(final QueryException | IOException ex) {
      Util.errln(Util.message(ex));
      Util.stack(12);
      final AssertionError err = new AssertionError("Query failed:\n" + query);
      err.initCause(ex);
      throw err;
    }
  }

  /**
   * Checks if a query yields the specified string.
   * @param query query string
   * @param expected expected query result
   */
  protected static void query(final String query, final Object expected) {
    final String res = query(query);
    final String exp = expected.toString();
    assertEquals("Wrong result:\n[Q] " + query + "\n[E] \u00bb" + exp +
        "\u00ab\n[F] \u00bb" + res + "\u00ab\n", exp, res);
  }

  /**
   * Creates a transform expression from a given input, modification and return clause.
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
  protected static void error(final String query, final QueryError... error) {
    try {
      final String res = run(query);
      final StringBuilder sb = new StringBuilder("Query did not fail:\n");
      sb.append(query).append("\n[E]");
      for(final QueryError e : error) sb.append(' ').append(e);
      fail(sb.append("\n[F] ").append(res).toString());
    } catch(final QueryIOException ex) {
      check(query, ex.getCause(), error);
    } catch(final QueryException ex) {
      check(query, ex, error);
    } catch(final Exception ex) {
      Util.stack(ex);
      fail("Unexpected exception: " + ex);
    }
  }

  /**
   * Checks if an exception yields one of the specified error codes.
   * @param query query
   * @param ex resulting query exception
   * @param errors expected errors
   */
  protected static void check(final String query, final QueryException ex,
      final QueryError... errors) {

    boolean found = false;
    final QueryError err = ex.error();
    for(final QueryError e : errors) found |= err != null ? err == e : e.qname().eq(ex.qname());

    if(!found) {
      final TokenBuilder tb = new TokenBuilder("\n");
      if(query != null) tb.add("Query: ").add(query).add("\n");
      tb.add("Error(s): ");
      if(err != null) {
        int c = 0;
        for(final QueryError er : errors) tb.add(c++ == 0 ? "" : "/").add(er.name());
        ex.printStackTrace();
        fail(tb.add("\nResult: ").add(err.name() + " (" + err.qname() + ')').toString());
      } else {
        int c = 0;
        for(final QueryError er : errors) tb.add(c++ == 0 ? "" : "/").add(er.qname().local());
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

  /**
   * Runs the specified query and normalizes newlines.
   * @param query query string
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private static String run(final String query) throws QueryException, IOException {
    final ArrayOutput ao = new ArrayOutput();
    try(final QueryProcessor qp = new QueryProcessor(query, context)) {
      qp.sc.baseURI(BASEURI);
      try(final Serializer ser = qp.getSerializer(ao)) {
        qp.value().serialize(ser);
      }
    }
    return normNL(ao.toString());
  }
}
