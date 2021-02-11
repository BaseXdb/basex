package org.basex.core.locks;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests database locking inside BaseX. For this purpose, two queries are forced to be
 * executed in parallel. If this fails, locking prevents these queries to run in parallel.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Jens Erat
 */
public final class ServerLockingTest extends SandboxTest {
  /** Maximum sleep time in ms. */
  private static final long SLEEP = 250;
  /** Additional allowed holding time for client creation overhead, ... in ms. */
  private static final long SYNC = 50;

  /** Test document. */
  private static final String DOC = "src/test/resources/test.xml";
  /** How often to run each query in load test. */
  private static final int RUN_COUNT = 10;
  /** Queries to run in load test. */
  private static final String[] QUERIES = {
    "%2$s",
    "(db:open('%1$s'), %2$s)",
    "insert node %2$s into db:open('%1$s')",
    "for $i in ('%1$s') return insert node %2$s into db:open($i)",
    "for $i in ('%1$s') return (db:open($i), %2$s)"
  };
  /** XQuery code for handling latches. */
  private static final String Q = "Q{" + ServerLockingTest.class.getName() + "}countDownAndWait()";

  /** Server reference. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws Exception None expected
   */
  @BeforeAll public static void start() throws Exception {
    server = createServer();
    final CountDownLatch latch = new CountDownLatch(2);
    new Client(new CreateDB(NAME, DOC), null, latch);
    new Client(new CreateDB(NAME + '1', DOC), null, latch);
    // wait for both databases being created
    latch.await();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterAll public static void stop() throws IOException {
    stopServer(server);
  }

  /** Latch for synchronizing threads inside lock. */
  private static CountDownLatch sync;
  /** Latch for testing parallelism. */
  private static CountDownLatch test;

  /**
   * Handle thread synchronization so both threads/queries have to be inside their locks
   * at the same time to count down {@code test} latch.
   * @throws Exception None expected
   */
  public static void countDownAndWait() throws Exception {
    sync.countDown();
    if(sync.await(SLEEP, TimeUnit.MILLISECONDS)) test.countDown();
  }

  /**
   * Test parallel execution of given queries.
   * @param query1 first query
   * @param query2 second query
   * @param parallel Should queries be executed in parallel?
   * @throws Exception None expected
   */
  private static void testQueries(final String query1, final String query2, final boolean parallel)
      throws Exception {

    sync = new CountDownLatch(2);
    test = new CountDownLatch(2);
    final Client cl1 = new Client(new XQuery(query1), null, null);
    final Client cl2 = new Client(new XQuery(query2), null, null);
    final boolean await = test.await(2 * SLEEP + SYNC, TimeUnit.MILLISECONDS);
    assertNull(cl1.error, cl1.error);
    assertNull(cl2.error, cl2.error);
    assertEquals(parallel, await, (parallel ? "Parallel" : "Serial") + " execution expected");
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
   * Query no databases.
   * @throws Exception None expected
   */
  @Test public void noDatabase() throws Exception {
    testQueries(Q, Q, true);
  }

    /**
   * Read same database.
   * @throws Exception None expected
   */
  @Test public void readDatabase() throws Exception {
    testQueries(
        f("(db:open('%s'), %s)", NAME, Q),
        f("(db:open('%s'), %s)", NAME, Q),
        true);
  }

  /**
   * Read two different databases.
   * @throws Exception None expected
   */
  @Test public void readDatabases() throws Exception {
    testQueries(
        f("(db:open('%s'), %s)", NAME, Q),
        f("(db:open('%s1'), %s)", NAME, Q),
        true);
  }

  /**
   * Write to the same database twice.
   * @throws Exception None expected
   */
  @Test public void writeDatabase() throws Exception {
    testQueries(
        f("insert node %s into db:open('%s')", Q, NAME),
        f("insert node %s into db:open('%s')", Q, NAME),
        false);
  }

  /**
   * Write to different databases.
   * @throws Exception None expected
   */
  @Test public void writeDatabases() throws Exception {
    testQueries(
        f("insert node %s into db:open('%s')", Q, NAME),
        f("insert node %s into db:open('%s1')", Q, NAME),
        true);
  }

  /**
   * Read from and write to the same database.
   * @throws Exception None expected
   */
  @Test public void readWriteDatabase() throws Exception {
    testQueries(
        f("(db:open('%s'), %s)", NAME, Q),
        f("insert node %s into db:open('%s')", Q, NAME),
        false);
  }

  /**
   * Read from and write to different databases.
   * @throws Exception None expected
   */
  @Test public void readWriteDatabases() throws Exception {
    testQueries(
        f("(db:open('%s'), %s)", NAME, Q),
        f("insert node %s into db:open('%s1')", Q, NAME),
        true);
  }

  /**
   * Read from a database, perform global write lock.
   * @throws Exception None expected
   */
  @Test public void readDatabasesGlobalWrite() throws Exception {
    testQueries(
        f("(db:open('%s'), %s)", NAME, Q),
        f("for $i in ('%s') return insert node %s into db:open($i)", NAME, Q),
        false);
  }

  /**
   * Global write lock twice.
   * @throws Exception None expected
   */
  @Test public void globalWrites() throws Exception {
    testQueries(
        f("for $i in ('%s') return insert node %s into db:open($i)", NAME, Q),
        f("for $i in ('%s') return insert node %s into db:open($i)", NAME, Q),
        false);
  }

  /**
   * Test XQuery locks.
   * @throws Exception none expected
   */
  @Test public void xqueryRead() throws Exception {
    final String prolog = "import module namespace qm='java:org.basex.query.func.QueryModuleTest';";
    testQueries(
      f(prolog + "qm:read-lock(), %s", Q),
      f(prolog + "qm:read-lock(), %s", Q),
      true);
  }

  /**
   * Test XQuery locks.
   * @throws Exception none expected
   */
  @Test public void xqueryWrite() throws Exception {
    final String prolog = "import module namespace qm='java:org.basex.query.func.QueryModuleTest';";
    testQueries(
      f(prolog + "qm:write-lock(), %s", Q),
      f(prolog + "qm:write-lock(), %s", Q),
      false);
  }

  /**
   * Load test.
   * @throws Exception None expected
   */
  @Test public void loadTests() throws Exception {
    final int totalQueries = RUN_COUNT * QUERIES.length;
    final ArrayList<Client> clients = new ArrayList<>(totalQueries);
    final CountDownLatch allDone = new CountDownLatch(totalQueries);

    for(int i = 0; i < RUN_COUNT; i++) {
      for(final String query : QUERIES) {
        clients.add(new Client(new XQuery(f(query, NAME, "1")), null, allDone));
      }
    }

    assertTrue(allDone.await(totalQueries * SLEEP, TimeUnit.MILLISECONDS));
    for(final Client client : clients) {
      assertNull(client.error, client.error);
    }
  }
}
