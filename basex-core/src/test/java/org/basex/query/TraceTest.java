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
