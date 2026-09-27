package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.core.jobs.*;
import org.basex.query.*;
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
  /** Latch awaited by {@link #hold()}. */
  private static volatile CountDownLatch latch = new CountDownLatch(0);

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
  @Test @Timeout(10) public void concurrentEval() throws Exception {
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
  @Test @Timeout(10) public void concurrentWritingJobs() {
    final int jobs = 10;
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
   * Stops many long-running and queued jobs at once and checks that all of them terminate and
   * that the job pool is emptied again.
   * @throws Exception exception
   */
  @Test @Timeout(10) public void concurrentStop() throws Exception {
    final int jobs = 100, block = 30000, timeout = 5000;

    // queue more jobs than can run at a time: some are running, the others are still queued
    final ArrayList<String> ids = new ArrayList<>(jobs);
    for(int j = 0; j < jobs; j++) ids.add(query(_JOB_EVAL.args("prof:sleep(" + block + ")")));

    // stop all jobs at once: running ones receive a stop signal, queued ones are cancelled
    final ArrayList<Callable<?>> tasks = new ArrayList<>(jobs);
    for(final String id : ids) tasks.add(() -> query(_JOB_REMOVE.args(id)));
    parallel(tasks);

    // the query that asks the question is the only job that may be left
    final long end = System.nanoTime() + timeout * 1000000L;
    do {
      if(Integer.parseInt(query("count(" + _JOB_LIST.args() + ')')) == 1) return;
      Performance.sleep(20);
    } while(System.nanoTime() < end);
    fail("Jobs are still registered: " + query(_JOB_LIST_DETAILS.args()));
  }

  /**
   * Checks that a running job acquires a database write lock and that an interactive query
   * writing to the same database is blocked until the job releases the lock.
   * @throws Exception exception
   */
  @Test @Timeout(10) public void jobHoldsWriteLock() throws Exception {
    execute(new CreateDB(NAME, "<root/>"));
    try {
      final String id = holdWriteLock();
      // an interactive write to the same database must wait for the job to release the lock
      blocked("insert node <b/> into db:get('" + NAME + "')/root");

      // both updates have been applied
      query(_JOB_WAIT.args(id));
      query("count(" + _DB_GET.args(NAME) + "/root/*)", 2);
    } finally {
      latch.countDown();
      execute(new DropDB(NAME));
    }
  }

  /**
   * Checks that the opened database is only bound as context value if it is locked.
   * @throws Exception exception
   */
  @Test @Timeout(10) public void openedDatabase() throws Exception {
    final String name2 = NAME + '2';
    execute(new CreateDB(name2, "<root/>"));
    execute(new CreateDB(NAME, "<root/>"));
    try {
      // a query that ignores the context must not access the opened database
      final int pins = context.datas.pins(NAME);
      try(QueryProcessor qp = new QueryProcessor(_DB_GET.args(name2), context)) {
        qp.optimize();
        assertEquals(pins, context.datas.pins(NAME));
      }

      // synchronous jobs share the opened database: only a job that references it is blocked
      final String id = holdWriteLock();
      query(_JOB_EXECUTE.args(" \"db:get('" + name2 + "')\""), "<root/>");
      assertEquals("1", blocked(_JOB_EXECUTE.args(" \"count(.)\"")));
      query(_JOB_WAIT.args(id));
    } finally {
      latch.countDown();
      execute(new DropDB(NAME));
      execute(new DropDB(name2));
    }
  }

  /**
   * Blocks the calling job until the latch is released.
   * @throws InterruptedException interrupted exception
   */
  public static void hold() throws InterruptedException {
    latch.await();
  }

  /**
   * Starts a job that updates the database and holds its write lock until the latch is released.
   * @return job id
   */
  private static String holdWriteLock() {
    latch = new CountDownLatch(1);
    final String id = query(_JOB_EVAL.args("insert node <a/> into db:get('" + NAME + "')/root, " +
        "void(Q{java:" + JobModuleConcurrencyTest.class.getName() + "}hold())"));
    // the job is running once it has acquired its locks
    for(Job job; (job = context.jobs.active.get(id)) == null || job.state != JobState.RUNNING;) {
      Performance.sleep(1);
    }
    return id;
  }

  /**
   * Runs a query that must wait for the locks of the holding job, and releases the latch.
   * @param query query
   * @return result
   * @throws Exception exception
   */
  private static String blocked(final String query) throws Exception {
    final CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> query(query));
    while(context.jobs.active.values().stream().noneMatch(job -> job.state == JobState.QUEUED)) {
      Performance.sleep(1);
    }
    assertFalse(result.isDone());
    latch.countDown();
    return result.get();
  }
}
