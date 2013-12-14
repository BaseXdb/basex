package net.xqj.basex.local;

import org.junit.Test;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
import java.util.ArrayList;

/**
 * XQJ concurrency test.
 *
 * @author Charles Foster
 */
public class XQJConcurrencyTest extends XQJBaseTest {
  /** Thread count. */
  private static final int THREAD_COUNT = 256;
  /** Numbers of iterations. */
  private static final int ITERATE_TO = 1024;

  /**
   * Runs concurrency test.
   * @throws Throwable any exception or error
   */
  @Test
  public void testConcurrentXQuery1to1024() throws Throwable {
    final ArrayList<SimpleQueryThread> sqtList = new ArrayList<SimpleQueryThread>();

    for(int i = 0; i < THREAD_COUNT; i++)
      sqtList.add(new SimpleQueryThread());

    for(final SimpleQueryThread s : sqtList) s.start();
    for(final SimpleQueryThread s : sqtList) s.join();
    for(final SimpleQueryThread s : sqtList) if(s.thrown != null) throw s.thrown;
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
