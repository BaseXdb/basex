package org.basex.test;

import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Proc;

/**
 * Pathfinder Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class PathfinderTest {
  /** Database Context. */
  protected final Context context = new Context();
  /** Verbose flag. */
  private static boolean verbose;

  /** Tests. */
  String[][] queries = new String[][] {
    { "1", "1" },
    { "-1", "-1" },
    { "--1", "1" },
    { "1-1", "0" },
    { "3-2-1", "0" },
    { "1.1 * 2.2", "2.42" },
    { "-1.1 * 2.2", "-2.42" },
    { "20 mod 17", "3" },
    { "2.0 <= 5", "true" },
    { "7 > 6", "true" },
  };
  
  /**
   * Main method of the test class.
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    if(args.length == 1 && args[0].equals("-v")) {
      verbose = true;
    } else if(args.length > 0) {
      System.out.println(XPathTest.TESTINFO);
      return;
    }
    new PathfinderTest();
  }

  /**
   * Constructor.
   */
  private PathfinderTest() {
    System.out.println("******** RUN TESTS ********\n");

    if(test()) System.out.println("All tests successfully passed.");
    else System.out.println("Check your parser..");
  }

  /**
   * Performs the tests.
   * @return true if everything went alright
   */
  private boolean test() {
    boolean ok = true;
    Prop.serialize = false;

    System.out.println(queries.length + " " + " Pathfinder Tests...");

    out("\nRunning Tests...\n");

    for(final String[] qu : queries) {
      out("- " + qu[0] + ": ");
      final Proc proc = Proc.get(context, Commands.PF, qu[0]);
      if(proc.execute()) {
        final String val = proc.result().toString().
          replaceAll("^.*\\[|\\].*$", "");
    
        if(!val.equals(qu[1])) {
          err("\"" + qu[1] + "\": " + val + " found, " + qu[1] +
              " expected.\n", qu[0]);
          ok = false;
        } else {
          out("ok\n");
        }
      } else {
        err(proc.info() + "\n", qu[0]);
        ok = false;
      }
    }
    out("\n");

    Proc.execute(context, Commands.DROP, "temp");
    return ok;
  }

  /**
   * Print specified string to standard output.
   * @param string string to be printed
   */
  private void out(final String string) {
    if(verbose) System.out.print(string);
  };

  /**
   * Print specified string to standard output.
   * @param string string to be printed
   * @param info additional info
   */
  private void err(final String string, final String info) {
    System.out.print((verbose ? "" : "- " + info + ", ") + string);
  };
}
