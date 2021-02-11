package org.basex.examples.api;

import java.io.*;

/**
 * This example shows how commands can be executed on a server.
 *
 * This example requires a running database server instance.
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class Example {
  /**
   * Main method.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String... args) throws IOException {
    // create session
    try(BaseXClient session = new BaseXClient("localhost", 1984, "admin", "admin")) {
      // initialize timer
      final long time = System.nanoTime();

      // version 1: perform command and print returned string
      System.out.println(session.execute("info"));

      // version 2 (faster): perform command and pass on result to output stream
      final OutputStream out = System.out;
      session.execute("xquery 1 to 10", out);

      // print time needed
      final double ms = (System.nanoTime() - time) / 1000000.0d;
      System.out.println("\n\n" + ms + " ms");
    }
  }
}
