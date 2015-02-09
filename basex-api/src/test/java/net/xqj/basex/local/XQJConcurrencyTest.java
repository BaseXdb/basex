package net.xqj.basex.local;

import static net.xqj.basex.BaseXXQInsertOptions.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import javax.xml.xquery.*;

import net.xqj.basex.*;

import org.junit.*;

import com.xqj2.*;

/**
 * Test XQJ concurrency, both reads and writes.
 *
 * @author Charles Foster
 */
public final class XQJConcurrencyTest extends XQJBaseTest {
  /** Number of threads used when executing read only queries. */
  private static final int CONCURRENT_READ_THREADS = 256;
  /** Numbers of iterations, when perform a ready query. */
  private static final int ITERATE_TO = 1024;
  /** Number of threads used when writing documents. */
  private static final int CONCURRENT_WRITE_THREADS = 12;
  /** Total number of documents to insert when writing. */
  private static final int DOCS_TO_INSERT = CONCURRENT_WRITE_THREADS * 30;
  /** BaseX insert strategy for inserting documents. */
  private static final BaseXXQInsertOptions INSERT_STRATEGY = options(REPLACE);

  /**
   * Runs read concurrency test.
   * @throws Throwable any exception or error
   */
  @Test
  public void testConcurrentXQuery1to1024() throws Throwable {
    final ArrayList<SimpleQueryThread> sqtList = new ArrayList<>();

    for(int i = 0; i < CONCURRENT_READ_THREADS; i++)
      sqtList.add(new SimpleQueryThread());

    for(final SimpleQueryThread s : sqtList) s.start();
    for(final SimpleQueryThread s : sqtList) s.join();
    for(final SimpleQueryThread s : sqtList) if(s.thrown != null) throw s.thrown;
  }

  /**
   * Runs insert concurrency test.
   * @throws Exception exceptions
   */
  @Test
  public void testConcurrentInsert() throws Exception {
    final XQExpression xqpe = xqc.createExpression();
    try {
      xqpe.executeCommand("CREATE DB xqj-concurrent-insert-test");
      xqpe.executeCommand("OPEN xqj-concurrent-insert-test");
      xqpe.executeCommand("SET DEFAULTDB true");

      final HashMap<String, XQItem> docs = new HashMap<>();

      final ThreadPoolExecutor tpe =
        new ThreadPoolExecutor(
          CONCURRENT_WRITE_THREADS, CONCURRENT_WRITE_THREADS, 4L,
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<Runnable>(CONCURRENT_READ_THREADS),
          new CallerRunsPolicy());

      final ArrayList<Future<?>> futures = new ArrayList<>();

      for(int i = 0; i < DOCS_TO_INSERT; i++) {
        final String uri = i + "-" + UUID.randomUUID() + ".xml";
        final XQItem item = createDocument("<e>" + uri + "</e>");
        docs.put(uri, item);
      }

      for(final Entry<String, XQItem> doc : docs.entrySet())
        futures.add(tpe.submit(new InsertItemThread(doc.getKey(), doc.getValue())));

      for(final Future<?> future : futures)
        future.get();

      for(final String uri : docs.keySet())
        assertTrue(docAvailable(uri));
    } finally {
      xqpe.executeCommand("DROP DB xqj-concurrent-insert-test");
    }
  }

  /**
   * Query Thread.
   */
  private class SimpleQueryThread extends Thread {
    /** Thrown exception or error. */
    Throwable thrown;

    @Override
    public void run() {

      XQConnection newConnection = null;

      try {
        newConnection = xqds.getConnection();

        final XQExpression xqpe = newConnection.createExpression();

        final XQResultSequence rs = xqpe.executeQuery("1 to " + ITERATE_TO);

        for(int expected = 1; expected != ITERATE_TO; expected++) {
          if(!rs.next()) {
            thrown = new AssertionError(
              "Expecting a result item, but did not find one.");
            return;
          }

          final int value = rs.getInt();

          if(value != expected) {
            thrown = new AssertionError(
              "expected result item '" + expected + "', but got '" + value + "'.");
            return;
          }
        }
      } catch(final Throwable th) {
        thrown = th;
      } finally {
        close(newConnection);
      }
    }
  }

  /** Insertion thread. */
  private class InsertItemThread extends Thread {
    /** URI of document being inserted. */
    private final String uri;
    /** Content of document being inserted. */
    private final XQItem item;

    /**
     * Constructor.
     * @param u uri
     * @param it item
     */
    public InsertItemThread(final String u, final XQItem it) {
      uri = u;
      item = it;
    }

    @Override
    public void run() {
      try {
        final XQConnection2 xqc2 = (XQConnection2) xqc;
        xqc2.insertItem(uri, item, INSERT_STRATEGY);
      } catch(final Throwable th) {
        // a JUnit assertion WILL fail later because of this happening.
        th.printStackTrace();
      }
    }
  }

  /**
   * Closes a connection.
   * @param conn connection to be closed
   */
  private static void close(final XQConnection conn) {
    if(conn != null) {
      try {
        conn.close();
      } catch(final XQException ignored) {
        /* ... superfluous ... */
      }
    }
  }

}
