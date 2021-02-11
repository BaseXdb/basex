package org.basex.examples.api;

import org.basex.examples.api.BaseXClient.Query;

import java.io.*;

/**
 * This example shows how queries can be executed in an iterative manner.
 * Iterative evaluation will be slower, as more server requests are performed.
 *
 * This example requires a running database server instance.
 * Documentation: https://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class QueryExample {
  /**
   * Main method.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public static void main(final String... args) throws IOException {
    // create session
    try(BaseXClient session = new BaseXClient("localhost", 1984, "admin", "admin")) {
      // create query instance
      final String input = "for $i in 1 to 10 return <xml>Text { $i }</xml>";

      try(Query query = session.query(input)) {
        // loop through all results
        while(query.more()) {
          System.out.println(query.next());
        }

        // print query info
        System.out.println(query.info());
      }
    }
  }
}
