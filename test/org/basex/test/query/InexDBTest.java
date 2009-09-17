package org.basex.test.query;

import static org.basex.Text.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Pattern;
import org.basex.BaseX;
import org.basex.core.ALauncher;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.List;
import org.basex.core.proc.Open;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.io.CachedOutput;
import org.basex.io.PrintOutput;
import org.basex.server.ClientLauncherNew;
import org.basex.util.Args;
import org.basex.util.Performance;
import org.basex.util.StringList;

/**
 * Simple INEX Database test.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class InexDBTest {
  /** Queries. */
  private static final String QUERIES = "inex.queries";
  /** Database prefix (1000 instances: "pages", 10 instances: "inex"). */
  private static final String DBPREFIX = "inex";

  /** Database context. */
  private final Context ctx = new Context();
  /** Launcher. */
  private ALauncher launcher;
  /** Queries. */
  private StringList queries;
  /** Databases. */
  private StringList databases;

  /** Maximum number of databases. */
  private int maxdb = Integer.MAX_VALUE;
  /** Maximum number of queries. */
  private int maxqu = Integer.MAX_VALUE;
  /** Number of runs. */
  private int runs = 1;
  /** Shows process info. */
  private boolean info;
  /** Container for qtimes and results. */
  private PrintOutput res;
  /** Budget for a query in ms. */
  private double  budget = -1;
  /** Remaining query time - used for budget queries.  */
  private double[] rqt;
  
  
  /**
   * Main test method.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new InexDBTest(args);
  }

  /**
   * Default constructor.
   * @param args command-line arguments
   * @throws Exception exception
   */
  private InexDBTest(final String[] args) throws Exception {
    if(!parseArguments(args)) return;

    // cache queries
    final BufferedReader br = new BufferedReader(new FileReader(QUERIES));
    queries = new StringList();
    String l;
    while((l = br.readLine()) != null && queries.size() < maxqu) 
      queries.add(l.substring(l.indexOf('/')));
    br.close();
    rqt = new double[queries.size()];

    // cache database names
    databases = new StringList();
    for(final String s : List.list(ctx)) {
      if(s.startsWith(DBPREFIX) && databases.size() < maxdb) databases.add(s);
    }

    BaseX.outln(BaseX.name(InexDBTest.class) + " [" + CLIENTMODE + "]");
    BaseX.outln("=> % queries on % databases, % runs: time in ms\n",
        queries.size(), databases.size(), runs);

    res = new PrintOutput("times");
    
    // run test
    final Performance p = new Performance();
    test();
    System.out.println("Total Time: " + p.getTimer());
    res.close();
  }

  /**
   * Second test, opening each databases before running the queries.
   * @throws Exception exception
   */
  private void test() throws Exception {
    launcher.execute(new Set(Prop.SERIALIZE, true));
    // loop through all databases
    for(int d = 0; d < databases.size(); d++) {
      // open database and loop through all queries
      launcher.execute(new Open(databases.get(d)));
      for(int q = 0; q < queries.size(); q++) {
        for(int r = 0; r < runs; r++) query(d, q, r == runs - 1);
      }
      launcher.execute(new Close());
    }
  }
  
  /**
   * Performs a single query.
   * @param db database offset
   * @param qu query offset
   * @param s store flag
   * @throws Exception exception
   */
  private void query(final int db, final int qu, final boolean s)
      throws Exception {
    if (budget > -1) {
      final double timer = budget - rqt[qu];
      if (timer <= 0) {
        if (s) {  
          res.println(0 + ";" + 0);
          BaseX.outln("Query % on %: %", qu + 1, databases.get(db), 0);
        }
        return;
      }
      launcher.execute(new Set(Prop.IBT, timer));   
    }
    
    final CachedOutput r = new CachedOutput();
    if(launcher.execute(new XQuery(queries.get(qu)))) {
      launcher.output(r);
      if(!s) return;

      final CachedOutput out = new CachedOutput();
      launcher.info(out);
      final String time = Pattern.compile(".*Total Time: (.*?) ms.*",
          Pattern.DOTALL).matcher(out.toString()).replaceAll("$1");

      // output result
      BaseX.outln("Query % on %: %", qu + 1, databases.get(db), time);
      if(info) {
        BaseX.outln("- " + Pattern.compile(".*Result: (.*?)\\n.*",
            Pattern.DOTALL).matcher(out.toString()).replaceAll("$1"));
      }
      String in = out.toString();
      in = in.substring(in.indexOf("Results") + 
          "Results".length(), in.indexOf("Item")).trim();
      rqt[qu] += Double.parseDouble(time);
      res.println(time + ";" + 
          Integer.parseInt(in.substring(in.indexOf(':') + 2)));
    } else {
      final CachedOutput out = new CachedOutput();
      launcher.info(out);
      BaseX.outln(out.toString());
      res.println(-1 + ";" + -1);
    }
  }
  
  /**
   * Parses the command-line arguments.
   * @param args the command-line arguments
   * @return true if all arguments have been correctly parsed
   */
  private boolean parseArguments(final String[] args) {
    final Args arg = new Args(args);
    boolean ok = true;
    try {
      while(arg.more() && ok) {
        if(arg.dash()) {
          final char c = arg.next();
          if(c == 'd') {
            maxdb = Integer.parseInt(arg.string());
          } else if(c == 'q') {
            maxqu = Integer.parseInt(arg.string());
          } else if(c == 'r') {
            runs = Integer.parseInt(arg.string());
          } else if(c == 'v') {
            info = true;
          } else if(c == 'b') {
            budget = Long.parseLong(arg.string());
          } else {
            ok = false;
          }
        } else {
          ok = false;
        }
      }

      launcher = new ClientLauncherNew(ctx);
      launcher.execute(new Set(Prop.INFO, true));
      launcher.execute(new Set(Prop.ALLINFO, info));
    } catch(final Exception ex) {
      ok = false;
      BaseX.errln("Please run BaseXServerNew for using server mode.");
      ex.printStackTrace();
    }

    if(!ok) {
      BaseX.outln("Usage: " + BaseX.name(this) + " [options]" + NL +
        "  -d<no>  maximum no/database" + NL +
        "  -q<no>  maximum no/queries" + NL +
        "  -r<no>  number of runs" + NL +
        "  -t      measure total time" + NL +
        "  -v      show process info");
    }
    return ok;
  }
}
