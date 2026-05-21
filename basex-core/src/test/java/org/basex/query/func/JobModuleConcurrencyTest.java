package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the Job Module under concurrent access (parallel submission and execution of jobs).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobModuleConcurrencyTest extends SandboxTest {
  /** Wait until all queued jobs have been processed and consume cached results. */
  @AfterEach public void clean() {
    query(_JOB_LIST.args() + "[. != " + _JOB_CURRENT.args() + "] ! " + _JOB_WAIT.args(" ."));
    query("for $id in " + _JOB_LIST_DETAILS.args() + "[@cached = 'true'] " +
        " return try { " + _JOB_RESULT.args(" $id") + " } catch * {}");
  }

  /**
   * Submits many cached jobs from parallel threads and checks that every job is registered
   * with a unique id and produces its result exactly once.
   * @throws Exception exception
   */
  @Test @Timeout(60) public void concurrentEval() throws Exception {
    final int jobs = 50;
    final ConcurrentLinkedQueue<String> ids = new ConcurrentLinkedQueue<>();

    // submit one cached job per thread; each job returns its own number
    final ArrayList<Callable<?>> tasks = new ArrayList<>(jobs);
    for(int i = 0; i < jobs; i++) {
      final int n = i;
      tasks.add(() -> {
        ids.add(query(_JOB_EVAL.args(Integer.toString(n), " ()", " { 'cache': true() }")));
        return null;
      });
    }
    parallel(tasks);

    // all jobs were registered, and all ids are distinct
    assertEquals(jobs, ids.size());
    assertEquals(jobs, new HashSet<>(ids).size());

    // every job completes and returns its number; results are exactly 0 .. jobs - 1
    final TreeSet<Integer> results = new TreeSet<>();
    for(final String id : ids) {
      query(_JOB_WAIT.args(id));
      results.add(Integer.parseInt(query(_JOB_RESULT.args(id))));
    }
    assertEquals(jobs, results.size());
    assertEquals(0, results.first());
    assertEquals(jobs - 1, results.last());
  }

  /**
   * Submits many jobs that update the same database in parallel and checks that all updates
   * are applied exactly once (writes must be serialized by the locking layer).
   */
  @Test @Timeout(60) public void concurrentWritingJobs() {
    final int jobs = 30;
    final String insert = "insert node <node/> into db:get('" + NAME + "')/root";

    execute(new CreateDB(NAME, "<root/>"));
    try {
      // submit jobs that each append one node to the same database
      final ArrayList<String> ids = new ArrayList<>(jobs);
      for(int i = 0; i < jobs; i++) ids.add(query(_JOB_EVAL.args(insert)));
      // wait for all jobs to finish
      for(final String id : ids) query(_JOB_WAIT.args(id));
      // every insert must have been applied exactly once
      query("count(" + _DB_GET.args(NAME) + "/root/node)", jobs);
    } finally {
      execute(new DropDB(NAME));
    }
  }

  /**
   * Checks that a running job acquires a database write lock and that an interactive query
   * writing to the same database is blocked until the job releases the lock.
   */
  @Test @Timeout(60) public void jobHoldsWriteLock() {
    final int hold = 1500;
    execute(new CreateDB(NAME, "<root/>"));
    try {
      // start a job that updates the database and holds the write lock for ~hold ms
      final String id = query(_JOB_EVAL.args(
          "insert node <a/> into db:get('" + NAME + "')/root, prof:sleep(" + hold + ")"));
      // wait until the job is running and has acquired the lock
      while(context.jobs.active.get(id) == null) Performance.sleep(1);
      Performance.sleep(200);

      // an interactive write to the same database must wait for the job to release the lock
      final long start = System.nanoTime();
      query("insert node <b/> into db:get('" + NAME + "')/root");
      final long elapsed = (System.nanoTime() - start) / 1000000;
      assertTrue(elapsed >= hold / 2,
          "interactive write was not blocked by the job (took " + elapsed + " ms)");

      // both updates have been applied
      query(_JOB_WAIT.args(id));
      query("count(" + _DB_GET.args(NAME) + "/root/*)", 2);
    } finally {
      execute(new DropDB(NAME));
    }
  }
}
