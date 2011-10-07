package org.basex.examples.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This example shows how documents can be added to databases, and how
 * existing documents can be replaced.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class AddExample {
  /** Hidden default constructor. */
  private AddExample() { }

  /**
   * Main method.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    try {
      // create session
      final BaseXClient session =
        new BaseXClient("localhost", 1984, "admin", "admin");

      try {
        // create empty database
        session.execute("create db database");
        System.out.println(session.info());

        // define input stream
        InputStream bais =
          new ByteArrayInputStream("<x>Hello World!</x>".getBytes());

        // add document
        session.add("world.xml", "/world", bais);
        System.out.println(session.info());

        // define input stream
        bais = new ByteArrayInputStream("<x>Hello Universe!</x>".getBytes());

        // add document
        session.add("universe.xml", "", bais);
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
