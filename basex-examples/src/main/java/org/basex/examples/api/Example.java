package org.basex.examples.api;

import java.io.*;

/**
 * This example shows how commands can be executed on a server.
 *
 * This example requires a running database server instance.
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class Example {
  /**
   * Main method.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    try {
      // initialize timer
      final long time = System.nanoTime();

      // create session
      final BaseXClient session = new BaseXClient("localhost", 1984, "admin", "admin");

      // version 1: perform command and print returned string
      System.out.println(session.execute("info"));

      // version 2 (faster): perform command and pass on result to output stream
      final OutputStream out = System.out;
      session.execute("xquery 1 to 10", out);

      // close session
      session.close();

      // print time needed
      final double ms = (System.nanoTime() - time) / 1000000d;
      System.out.println("\n\n" + ms + " ms");

    } catch(final IOException ex) {
      // print exception
      ex.printStackTrace();
    }
  }
}
