package main;

import java.io.IOException;

import main.util.Performance;

/**
 * -----------------------------------------------------------------------------
 *
 * This example shows how BaseX commands can be performed via the Java API.
 * The execution time will be printed along with the result of the command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class Example {

  /**
   * Main method, launching the standalone console mode.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new Example();
  }
  
  /**
   * Constructor.
   */
  private Example() {
    Performance perf = new Performance();
    String cmd = "xquery 1 to 10";
    try {
      BaseXClient bxc = new BaseXClient("localhost", 1984, "admin", "admin");
      if(!bxc.execute(cmd, System.out)) {
        System.out.println(bxc.info());
      }
      bxc.close();
      System.out.println(System.getProperty("line.separator") 
          + perf.getTimer());
    } catch(IOException e) {
      e.printStackTrace();
    }
  };
}
