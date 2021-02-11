package org.basex.examples.api;

import java.io.*;

/**
 * This example shows how new databases can be created.
 *
 * This example requires a running database server instance.
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class CreateExample {
  /**
   * Main method.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String... args) throws IOException {
    // create session
    try(BaseXClient session = new BaseXClient("localhost", 1984, "admin", "admin")) {
      // define input stream
      final InputStream bais =
        new ByteArrayInputStream("<xml>Hello World!</xml>".getBytes());

      // create new database
      session.create("database", bais);
      System.out.println(session.info());

      // run query on database
      System.out.println(session.execute("xquery doc('database')"));

      // drop database
      session.execute("drop db database");
    }
  }
}
