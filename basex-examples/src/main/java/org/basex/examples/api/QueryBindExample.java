package org.basex.examples.api;

import org.basex.examples.api.BaseXClient.Query;

import java.io.*;

/**
 * This example shows how external variables can be bound to XQuery expressions.
 *
 * This example requires a running database server instance.
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class QueryBindExample {
  /**
   * Main method.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String... args) throws IOException {
    // create session
    try(BaseXClient session = new BaseXClient("localhost", 1984, "admin", "admin")) {
      // create query instance
      final String input = "declare variable $name external; " +
          "for $i in 1 to 10 return element { $name } { $i }";

      try(Query query = session.query(input)) {
        // bind variable
        query.bind("$name", "number", "");

        // print result
        System.out.print(query.execute());
      }
    }
  }
}
