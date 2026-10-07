package org.basex.query;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the trace output of queries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TraceTest extends SandboxTest {
  /** Collected trace output. */
  private static final ArrayList<String> TRACES = new ArrayList<>();

  /** Registers a query tracer. */
  @BeforeAll public static void beforeAll() {
    context.setExternal(new QueryTracer() {
      @Override
      public void printTrace(final String message) {
        TRACES.add(message);
      }

      @Override
      public boolean cacheTrace() {
        return false;
      }
    });
  }

  /** Discards collected trace output. */
  @BeforeEach public void beforeEach() {
    TRACES.clear();
  }

  /** Test method. */
  @Test public void trace() {
    query(TRACE.args(1), 1);
    assertEquals(List.of("1"), TRACES);

    query(TRACE.args(1, "L"), 1);
    assertEquals(List.of("1", "L: 1"), TRACES);
  }

  /** Test method. */
  @Test public void message() {
    query(MESSAGE.args(1), "");
    assertEquals(List.of("1"), TRACES);
  }

  /** General comparisons: each operand is evaluated once. */
  @Test public void generalComparison() {
    query("declare function local:f($n) { message('f'), (1, 2, 3)[. > $n] };\n"
        + "declare function local:g($n) { message('g'), (5, 6, 7)[. > $n] };\n"
        + "local:f(" + _RANDOM_INTEGER.args(1) + ") = local:g(" + _RANDOM_INTEGER.args(1) + ')',
        false);
    assertEquals(List.of("f", "g"), TRACES);
  }

  /** Test method. */
  @Test public void permission() {
    query(_XQUERY_EVAL.args(" 'trace(1)'"), 1);
    assertEquals(List.of("1"), TRACES);

    TRACES.clear();
    query(_XQUERY_EVAL.args(" 'trace(1)'", " ()", " { 'permission': 'create' }"), 1);
    assertEquals(List.of("1"), TRACES);

    TRACES.clear();
    query(_XQUERY_EVAL.args(" 'trace(1)'", " ()", " { 'permission': 'write' }"), 1);
    assertTrue(TRACES.isEmpty(), TRACES::toString);
  }
}
