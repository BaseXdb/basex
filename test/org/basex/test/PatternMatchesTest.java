package org.basex.test;

import java.util.regex.Pattern;

/**
 * Test class for regular patterns.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz
 */
public final class PatternMatchesTest {
  
  /** Private constructor. */
  private PatternMatchesTest() { }
  
  /**
   * Main method.
   * @param args command-line arguments (ignored)
   */
  public static void main(final String[] args) {

    String[] regex = new String[5];
    regex[0] = ".*\\.pdf";  // uebersetzung von *.pdf
    regex[1] = "a.d\\.pdf"; // uebersetzung von a?d.pdf  
    regex[2] = "[abc]dd.pdf";  // uebersetzung von [abc]dd.pdf  
    regex[3] = "[a-c]dd.pdf"; // uebersetzung von [a-c]dd.pdf
    regex[4] = "[a-c].d\\.p.*"; // uebersetzung von [a-c]?d.p*
    
    String[] input = new String[3];
    input[0] = "add.pdf";
    input[1] = "dpdf";
    input[2] = "cdd.pdf";

    for(String i : regex) {
      System.out.println("Pattern: " + i);
      for(String j : input) {
        boolean isMatch = Pattern.matches(i, j);
        System.out.println(isMatch + " " + j);        
      }
      System.out.println();
    }
  }
}
