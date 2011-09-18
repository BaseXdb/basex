package org.basex.examples.api;

import java.io.IOException;

/**
 * This example shows how external variables can be bound to XQuery expressions.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class QueryBindExample {
  /** Hidden default constructor. */
  private QueryBindExample() { }

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
        // create query instance
        final String input = "declare variable $name external; " +
            "for $i in 1 to 10 return element { $name } { $i }";

        final BaseXClient.Query query = session.query(input);

        // bind variable
        query.bind("$name", "number");

        // print result
        System.out.print(query.execute());

        // close query instance
        query.close();

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
