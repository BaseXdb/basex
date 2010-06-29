import java.io.IOException;

/**
 * -----------------------------------------------------------------------------
 *
 * This example shows how BaseX commands can be performed in an iterative mode.
 * The execution time will be printed along with the result of the command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
public final class QueryIteratorExample {
  /**
   * Main method.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new QueryIteratorExample();
  }

  /**
   * Constructor.
   */
  private QueryIteratorExample() {
    long time = System.nanoTime();

    try {
      // create session
      BaseXClient session =
        new BaseXClient("localhost", 1984, "admin", "admin");

      // run query iterator
      try {
        // perform command and output result
        String result = session.execute("xquery 1 to 3");
        System.out.println(result);

        BaseXClient.Query query = session.query("4 to 6");
        while(query.more()) {
          System.out.println("- " + query.next());
        }
        query.close();
      } catch(IOException ex) {
        ex.printStackTrace();
      }

      // close session
      session.close();

      // print time needed
      double ms = (System.nanoTime() - time) / 1000000d;
      System.out.println("\n" + ms + " ms");
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  };
}
