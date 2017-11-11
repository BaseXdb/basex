package org.basex.query;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.util.*;

/**
 * This class contains some methods for performing advanced query tests.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class AdvancedQueryTest extends SandboxTest {
  /**
   * Checks if a query yields the specified string.
   * @param query query string
   * @param expected expected result
   */
  protected static void query(final String query, final Object expected) {
    final String res = query(query).replaceAll("(\r?\n|\r) *", "\n");
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
    final String res = normNL(query(query));
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
      final String res = eval(query);
      final TokenBuilder tb = new TokenBuilder("Query did not fail:\n");
      tb.add(query).add("\n[E] Error: ");
      for(final QueryError e : error) tb.add(' ').add(e.qname().prefixId());
      fail(tb.add("\n[F] ").add(res).toString());
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
        fail(tb.add("\nResult: ").add(err.name() + " (" + ex.getLocalizedMessage() + ')').
            toString());
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
}
