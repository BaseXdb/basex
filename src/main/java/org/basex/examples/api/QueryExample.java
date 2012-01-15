package org.basex.examples.api;

import java.io.IOException;

/**
 * This example shows how queries can be executed in an iterative manner.
 * Iterative evaluation will be slower, as more server requests are performed.
 *
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class QueryExample {
  /** Hidden default constructor. */
  private QueryExample() { }

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
        final String input = "for $i in 1 to 10 return <xml>Text { $i }</xml>";
        final BaseXClient.Query query = session.query(input);

        // loop through all results
        while(query.more()) {
          System.out.println(query.next());
        }

        // print query info
        System.out.println(query.info());

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
