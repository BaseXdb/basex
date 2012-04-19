package org.basex.examples.api;

import java.io.*;

/**
 * This example shows how new databases can be created.
 *
 * This example required a running database server instance.
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class CreateExample {
  /** Hidden default constructor. */
  private CreateExample() { }

  /**
   * Main method.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    try {
      // create session
      final BaseXClient session = new BaseXClient("localhost", 1984, "admin", "admin");

      try {
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

      } catch(final IOException ex) {
        // print exception
        ex.printStackTrace();
      }

      // close session
      session.close();

    } catch(final IOException ex) {
      // print exception
      ex.printStackTrace();
    }
  }
}
