package org.basex.examples.api;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This example shows how commands can be executed via the server instance.
 * The database server must be started first to make this example work.
 * Documentation: http://basex.org/api
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class Example {
  /**
   * Main method, launching the standalone console mode.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    try {
      // initialize timer
      long time = System.nanoTime();

      // create session
      BaseXClient session =
        new BaseXClient("localhost", 1984, "admin", "admin");

      // version 1: perform command and print returned string
      System.out.println(session.execute("info"));

      // version 2 (faster): perform command and pass on result to output stream
      OutputStream out = System.out;
      session.execute("xquery 1 to 10", out);

      // close session
      session.close();

      // print time needed
      double ms = (System.nanoTime() - time) / 1000000d;
      System.out.println("\n\n" + ms + " ms");

    } catch(IOException ex) {
      // print exception
      ex.printStackTrace();
    }
  }
}
