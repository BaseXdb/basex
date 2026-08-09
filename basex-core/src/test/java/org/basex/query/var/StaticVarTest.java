package org.basex.query.var;

import static org.basex.query.QueryError.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Timeout.ThreadMode.*;

import java.io.*;

import org.basex.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;
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
      assertNotEquals(values[0].toString(), values[1].toString(),
          "Value was shared by unrelated query contexts.");
    }
  }

  /** Reads a static variable from a nested context while it is being evaluated. */
  @Test public void nestedContext() {
    error("declare %basex:lazy variable $v := xquery:eval(function() { $v }); $v", CIRCVAR_X);
  }

  /** Reads two mutually dependent static variables from nested contexts of parallel tasks. */
  @Test @Timeout(value = 20, threadMode = SEPARATE_THREAD) public void nestedCircular() {
    error("declare %basex:lazy variable $x := " +
        "(prof:sleep(100), xquery:eval(function() { $y })); " +
        "declare %basex:lazy variable $y := " +
        "(prof:sleep(100), xquery:eval(function() { $x })); " +
        "xquery:fork-join((function() { $x }, function() { $y }))", CIRCVAR_X);
  }

  /**
   * Reads a failing static variable from the parallel tasks of one query.
   * @throws IOException I/O exception
   */
  @Test public void forkJoinError() throws IOException {
    final IOFile file = new IOFile(sandbox(), "static-var");
    file.delete();
    error("declare %basex:lazy variable $v := (file:append-text('" + file.path() + "', 'x'), " +
        "error()); xquery:fork-join(for $i in 1 to 4 return function() { $v })", FUNERR1);
    assertEquals("x", Token.string(file.read()), "Variable was evaluated more than once.");
  }
}
