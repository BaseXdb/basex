package org.basex.test.cs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import org.basex.BaseX;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Delete;
import org.basex.core.proc.Insert;
import org.basex.core.proc.Open;
import org.basex.core.proc.XQuery;
import org.basex.io.PrintOutput;
import org.basex.server.BaseXServerNew;
import org.basex.server.ClientLauncherNew;

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
  /** Server Thread. */
  Thread st;
  /** BaseXServer. */
  BaseXServerNew bxs;
  
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
      final int port = bxs.context.prop.num(Prop.PORT);
      final String host = bxs.context.prop.get(Prop.HOST);
      socket1 = new Socket(host, port);
      socket2 = new Socket(host, port);
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
    read = "for $country in //country for $city in //city where" +
    " $city/name = 'Berlin' and $country/name = 'Germany' return $city";
    startTests();
  }
  
  /**
   * Starts the Server.
   */
  private void startTheServer() {
    st = new Thread() {
      @Override
      public void run() {
        bxs = new BaseXServerNew("-v");
      }
    };
    st.start();
  }
  
  /**
   * Starts the Tests.
   */
  private void startTests() {
    exe(socket1, new Open("factbook"));
    exe(socket2, new Open("factbook"));
    exe(socket1, new XQuery(read));
    exe(socket2, new XQuery(read));
    exe(socket1, new Insert("element", "aa", "0", "//members"));
    exe(socket2, new Delete("//aa"));
    exe(socket1, new XQuery(read));
    BaseX.out("\nEnd of Tests\n");
  }
  
  /**
   * Executes the Process with the specified Socket.
   * @param s Socket
   * @param p Process
   */
  private void exe(final Socket s, final Process p) {
    final ClientLauncherNew proc = new ClientLauncherNew(p, s);
    try {
      final boolean ok = proc.execute();
      if(ok && p.printing()) {
        final PrintOutput out = new PrintOutput(System.out);
        proc.out(out);
        out.close();
      }
    } catch(final FileNotFoundException ex) {
      ex.printStackTrace();
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }
}
