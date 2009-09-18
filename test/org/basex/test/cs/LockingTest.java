package org.basex.test.cs;

import java.io.IOException;
import org.basex.BaseXServer;
import org.basex.core.ALauncher;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.proc.Delete;
import org.basex.core.proc.Insert;
import org.basex.core.proc.Open;
import org.basex.core.proc.XQuery;
import org.basex.io.NullOutput;
import org.basex.server.ClientLauncher;

/**
 * This class tests the four locking cases.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class LockingTest {
  /** Database Context. */
  private Context context = new Context();
  /** Read Query. */
  String read;

  /**
   * Main method, launching the test.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new LockingTest();
  }

  /**
   * Private constructor.
   * @throws Exception exception
   */
  private LockingTest() throws Exception {
    startTheServer();

    read = "for $country in //country for $city in //city where"
        + " $city/name = 'Berlin' and $country/name = 'Germany' return $city";
    int cnr = 2;
    for(int i = 0; i < cnr; i++) {
      startAClient(new ClientLauncher(context), i);
    }
  }

  /**
   * Starts the server.
   */
  private void startTheServer() {
    new Thread() {
      @Override
      public void run() {
        new BaseXServer("-v");
      }
    }.start();
  }

  /**
   * Starts a client.
   * @param client client reference
   * @param c int
   */
  private void startAClient(final ALauncher client, final int c) {
    new Thread() {
      int check = c;
      @Override
      public void run() {
        exe(client, new Open("factbook"));
        exe(client, new XQuery(read));
        if(check % 2 == 0) {
          exe(client, new Insert("element", "//members", "aa"));
        } else {
          exe(client, new Delete("//aa"));
        }
      }
    }.start();
  }

  /**
   * Executes the ClientLauncher.
   * @param client client reference
   * @param pr process to be executed
   */
  void exe(final ALauncher client, final Process pr) {
    try {
      if(client.execute(pr)) client.output(new NullOutput());
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }
}
