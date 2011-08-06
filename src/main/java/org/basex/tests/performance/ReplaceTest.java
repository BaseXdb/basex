package org.basex.tests.performance;

import java.util.Random;

import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.data.DataText;
import org.basex.util.Performance;

/**
 * This class performs a local stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ReplaceTest {
  /** Number of runs per client. */
  private static final int NQUERIES = 10000;
  /** Global context. */
  static final Context CONTEXT = new Context();
  /** Random number generator. */
  static final Random RND = new Random();
  /** Result counter. */
  static int counter;

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    System.out.println("=== ReplaceTest ===");

    new Set(Prop.TEXTINDEX, false).execute(CONTEXT);
    new Set(Prop.ATTRINDEX, false).execute(CONTEXT);

    final Performance perf = new Performance();

    // Create test database
    System.out.println("\n* Create test database.");
    Command cmd = new CreateDB("test", "<X><A/><A/></X>");
    cmd.execute(CONTEXT);
    System.out.print(cmd.info());

    cmd = new XQuery("for $a in //A " +
        "return replace value of node $a with '01234567890.12345678'");
    cmd.execute(CONTEXT);

    long len1 = CONTEXT.data.meta.dbfile(DataText.DATATXT).length();

    // deactivate flushing to speed up querying
    new Set(Prop.FORCEFLUSH, false).execute(CONTEXT);

    // replace texts with random doubles
    final Random rnd = new Random();
    for(int i = 0; i < NQUERIES; i++) {
      final double d = rnd.nextDouble();
      cmd = new XQuery("for $a in //A return replace node $a/text() with " + d);
      cmd.execute(CONTEXT);
    }

    // perform final, flushed replacement
    new Set(Prop.FORCEFLUSH, true).execute(CONTEXT);

    cmd = new XQuery("for $a in //A " +
        "return replace value of node $a with '01234567890.12345678'");
    cmd.execute(CONTEXT);

    System.out.print("\n* Old vs. new size... ");
    long len2 = CONTEXT.data.meta.dbfile(DataText.DATATXT).length();
    System.out.println(len1 + " vs. " + len2);

    // Drop database
    System.out.println("\n* Drop test database.");

    cmd = new DropDB("test");
    //cmd.execute(CONTEXT);
    System.out.print(cmd.info());

    CONTEXT.close();

    System.out.println("\n* Time: " + perf);
  }
}
