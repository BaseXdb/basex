import java.io.IOException;

/**
 * This example shows how queries can be executed in an iterative manner.
 * The database server must be started first to make this example work.
 * Documentation: http://docs.basex.org/wiki/Clients
 *
 * @author BaseX Team 2005-11, BSD License
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
      BaseXClient session =
        new BaseXClient("localhost", 1984, "admin", "admin");

      try {
        // create query instance
        String input = "for $i in 1 to 10 return <xml>Text { $i }</xml>";
        BaseXClient.Query query = session.query(input);

        // initialize iterator
        System.out.print(query.init());

        // loop through all results
        while(query.more()) {
          System.out.println(query.next());
        }

        // close query instance
        System.out.print(query.close());

      } catch(IOException ex) {
        // print exception
        ex.printStackTrace();
      }

      // close session
      session.close();

    } catch(IOException ex) {
      // print exception
      ex.printStackTrace();
    }
  }
}
