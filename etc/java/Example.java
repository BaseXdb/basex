import java.io.IOException;
import java.io.OutputStream;

/**
 * This example shows how database commands can be executed.
 * Documentation: http://basex.org/api
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
public final class Example {
  /** Private constructor. */
  private Example() { }

  /**
   * Main method, launching the standalone console mode.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    try {
      // initialize timer
      long time = System.nanoTime();

      // create session
      BaseXClient session =
        new BaseXClient("localhost", 1984, "admin", "admin");

      // version 1: perform command and print returned string
      System.out.println(session.execute("info"));

      // version 2 (faster): perform command and pass on result to output stream
      OutputStream out = System.out;
      session.execute("xquery 1 to 10", out);

      // close session
      session.close();

      // print time needed
      double ms = (System.nanoTime() - time) / 1000000d;
      System.out.println("\n\n" + ms + " ms");

    } catch(IOException ex) {
      // print exception
      ex.printStackTrace();
    }
  };
}
