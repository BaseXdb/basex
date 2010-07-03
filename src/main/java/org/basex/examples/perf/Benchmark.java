package org.basex.examples.perf;

import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.util.Args;
import org.basex.util.Performance;

/**
 * This class benchmarks delete operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public abstract class Benchmark {
  /** Global context. */
  private final Context context = new Context();
  /** Performance. */
  private final Performance perf = new Performance();
  /** Input document. */
  private String input;
  /** Number of runs. */
  private int runs = 1;

  /**
   * Private constructor.
   * @param args command-line arguments
   * @return result flag
   * @throws Exception exception
   */
  protected boolean init(final String... args) throws Exception {
    System.out.println("=== " + Main.name(this) + " Test ===\n");

    if(!parseArguments(args)) return false;

    // create test database
    new Set("info", "all").execute(context);
    return true;
  }

  /**
   * Finishes the test.
   * @throws Exception exception
   */
  protected void finish() throws Exception {
    new DropDB(Main.name(this)).execute(context);
  }

  /**
   * Performs an update query for the specified number of times
   * and prints the minimum time.
   * @param query query to be evaluated
   * @throws Exception exception
   */
  protected void update(final String query) throws Exception {
    System.out.println("* Query: " + query);
    double time = Double.MAX_VALUE;
    String updated = "";
    for(int r = 0; r < runs; r++) {
      new CreateDB(Main.name(this), input).execute(context);
      perf.getTime();
      final Command cmd = new XQuery(query);
      cmd.execute(context);
      final String inf = cmd.info().replaceAll("\\r?\\n", " ");
      final double t = Double.parseDouble(
          inf.replaceAll(".*Time: ([^ ]+).*", "$1"));
      if(time > t) time = t;
      updated = inf.replaceAll(".*Updated: (\\d+).*", "$1");
    }
    System.out.println("* Nodes: " + updated);
    System.out.println("* MS: " + time);
    System.out.println();
  }

  /**
   * Parses the command-line arguments.
   * @param args command-line arguments
   * @return true if all arguments have been correctly parsed
   */
  protected boolean parseArguments(final String[] args) {
    final Args arg = new Args(args, this, " doc\n -r  number of runs");
    while(arg.more()) {
      if(arg.dash()) {
        arg.check(false);
        final char ch = arg.next();
        if(ch == 'r') {
          runs = arg.num();
          System.out.println("* Runs: " + runs);
        } else {
          arg.check(false);
        }
      } else {
        input = arg.string();
      }
    }
    if(input == null) arg.check(false);
    return arg.finish();
  }
}
