package org.basex.test.query;

import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XPath;
import org.basex.core.proc.XQuery;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.query.xquery.XQResult;

/**
 * XPath Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class QueryTest {
  /** Database Context. */
  private final Context context = new Context();

  /** Test Information. */
  static final String TESTINFO =
    "\nUsage: Test [options]" +
    "\n -h  show this help" +
    "\n -v  show query information";
  /** Verbose flag. */
  private static boolean verbose = false;

  /**
   * Main method of the test class.
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    if(args.length == 1 && args[0].equals("-v")) {
      verbose = true;
    } else if(args.length > 0) {
      System.out.println(TESTINFO);
      return;
    }
    new QueryTest();
  }

  /**
   * Constructor.
   */
  private QueryTest() {
    Prop.textindex = true;
    Prop.attrindex = true;
    Prop.ftindex = true;
    Prop.ftfuzzy = true;
    Prop.chop = true;

    Prop.ftindex = true;
    test(false);
    Prop.ftindex = false;
    test(false);
    test(true);
  }

  /**
   * Tests the specified query implementation.
   * @param xquery use xpath/xquery
   * @return true if everything went alright
   */
  private boolean test(final boolean xquery) {
    System.out.println("Testing " + (xquery ? "XQuery" : "XPath") +
        " (Index " + (Prop.ftindex ? "ON" : "OFF") + ")");
    boolean ok = true;
    //ok &= test(xquery, new SimpleTest());
    //ok &= test(xquery, new XPathMarkFTTest());
    ok &= test(xquery, new FTTest());
    System.out.println(ok ? "All tests correct.\n" : "Wrong results...\n");
    return ok;
  }

  /**
   * Tests the specified instance.
   * @param xquery use xpath/xquery
   * @param test instance
   * @return true if everything went alright
   */
  private boolean test(final boolean xquery, final AbstractTest test) {
    boolean ok = true;

    final String name = test.getClass().getSimpleName();
    System.out.println(name + " (" + test.queries.length + " queries)...");
    out("\nBuilding database...\n");

    final String file = test.doc.replaceAll("\\\"", "\\\\\"");
    Process proc = new CreateDB(file);
    if(!proc.execute(context)) {
      err("\n", proc.info());
      return false;
    }

    out("\nRunning tests...\n");

    for(final Object[] qu : test.queries) {
      out("- " + qu[0] + ": ");
      final boolean correct = qu.length == 3;
      final String query = qu[correct ? 2 : 1].toString();

      proc = xquery ? new XQuery(query) : new XPath(query);
      if(proc.execute(context)) {
        Result value = proc.result();
        if(xquery) value = ((XQResult) value).xpResult(context.data());

        final Result cmp = correct ? (Result) qu[1] : null;
        if(value instanceof Nodes && cmp instanceof Nodes) {
          ((Nodes) cmp).data = ((Nodes) value).data;
        }
        if(!correct || !value.same(cmp)) {
          err("\"" + query + "\":\n  Expected: " +
              (correct ? qu[1] : "error") +
              "\n  Found: " + value + "\n", qu[0]);
          ok = false;
          continue;
        }
        out("ok\n");
      } else {
        final String info = proc.info().replaceAll(
            "Stopped.*\\n(\\[.*?\\] )?", "");
        if(correct) {
          err(info + "\n", qu[0].toString());
          ok = false;
        } else {
          out("ok (" + info + ")\n");
        }
      }
    }
    out("\n");

    final String db = context.data().meta.dbname;
    new Close().execute(context);
    DropDB.drop(db);
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
  private void err(final String string, final Object info) {
    System.out.print((verbose ? "" : "- " + info + ": ") + string);
  };
}
