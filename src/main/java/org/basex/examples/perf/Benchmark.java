package org.basex.examples.perf;

import static org.basex.core.Text.*;
import static java.lang.System.*;
import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Check;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.server.ClientSession;
import org.basex.server.LocalSession;
import org.basex.server.Session;
import org.basex.util.Args;
import org.basex.util.Util;

/**
 * This class benchmarks delete operations.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author BaseX Team
 */
public abstract class Benchmark {
  /** Session. */
  private Session session;
  /** Input document. */
  private String input = "etc/xml/factbook.xml";
  /** Number of runs. */
  private int runs = 1;
  /** Maximum number of milliseconds to wait for any query. */
  private int totalMax = Integer.MAX_VALUE;
  /** Maximum number of milliseconds to wait for a single query. */
  private int max = Integer.MAX_VALUE;
  /** Local vs server flag. */
  private boolean local;
  /** Server started flag. */
  private boolean start;

  /**
   * Initializes the benchmark.
   * @param args command-line arguments
   * @return result flag
   * @throws Exception exception
   */
  protected boolean init(final String... args) throws Exception {
    out.println("=== " + Util.name(this) + " Test ===");
    if(!parseArguments(args)) return false;

    final Context ctx = new Context();

    // Check if server is (not) running
    start = !local &&
      !BaseXServer.ping(LOCALHOST, ctx.prop.num(Prop.SERVERPORT));

    if(start) new BaseXServer("");

    session = local ? new LocalSession(ctx) :
      new ClientSession(ctx, "admin", "admin");

    // Create test database
    session.execute(new Set(Prop.QUERYINFO, true));

    drop();
    return true;
  }

  /**
   * Stops the server.
   */
  protected void finish() {
    if(start) BaseXServer.stop(1984);
  }

  /**
   * Opens the test database.
   * @throws BaseXException exception
   */
  protected void check() throws BaseXException {
    session.execute(new Check(input));
  }

  /**
   * Drops the test database.
   * @throws BaseXException exception
   */
  protected void drop() throws BaseXException {
    session.execute(new DropDB(Util.name(this)));
  }

  /**
   * Creates a new database instance and performs a query.
   * @param queries queries to be evaluated
   * @throws Exception exception
   */
  protected void update(final String... queries) throws Exception {
    update(1, queries);
  }

  /**
   * Creates a new database instance and performs a query for the
   * specified number of runs.
   * @param queries queries to be evaluated
   * @param r runs the number for the specified number of time without creating
   *   a new database
   * @throws BaseXException exception
   */
  protected void update(final int r, final String... queries)
      throws BaseXException {

    if(queries.length == 0) return;

    out.print("\n* Queries: " + queries[0]);
    if(queries.length > 1)out.print(", ...");
    if(r > 1) out.print(" (" + r + "x)");
    out.println();

    // minimum time for performing all queries
    double time = Double.MAX_VALUE;
    // number of updated nodes
    int upd = 0;

    // loop through global number of runs
    for(int rr = 0; rr < runs; ++rr) {
      upd = 0;
      double t = 0;
      check();

      // loop through all queries
      for(final String q : queries) {
        // loop through number of runs for a single query
        for(int rn = 0; rn < r; ++rn) {
          session.execute(new XQuery(q));

          final String inf = session.info().replaceAll("\\r?\\n", " ");
          // get number of updated nodes
          upd += Long.parseLong(inf.replaceAll(".*Updated: ([^ ]+).*", "$1"));
          // get execution time
          t += Double.parseDouble(inf.replaceAll(".*Time: ([^ ]+).*", "$1"));
          if(t > totalMax) break;
        }
        if(t > totalMax) break;
      }
      //drop();

      if(time > t) time = t;
      if(t > totalMax) {
        time = -1;
        break;
      }
      if(t > max) break;
    }
    out.println("  Nodes: " + upd);
    out.println("  ms: " + Math.round(time));
  }

  /**
   * Performs the specified query and returns the result.
   * @param query query to be evaluated
   * @return result
   * @throws BaseXException exception
   */
  protected String query(final String query) throws BaseXException {
    check();
    return session.execute(new XQuery(query));
  }

  /**
   * Parses the command-line arguments.
   * @param args command-line arguments
   * @return true if all arguments have been correctly parsed
   */
  protected boolean parseArguments(final String[] args) {
    final Args arg = new Args(args, this,
        " [-lr] document\n" +
        " -l        use local session\n" +
        " -m<max>   maximum #ms for a single query\n" +
        " -M<max>   total maximum #ms\n" +
        " -r<runs>  number of runs");

    while(arg.more()) {
      if(arg.dash()) {
        final char ch = arg.next();
        if(ch == 'r') {
          runs = arg.num();
          out.println("- number of runs: " + runs);
        } else if(ch == 'l') {
          local = true;
          out.println("- local session");
        } else if(ch == 'm') {
          max = arg.num();
          out.println("- maximum #ms: " + max);
        } else if(ch == 'M') {
          totalMax = arg.num();
          out.println("- total maximum #ms: " + totalMax);
        } else {
          arg.check(false);
        }
      } else {
        input = arg.string();
        out.println("- Document: " + input);
      }
    }
    return arg.finish();
  }
}
