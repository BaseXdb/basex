package net.xqj.basex.local;

import com.xqj2.XQConnection2;
import static net.xqj.basex.BaseXXQInsertOptions.*;

import net.xqj.basex.BaseXXQInsertOptions;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQResultSequence;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Test XQJ concurrency, both reads and writes
 *
 * @author Charles Foster
 */
public class XQJConcurrencyTest extends XQJBaseTest {

  /** Number of threads used when executing read only queries */
  private static final int CONCURRENT_READ_THREADS = 256;

  /** Numbers of iterations, when perform a ready query */
  private static final int ITERATE_TO = 1024;

  /** Number of threads used when writing documents **/
  private static final int CONCURRENT_WRITE_THREADS = 12;

  /** Total number of documents to insert when writing **/
  private static final int DOCS_TO_INSERT = CONCURRENT_WRITE_THREADS * 30;

  /** BaseX insert strategy for inserting documents **/
  private static final BaseXXQInsertOptions INSERT_STRATEGY = options(REPLACE);

  /**
   * Runs read concurrency test.
   * @throws Throwable any exception or error
   */
  @Test
  public void testConcurrentXQuery1to1024() throws Throwable {
    final ArrayList<SimpleQueryThread> sqtList = new ArrayList<SimpleQueryThread>();

    for(int i = 0; i < CONCURRENT_READ_THREADS; i++)
      sqtList.add(new SimpleQueryThread());

    for(final SimpleQueryThread s : sqtList) s.start();
    for(final SimpleQueryThread s : sqtList) s.join();
    for(final SimpleQueryThread s : sqtList) if(s.thrown != null) throw s.thrown;
  }

  /**
   * Runs insert concurrency test.
   */
  @Test
  public void testConcurrentInsert() throws Exception {

    XQExpression xqpe = xqc.createExpression();

    try
    {
      xqpe.executeCommand("CREATE DB xqj-concurrent-insert-test");
      xqpe.executeCommand("OPEN xqj-concurrent-insert-test");
      xqpe.executeCommand("SET DEFAULTDB true");

      HashMap<String, XQItem> docs = new HashMap<String, XQItem>();

      ThreadPoolExecutor tpe =
        new ThreadPoolExecutor(
          CONCURRENT_WRITE_THREADS, CONCURRENT_WRITE_THREADS, 4l,
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<Runnable>(CONCURRENT_READ_THREADS),
          new ThreadPoolExecutor.CallerRunsPolicy());

      ArrayList<Future> futures = new ArrayList<Future>();

      for(int i=0;i<DOCS_TO_INSERT;i++) {
        String uri = i + "-" + UUID.randomUUID().toString() + ".xml";
        XQItem item = createDocument("<e>" + uri + "</e>");
        docs.put(uri, item);
      }

      for(String uri : docs.keySet())
        futures.add(tpe.submit(new InsertItemThread(uri, docs.get(uri))));

      for(Future future : futures)
        future.get();

      for(String uri : docs.keySet())
        assertTrue(docAvailable(uri));
    }
    finally {
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

  private class InsertItemThread extends Thread {

    /** uri of document being inserted **/
    private final String uri;

    /** content of document being inserted **/
    private final XQItem item;

    public InsertItemThread(String uri, XQItem item) {
      this.uri = uri;
      this.item = item;
    }

    @Override
    public void run() {
      try {
        XQConnection2 xqc2 = (XQConnection2)xqc;
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
  private void close(final XQConnection conn) {
    if(conn != null) {
      try {
        conn.close();
      } catch(final XQException ignored) {
        /* ... superfluous ... */
      }
    }
  }

}
