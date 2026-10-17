package org.basex.query.func;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.core.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.geo.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the XQuery Geo functions prefixed with "geo".
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public abstract class GmlTest extends Sandbox {
  /** Geo module import. */
  protected static final String GEO_MODULE_IMPORT = "import module "
      + "namespace geo='http://expath.org/ns/geo' at 'src/main/resources/org/expath/ns/geo.xqm';\n";

  /**
   * Return the GML namespace URI to be used in the tests of this class.
   * @return GML namespace URI
   */
  protected abstract String gmlUri();

  /**
   * Creates the sandbox.
   */
  @BeforeAll
  public static void beforeAll() {
    initSandbox();
  }

  /**
   * Removes test databases and closes the database context.
   */
  @AfterAll
  public static void afterAll() {
    finishSandbox();
  }

  /**
   * Query.
   *
   * @param query query
   * @param result result
   */
  protected void run(final String query, final Object result) {
    query("declare namespace gml='" + gmlUri() + "';\n" + GEO_MODULE_IMPORT
        + query, result);
  }

  /**
   * Builds the function call for the specified function and arguments, with a "geo:" prefix.
   * @param func function
   * @param args arguments
   * @return function call
   */
  protected String args(final String func, final Object... args) {
    return " geo:" + func + args(args);
  }

  /**
   * Returns a string representation of the specified function arguments. All objects are wrapped
   * with quotes, except for the following ones:
   * <ul>
   * <li>numbers (integer, long, float, double)</li>
   * <li>booleans (which will be suffixed with parentheses)</li>
   * <li>strings starting with a space (which will be chopped)</li>
   * </ul>
   * @param args arguments
   * @return string representation with leading space (simplifies nesting of returned string)
   */
  private String args(final Object... args) {
    final TokenBuilder tb = new TokenBuilder().add('(');
    int c = 0;
    for(final Object arg : args) {
      if(c++ > 0) tb.add(", ");
      if(arg instanceof Expr || arg instanceof Number) {
        tb.add(arg);
      } else if(arg instanceof Boolean) {
        tb.add(arg + "()");
      } else {
        final String str = arg.toString();
        if(Strings.startsWith(str, ' ')) {
          tb.add(str.substring(1));
        } else {
          tb.add('"' + str.replace("\"", "\"\"") + '"');
        }
      }
    }
    return tb.add(')').toString();
  }

  /**
   * Checks if a query yields the specified BaseX error code.
   *
   * @param query query string
   * @param qe query error
   */
  protected void error(final String query, final QueryError qe) {
    error(query, qe, null);
  }

  /**
   * Checks if a query yields the specified Geo error code.
   *
   * @param query query string
   * @param ge Geo error
   */
  protected void error(final String query, final GeoError ge) {
    error(query, null, ge);
  }

  /**
   * Checks if a query yields the specified error code.
   *
   * @param query query string
   * @param qe query error (can be {@code null})
   * @param ge geo error (can be {@code null})
   */
  private void error(final String query, final QueryError qe, final GeoError ge) {
    final String qu = "declare namespace gml='" + gmlUri() + "';\n" + GEO_MODULE_IMPORT + query;
    final QNm qname = ge != null ? ge.qname() : qe.qname();
    try (QueryProcessor qp = new QueryProcessor(qu, context)) {
      final ArrayOutput ao = qp.value().serialize();
      fail("Query did not fail:\n" + query + "\n[E] " + qname + "...\n[F] " + ao);
    } catch (final QueryException ex) {
      QueryError ee = ex.error();
      if (qe != null && qe != ee || ge != null && !qname.eq(ex.qname())) {
        Util.stack(ex);
        final StringBuilder sb = new StringBuilder("Wrong error code:\n[E] ");
        fail(sb.append(qe != null ? qe : ge.qname()).append("\n[F] ").append(
            ee != null ? ee.name() : ex.qname()).toString());
      }
    } catch (final Exception ex) {
      Util.stack(ex);
      fail(ex.toString());
    }
  }
}
