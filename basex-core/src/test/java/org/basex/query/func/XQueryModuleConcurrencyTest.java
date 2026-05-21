package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests {@code xquery:fork-join} with concurrent database access and nesting. Unlike the
 * functional fork-join tests, these exercise parallel access to shared database structures.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQueryModuleConcurrencyTest extends SandboxTest {
  /**
   * Reads from the same database concurrently in all fork-join branches and checks that the
   * aggregated result is correct (concurrent access to a shared {@code Data} instance).
   */
  @Test @Timeout(60) public void forkJoinDatabaseRead() {
    final int docs = 200;
    query(_DB_CREATE.args(NAME,
        " <root>{ (1 to " + docs + ") ! <e>{.}</e> }</root>", "doc.xml"));
    try {
      query("""
xquery:fork-join(
  for $i in 1 to %d
  return fn() { xs:integer(db:get('%s')/root/e[$i]) }
) => sum()
          """.formatted(docs, NAME), docs * (docs + 1) / 2);
    } finally {
      query(_DB_DROP.args(NAME));
    }
  }

  /**
   * Runs fork-join nested inside fork-join; this must not exhaust the worker thread pool or
   * deadlock.
   */
  @Test @Timeout(60) public void nestedForkJoin() {
    query("""
xquery:fork-join(
  for $i in 1 to 20
  return fn() {
    xquery:fork-join(
      for $j in 1 to 20
      return fn() { $i * $j }
    ) => sum()
  }
) => sum()
        """, 44100);
  }

  /**
   * Stops a long-running fork-join job and checks that the cancellation reaches all branches so
   * the job terminates promptly (otherwise job:wait would block until the timeout).
   */
  @Disabled("timing-dependent")
  @Test @Timeout(30) public void stopForkJoin() {
    final String slow = "xquery:fork-join((1 to 64) ! fn() { (1 to 10000000000)[. = 1] })";
    final String id = query(_JOB_EVAL.args(slow));
    // wait until the fork-join job is running
    while(context.jobs.active.get(id) == null) Performance.sleep(1);
    // cancel all branches, including those that start after the stop
    query(_JOB_REMOVE.args(id));
    query(_JOB_WAIT.args(id));
    query(_JOB_FINISHED.args(id), true);
    // ensure that the processor remains responsive
    query("1 + 1", 2);
  }
}
