package net.xqj.basex.local;

import org.junit.Test;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
import java.util.ArrayList;

public class XQJConcurrencyTest extends XQJBaseTest
{
  final int THREAD_COUNT = 256;
  final int ITERATE_TO = 1024;

  @Test
  public void testConcurrentXQuery1to1024() throws Throwable
  {
    ArrayList<SimpleQueryThread> sqtList = new ArrayList<SimpleQueryThread>();

    for(int i=0;i<THREAD_COUNT;i++)
      sqtList.add(new SimpleQueryThread());

    for(SimpleQueryThread s : sqtList) s.start();
    for(SimpleQueryThread s : sqtList) s.join();
    for(SimpleQueryThread s : sqtList) if(s.thrown != null) throw s.thrown;
  }

  private class SimpleQueryThread extends Thread
  {
    Throwable thrown = null;

    public void run()
    {
      XQConnection newConnection = null;

      try
      {
        newConnection = xqds.getConnection();

        XQExpression xqpe = newConnection.createExpression();

        XQResultSequence rs = xqpe.executeQuery("1 to "+ITERATE_TO);

        for(int expected=1; expected != ITERATE_TO; expected++)
        {
          if(!rs.next()) {
            thrown = new AssertionError(
              "Expecting a result item, but did not find one.");
            return;
          }

          int value = rs.getInt();

          if(value != expected) {
            thrown = new AssertionError(
              "expected result item '" + expected + "', but got '"+value+"'.");
            return;
          }
        }
      }
      catch(Throwable e) {
        this.thrown = e;
      }
      finally {
        close(newConnection);
      }
    }
  }

  private void close(XQConnection conn) {
    if(conn != null) {
      try {
        conn.close();
      } catch(XQException e) {
        /** ... superfluous ... **/
      }
    }
  }

}
