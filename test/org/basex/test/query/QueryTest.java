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
  /** Extended debugging information. */
  private String ext;

  /** Test Information. */
  static final String TESTINFO =
    "\nUsage: Test [options]" +
    "\n -h  show this help" +
    "\n -v  show query information";

  /**
   * Main method of the test class.
   * @param args command line arguments (ignored)
   */
  public static void main(final String[] args) {
    new QueryTest();
  }

  /**
   * Constructor.
   */
  private QueryTest() {
    Prop.textindex = true;
    Prop.attrindex = true;
    Prop.chop = true;

    boolean ok = true;

    /* testing all kinds of combinations
    for(int x = 0; x < 2; x++) {
      for(int a = 0; a < 2; a++) { Prop.ftindex = a == 0;
        for(int b = 0; b < 2; b++) { Prop.ftittr = b == 0;
          for(int c = 0; c < 2; c++) { Prop.ftfuzzy = c == 0;
            for(int d = 0; d < 2; d++) { Prop.ftst = d == 0;
              for(int e = 0; e < 2; e++) { Prop.ftdc = e == 0;
                for(int f = 0; f < 2; f++) { Prop.ftcs = f == 0;
                  ok &= test(x != 0);
                }
              }
            }
          }
        }
      }
    }*/
    
    // single test
    Prop.ftindex = true;
    Prop.ftittr = true;
    Prop.ftfuzzy = true;
    Prop.ftst = true;
    Prop.ftdc = false;
    Prop.ftcs = true;
    ok &= test(false);
    
    
    System.out.println(ok ? "All tests correct.\n" : "Wrong results...\n");
  }

  /**
   * Tests the specified query implementation.
   * @param xquery use xpath/xquery
   * @return true if everything went alright
   */
  private boolean test(final boolean xquery) {
    boolean ok = true;
    ext = "";
    ok &= test(xquery, new SimpleTest());
    ok &= test(xquery, new XPathMarkFTTest());
    ext = ft();
    ok &= test(xquery, new FTTest());
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
    final String file = test.doc.replaceAll("\\\"", "\\\\\"");
    Process proc = new CreateDB(file);
    if(!proc.execute(context)) {
      err(proc.info(), null);
      return false;
    }

    for(final Object[] qu : test.queries) {
      final boolean correct = qu.length == 3;
      final String query = qu[correct ? 2 : 1].toString();

      proc = xquery ? new XQuery(query) : new XPath(query);
      if(proc.execute(context)) {
        Result val = proc.result();
        if(xquery) val = ((XQResult) val).xpResult(context.data());

        final Result cmp = correct ? (Result) qu[1] : null;
        if(val instanceof Nodes && cmp instanceof Nodes) {
          ((Nodes) cmp).data = ((Nodes) val).data;
        }
        if(!correct || !val.same(cmp)) {
          err(qu[0] + ": " + (xquery ? "xquery " : "xpath ") + query,
              "  Right: " + (correct ? qu[1] : "error") + "\n  Found: " +
              val + (ext != null ? "\n  Flags: " + ext : ""));
          ok = false;
          continue;
        }
      } else if(correct) {
        err(qu[0].toString(), proc.info().replaceAll(
            "Stopped.*\\n(\\[.*?\\] )?", ""));
        ok = false;
      }
    }

    final String db = context.data().meta.dbname;
    new Close().execute(context);
    DropDB.drop(db);
    return ok;
  }

  /**
   * Prints fulltext information.
   * @return string
   */
  private String ft() {
    final StringBuilder sb = new StringBuilder();
    sb.append("index " + Prop.ftindex + ", ");
    sb.append("fz " + Prop.ftfuzzy + ", ");
    sb.append("it " + Prop.ftittr + ", ");
    sb.append("st " + Prop.ftst + ", ");
    sb.append("dc " + Prop.ftdc + ", ");
    sb.append("cs " + Prop.ftcs);
    return sb.toString();
  };

  /**
   * Print specified string to standard output.
   * @param info short info
   * @param detail detailed info
   */
  private void err(final String info, final String detail) {
    System.out.println("- " + info);
    if(detail != null) System.out.println(detail);
  };
}
