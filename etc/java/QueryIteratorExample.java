package org.basex;

import java.io.IOException;
import org.basex.server.BaseXClient.Query;

/**
 * -----------------------------------------------------------------------------
 *
 * This example shows how BaseX commands can be performed via the Java API.
 * The execution time will be printed along with the result of the command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
public final class QueryIteratorExample {
  /** New line string. */
  private String nl = System.getProperty("line.separator");

  /**
   * Main method, launching the standalone console mode.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new QueryIteratorExample();
  }

  /**
   * Constructor.
   */
  private QueryIteratorExample() {
    long startTime = System.nanoTime();

    String cmd = "1 to zs";

    try {
      BaseXClient session = new BaseXClient("localhost", 1984,
          "admin", "admin");

      System.out.println("=== Query Iterator Version ===");
      
      try {
      Query query = session.query(cmd);
      while(query.hasNext()) {
        System.out.println("Query Result: " + query.next());
      }
      query.close();
      } catch(IOException e) {
        System.out.println(e.getMessage());
      }

      session.close();
      long endTime = System.nanoTime() - startTime;
      System.out.println(nl + endTime / 10000 / 100d + " ms");
    } catch(IOException e) {
      e.printStackTrace();
    }
  };
}
