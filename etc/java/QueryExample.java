package org.basex.examples.tmp;

import java.io.IOException;

/**
 * This example shows how queries can be executed in an iterative manner.
 * Documentation: http://basex.org/api
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
public final class QueryExample {
  /**
   * Main method.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new QueryExample();
  }

  /**
   * Constructor.
   */
  private QueryExample() {
    try {
      // initialize timer
      long time = System.nanoTime();

      // create session
      BaseXClient session =
        new BaseXClient("localhost", 1984, "admin", "admin");

      try {
        // create query instance
        String input = "for $i in 1 to 10 return <xml>Text { $i }</xml>";
        BaseXClient.Query query = session.query(input);

        // loop through all results
        while(query.more()) {
          System.out.print(query.next());
        }

        // close query instance
        query.close();

      } catch(IOException ex) {
        // print exception
        ex.printStackTrace();
      }

      // close session
      session.close();

      // print time needed
      double ms = (System.nanoTime() - time) / 1000000d;
      System.out.println("\n" + ms + " ms");

    } catch(IOException ex) {
      // print exception
      ex.printStackTrace();
    }
  };
}
