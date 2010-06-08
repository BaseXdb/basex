import java.io.IOException;

/**
 * -----------------------------------------------------------------------------
 *
 * This example shows how BaseX commands can be performed via the Java API.
 * The execution time will be printed along with the result of the command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 */
public final class Example {
  /** Privator constructor. */
  private Example() { }
  
  /**
   * Main method, launching the standalone console mode.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    long time = System.nanoTime();

    String cmd = "xquery 1 to 10";

    try {
      BaseXClient session = new BaseXClient("localhost", 1984,
          "admin", "admin");

      System.out.println("=== 1st version with output stream ===");

      session.execute(cmd, System.out);

      System.out.println("\n\n=== 2nd version without output stream ===");

      System.out.println(session.execute(cmd));

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
