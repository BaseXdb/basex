package org.basex.server;

import java.io.IOException;
import java.net.Socket;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Delete;
import org.basex.core.proc.Insert;
import org.basex.core.proc.Open;
import org.basex.core.proc.XQuery;

/**
 * Test the 4 Locking Cases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class LockingTest {
  
  /** Socket from Client1.*/
  Socket socket1;
  /** Socket from Client2. */
  Socket socket2;
  /** Read Query. */
  String read;
  /** Context. */
  Context ctx = new Context();
  /**
   * Main method, launching the Test.
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    new LockingTest();
  }
  
  /**
   * Private Constructor.
   */
  private LockingTest() {
    startTheServer();
    try {
      socket1 = new Socket("localhost", Prop.port);
      socket2 = new Socket("localhost", Prop.port);
    } catch(IOException e) {
      e.printStackTrace();
    }
    read = "for $country in //country for $city in //city where" +
    " $city/name = 'Berlin' and $country/name = 'Germany' return $city";
    startTests();
  }
  
  /**
   * Starts the Server.
   */
  private void startTheServer() {
    new Thread() {
      @Override
      public void run() {
        new BaseXServerNew("-v");
      }
    }.start();
  }
  
  /**
   * Starts the Tests.
   */
  private void startTests() {
    new Open("factbook").execute(ctx);
    new ClientProcessNew(socket1, new XQuery(read)).execute(ctx);
    new ClientProcessNew(socket2, new XQuery(read)).execute(ctx);
    new ClientProcessNew(socket1, new Insert("element", "aa", "0",
        "//members")).execute(ctx);
    new ClientProcessNew(socket2, new Delete("//aa")).execute(ctx);
    new ClientProcessNew(socket1, new XQuery(read)).execute(ctx);
  }
}
