package org.basex;

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
  
  /** New line string. */
  private String nl = System.getProperty("line.separator");
  
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
    long startTime = System.nanoTime();
    
    String cmd = "xquery 1 to 10";
    
    try {
      BaseXClient session = new BaseXClient("localhost", 1984,
          "admin", "admin");
      
      System.out.println("=== 1st version with output stream ===");
      
      if(!session.execute(cmd, System.out)) {
        System.out.println(session.info());
      }
      
      System.out.println(nl + nl + "=== 2nd version without output stream ===");
      
      if(session.execute(cmd)) {
        System.out.println(session.result());
      } else {
        System.out.println(session.info());
      }
      
      session.close();
      long endTime = System.nanoTime() - startTime;
      System.out.println(nl + endTime / 10000 / 100d + " ms");
    } catch(IOException e) {
      e.printStackTrace();
    }
  };
}
