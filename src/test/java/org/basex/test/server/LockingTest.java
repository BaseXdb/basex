package org.basex.test.server;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.test.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

/**
 * This class tests database locking inside BaseX. For this purpose, two queries are
 * forced to be executed in parallel. If this fails, locking prevents these queries to
 * run in parallel.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Jens Erat
 */
@RunWith(Parameterized.class)
public final class LockingTest extends SandboxTest {
  /** How often should tests be repeated? */
  private static final int REPEAT = 1;

  /** Maximum sleep time in ms. */
  private static final int SLEEP = 100;
  /** Additional allowed holding time for client creation overhead, ... in ms. */
  private static final int SYNC = 50;

  /** Test document. */
  private static final String DOC = "src/test/resources/test.xml";
  /** XQuery code for handling latches. */
  private static final String Q
    = "Q{java:org.basex.test.server.LockingTest}countDownAndWait()";

  /** Server reference. */
  private static BaseXServer server;

  /**
   * Enable repeated running of test to track down synchronization issues.
   * @return Collection of empty object arrays
   */
  @Parameters
  public static Collection<Object[]> generateParams() {
    List<Object[]> params = new ArrayList<Object[]>();
    for(int i = 1; i <= REPEAT; i++) {
      params.add(new Object[0]);
    }
    return params;
  }

  /**
   * Starts the server.
   * @throws Exception None expected
   */
  @BeforeClass
  public static void start() throws Exception {
    server = createServer();
    CountDownLatch latch = new CountDownLatch(2);
    new Client(new CreateDB(NAME, DOC), null, latch);
    new Client(new CreateDB(NAME + "1", DOC), null, latch);
    // Wait for both databases being created
    latch.await();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void stop() throws IOException {
    stopServer(server);
  }

  /** Latch for synchronizing threads inside lock. */
  public static CountDownLatch sync;
  /** Latch for testing parallelism. */
  public static CountDownLatch test;

  /**
   * Handle thread synchronization so both threads/queries have to be inside their locks
   * at the same time to count down {@code test} latch.
   * @throws Exception None expected
   */
  public static void countDownAndWait() throws Exception {
    sync.countDown();
    if (sync.await(SLEEP, TimeUnit.MILLISECONDS)) {
      test.countDown();
    }
  }

  /**
   * Test parallel execution of given queries.
   * @param c1 First command
   * @param c2 Second command
   * @param parallel Should queries be executed in parallel?
   * @throws Exception None expected
   */
  private static void testQueries(final Command c1, final Command c2,
      final boolean parallel) throws Exception {
    sync = new CountDownLatch(2);
    test = new CountDownLatch(2);
    Client cl1 = new Client(c1, null, null);
    Client cl2 = new Client(c2, null, null);
    boolean await = test.await(2 * SLEEP + SYNC, TimeUnit.MILLISECONDS);
    assertTrue(cl1.error, cl1.error == null);
    assertTrue(cl2.error, cl2.error == null);
    assertTrue(parallel ? "Parallel execution expected" : "Serial execution expected",
                        parallel == await);
  }

  /**
   * Encapsulates string formatter for convenience.
   * @param formatString Format string
   * @param args Objects to insert into format string
   * @return Formatted string
   */
  private static String f(final String formatString, final Object... args) {
    return new Formatter().format(formatString, args).toString();
  }

  /**
   * Test whether parallel execution of queries is successful.
   * @throws Exception None expected
   */
  @Test
  public void noLocking() throws Exception {
    // Not querying any databases
    testQueries(
        new XQuery(Q),
        new XQuery(Q),
        true);
    // Reading the same database twice
    testQueries(
        new XQuery(f("(doc('%s'), %s)", NAME, Q)),
        new XQuery(f("(doc('%s'), %s)", NAME, Q)),
        true);
    // Reading two different databases
    testQueries(
        new XQuery(f("(doc('%s'), %s)", NAME, Q)),
        new XQuery(f("(doc('%s1'), %s)", NAME, Q)),
        true);
    // Writing to the same database twice
    testQueries(
        new XQuery(f("insert node %s into doc('%s')", Q, NAME)),
        new XQuery(f("insert node %s into doc('%s')", Q, NAME)),
        false);
    // Writing to different databases
    testQueries(
        new XQuery(f("insert node %s into doc('%s')", Q, NAME)),
        new XQuery(f("insert node %s into doc('%s1')", Q, NAME)),
        true);
    // Read from and write to the same database
    testQueries(
        new XQuery(f("(doc('%s'), %s)", NAME, Q)),
        new XQuery(f("insert node %s into doc('%s')", Q, NAME)),
        false);
    // Read from and write to different databases
    testQueries(
        new XQuery(f("(doc('%s'), %s)", NAME, Q)),
        new XQuery(f("insert node %s into doc('%s1')", Q, NAME)),
        true);
    // Read from a database, perform global write lock
    testQueries(
        new XQuery(f("(doc('%s'), %s)", NAME, Q)),
        new XQuery(f("for $i in ('%s') return insert node %s into doc($i)", NAME, Q)),
        false);
    // Global write lock twice
    testQueries(
        new XQuery(f("for $i in ('%s') return insert node %s into doc($i)", NAME, Q)),
        new XQuery(f("for $i in ('%s') return insert node %s into doc($i)", NAME, Q)),
        false);
  }
}
