package org.basex.examples.api;

import java.io.*;

/**
 * This example shows how documents can be added to databases, and how
 * existing documents can be replaced.
 *
 * This example requires a running database server instance.
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class AddExample {
  /**
   * Main method.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String[] args) throws IOException {
    // create session
    final BaseXClient session = new BaseXClient("localhost", 1984, "admin", "admin");

    try {
      // create empty database
      session.execute("create db database");
      System.out.println(session.info());

      // define input stream
      InputStream bais = new ByteArrayInputStream("<x>Hello World!</x>".getBytes());

      // add document
      session.add("world/world.xml", bais);
      System.out.println(session.info());

      // define input stream
      bais = new ByteArrayInputStream("<x>Hello Universe!</x>".getBytes());

      // add document
      session.add("universe.xml", bais);
      System.out.println(session.info());

      // run query on database
      System.out.println(session.execute("xquery collection('database')"));

      // define input stream
      bais = new ByteArrayInputStream("<x>Hello Replacement!</x>".getBytes());

      // add document
      session.replace("universe.xml", bais);
      System.out.println(session.info());

      // run query on database
      System.out.println(session.execute("xquery collection('database')"));

      // drop database
      session.execute("drop db database");

    } finally {
      // close session
      session.close();
    }
  }
}
