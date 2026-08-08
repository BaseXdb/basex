package org.basex.query.var;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the concurrent evaluation of static variables.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StaticVarTest extends SandboxTest {
  /** Query declaring a lazy variable that takes a while to evaluate. */
  private static final String QUERY =
      "declare %basex:lazy variable $v := (prof:sleep(1000), random:uuid()); $v";
  /** Milliseconds to wait before the second context reads the variable. */
  private static final long DELAY = 300;
  /** Milliseconds to wait for the second context to return. */
  private static final long TIMEOUT = 10000;

  /**
   * Reads a single static variable from two query contexts that have no common parent.
   * @throws Exception exception
   */
  @Test public void crossContext() throws Exception {
    try(QueryProcessor qp = new QueryProcessor(QUERY, context)) {
      qp.compile();
      final StaticVar var = qp.qc.vars.iterator().next();

      final Value[] values = new Value[2];
      final Throwable[] error = new Throwable[1];
      final Thread thread = new Thread(() -> {
        try(QueryProcessor qp2 = new QueryProcessor("()", context)) {
          Thread.sleep(DELAY);
          values[1] = var.value(qp2.qc);
        } catch(final Throwable th) {
          error[0] = th;
        }
      });
      thread.setDaemon(true);
      thread.start();

      values[0] = var.value(qp.qc);
      thread.join(TIMEOUT);

      assertFalse(thread.isAlive(), "Second context is still waiting for the variable.");
      if(error[0] != null) fail(error[0]);
      assertEquals(values[0].toString(), values[1].toString(),
          "Static variable was evaluated more than once.");
    }
  }
}
