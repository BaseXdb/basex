package org.basex.test;

import java.io.*;

import org.basex.util.Performance;
import org.basex.index.CTArrayNew;

/**
 * Main memory index test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class IndexTest {
  /** Number of tokens to create. */
  private int nrTokens = 50000;

  /** String array for testing. */
  private String[] copy;
  /** String array for testing. */
  private String[] indexed;
  
  /**
   * Main method.
   * @param args command line arguments.
   * You can specify two arguments:
   * the name of your index structure and
   * the number of tokens to test with
   */
  public static void main(final String[] args) {
    // start test...
    IndexTest test = new IndexTest();

    // check if the number of tokens was specified
    if(args.length > 0 && !test.setSize(args[0])) {
      System.out.println("Could not interprete the value \"" + args[0] + "\"");
      System.out.println("Usage: java IndexTest <number of tokens>");
    } else {
      System.out.println("Number of tokens: " + test.nrTokens);
      // perform test
      test.run();
    }
  }

  /**
   * Performs the test.
   */
  public void run() {
    // create arrays with random tokens (duplicates are possible)
    System.out.println("\nCreating random tokens...");
    //initTimer();

    // fill test arrays with random tokens
    indexed = new String[nrTokens];
    copy = new String[nrTokens];
    for(int i = 0; i < nrTokens; i++) {
      indexed[i] = randomToken();
      copy[i] = new String(indexed[i].toString());
    }
    // show performance results
    System.out.println(nrTokens + " tokens created.");
    //printTimer();
    Performance.gc(5);
    System.out.println(Performance.getMem());
    //printMem();

    // add tokens to index
    System.out.println("\nBuilding index...");
    //initTimer();

    // create instance of token index
    CTArrayNew index = new CTArrayNew();
    // index all tokens and get their reference
    for(int i = 0; i < nrTokens; i++) {
      //System.out.println(indexed[i]);
      index.index(indexed[i].getBytes(), i, i * nrTokens);
    }
    
    //int indexSize = index.size();

    // show performance results & reinitialize timer
    System.out.println(nrTokens + " tokens indexed.");
    //intTimer();
    //printMem();
    Performance.gc(5);
    System.out.println(Performance.getMem());
    
    // if size of array and index are equal, tokens might have been stored
    // several times
    if(nrTokens > 100) {
      System.out.println("- Index might contain duplicate tokens.");
    }
    
    // request all existing tokens
    System.out.println("\nRequesting indexed tokens...");
    //initTimer();
    int[][] data;
    for(int i = 0; i < nrTokens; i++) {
      //System.out.println(copy[i]);
      data = index.getNodeFromTrie(copy[i].getBytes());
      if(data == null) {
        System.out.println("- Token " + indexed[i] + " was not found.");
        break;
      } else if (data[0][0] != i && data[1][0] != i * nrTokens) {
        System.out.println(CTArrayNew.intArrayToString(data[0]));
        System.out.println(CTArrayNew.intArrayToString(data[1]));
      }
    }
    // show performance results & reinitialize timer
    //printTimer();

    // check if results are correct
    /*for(int i = 0; i < nrTokens; i++) {
      // wrong token returned
      if(indexed[i] != copy[i]) {
        if(!indexed[i].equals(copy[i])) {
          System.out.println("- Index returns wrong tokens: " +
              indexed[i] + " expected, " + copy[i] + " returned");
          break;
        }
        System.out.println("- Different references for " +
            "the token \"" + indexed[i] + "\".");
        break;
      }
    }
    */
    // create new random tokens
    indexed = null;
    for(int i = 0; i < nrTokens; i++) {
      copy[i] = randomToken();
    }

    // request random tokens
    System.out.println("\nRequesting random tokens...");
    //initTimer();

    for(int i = 0; i < nrTokens; i++) {
      // find token in index (ignore result)
      index.getNodeFromTrie(copy[i].getBytes());
    }
    // show performance results & reinitialize timer
    //printTimer();
    
    // launch iterator and count returned tokens
    //System.out.println("\nIterating indexed tokens...");
    //initTimer();

   /* copy = null;
    index.init();
    int size2 = 0;
    while(index.more()) {
      index.next();
      size2++;
    }
    // wrong number of returned tokens (shouldn't happen)
    if(indexSize != size2) {
      System.out.println("- Iterator returned " + size2 + 
          " instead of " + indexSize + " tokens.");
    }

    // show performance results & reinitialize timer
    printTimer();*/
  }


  /**
   * Waits for user input.
   * @return user input
   */
  String input() {
    try {
      InputStreamReader isr = new InputStreamReader(System.in);
      return new BufferedReader(isr).readLine().trim(); 
    } catch(Exception e) {
      return "";
    }
  }

  /**
   * Sets the number of test tokens.
   * @param input input string
   * @return true if input string was valid
   */
  boolean setSize(final String input) {
    try {
      nrTokens = Integer.parseInt(input);
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  /**
   * Returns a randomly created token.
   * @return random token
   */
  String randomToken() {
    StringBuilder sb = new StringBuilder();
    // calculate random token length (1 - 10 characters)
    int len = (int) (Math.random() * 10 + 1);
    for(int k = 0; k < len; k++) {
      // add random letter
      sb.append((char) (Math.random() * 26 + 65));
    }
    return sb.toString();
  }
}
