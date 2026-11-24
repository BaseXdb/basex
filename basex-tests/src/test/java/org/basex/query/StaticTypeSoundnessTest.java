package org.basex.query;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.*;

import org.basex.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Checks if the results of queries are instances of their statically inferred types.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StaticTypeSoundnessTest extends SandboxTest {
  /** Directory with test resources. */
  private static final String DIR = "src/test/resources/static-types/";
  /** Minimum number of checked results (guards against silently skipped cases). */
  private static final int MIN_CHECKS = 1000;

  /**
   * Compiles each case without bindings and checks its results for all binding sets.
   * @throws Exception exception
   */
  @Test public void soundness() throws Exception {
    final String prolog = Files.readString(Path.of(DIR, "prolog.xq"));
    final List<String> cases = Files.readAllLines(Path.of(DIR, "cases.txt"));
    final List<XQMap> bindings = bindings();

    final StringBuilder errors = new StringBuilder();
    int checks = 0;
    for(final String cs : cases) {
      final String query = prolog + cs;
      final SeqType st = staticType(query);
      if(st == null) continue;
      for(final XQMap binding : bindings) {
        final String error = check(query, binding, st);
        if(error == null) continue;
        checks++;
        if(!error.isEmpty()) errors.append(Prop.NL).append(cs).append(": ").append(error);
      }
    }
    if(!errors.isEmpty()) fail("Results are no instances of the static types:" + errors);
    assertTrue(checks >= MIN_CHECKS, "Too few results checked: " + checks);
  }

  /**
   * Returns the binding sets.
   * @return binding sets
   * @throws Exception exception
   */
  private static List<XQMap> bindings() throws Exception {
    final String query = Files.readString(Path.of(DIR, "bindings.xq"));
    final List<XQMap> bindings = new ArrayList<>();
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      for(final Item item : qp.value()) bindings.add((XQMap) item);
    }
    return bindings;
  }

  /**
   * Returns the static type of a compiled query.
   * @param query query
   * @return static type (can be {@code null})
   */
  private static SeqType staticType(final String query) {
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      qp.optimize();
      return qp.qc.main.expr.seqType();
    } catch(final QueryException ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Evaluates a query with the specified bindings and checks the result against a static type.
   * @param query query
   * @param binding variable bindings
   * @param st static type
   * @return error description, empty if the result matches (can be {@code null})
   */
  private static String check(final String query, final XQMap binding, final SeqType st) {
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      binding.forEach((key, value) -> qp.variable(key.toJava().toString(), value));
      final Value value = qp.value();
      return st.instance(value) ? "" : "static " + st + ", result " + value.seqType() + ": " +
        value;
    } catch(final QueryException ex) {
      Util.debug(ex);
      return null;
    }
  }
}
