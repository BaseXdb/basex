package org.basex.test.query;

import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.util.Performance;

/**
 * XPath Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class QueryTest {
  /** Database Context. */
  private static final Context CONTEXT = new Context();
  /** Test instances. */
  private static final AbstractTest[] TESTS = {
    new SimpleTest(), new XPathMarkFTTest(), new FTTest()
  };
  /** Verbose flag. */
  private static final boolean VERBOSE = false;
  /** Test all flag. */
  private static final boolean ALL = false;
  /** Wrong results counter. */
  private int wrong;
  /** Query counter. */
  private int counter;

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
    Performance p = new Performance();
    Prop.textindex = true;
    Prop.attrindex = true;
    Prop.chop = true;
    boolean ok = true;

    if(ALL) {
      // testing all kinds of combinations 
      for(int a = 0; a < 2; a++) { Prop.ftindex = a == 0;
        for(int b = 0; b < 2; b++) { Prop.ftfuzzy = b == 0;
          for(int c = 0; c < 2; c++) { Prop.ftst = c == 0;
            for(int d = 0; d < 2; d++) { Prop.ftdc = d == 0;
              for(int e = 0; e < 2; e++) { Prop.ftcs = e == 0;
                ok &= test();
              }
            }
          }
        }
      }
    } else {
      // single test
      Prop.ftindex = false;
      Prop.ftfuzzy = true;
      Prop.ftst = false;
      Prop.ftdc = false;
      Prop.ftcs = false;
      ok &= test();
    }

    System.out.println(ok ? "All tests correct." : wrong + " Wrong results...");
    System.out.println(counter + " queries, " + p);
  }

  /**
   * Tests the specified query implementation.
   * @return true if everything went alright
   */
  private boolean test() {
    boolean ok = true;
    for(final AbstractTest test : TESTS) ok &= test(test, test.details());
    return ok;
  }

  /**
   * Tests the specified instance.
   * @param test instance
   * @param ext extended error info
   * @return true if everything went alright
   */
  private boolean test(final AbstractTest test, final String ext) {
    boolean ok = true;
    final String name = test.getClass().getSimpleName();
    final String file = test.doc.replaceAll("\\\"", "\\\\\"");
    Process proc = new CreateDB(file, name);
    if(!proc.execute(CONTEXT)) {
      err(proc.info(), null);
      wrong++;
      return false;
    }

    for(final Object[] qu : test.queries) {
      final boolean correct = qu.length == 3;
      final String query = qu[correct ? 2 : 1].toString();
      final String cmd = qu[0] + ": " + query;
      
      if(VERBOSE) err(cmd, ext);

      proc = new XQuery(query);
      counter++;
      
      if(proc.execute(CONTEXT)) {
        Result val = proc.result();
        final Result cmp = correct ? (Result) qu[1] : null;
        if(val instanceof Nodes && cmp instanceof Nodes) {
          ((Nodes) cmp).data = ((Nodes) val).data;
        }
        if(!correct || !val.same(cmp)) {
          err(cmd, "  Right: " + (correct ? qu[1] : "error") + "\n  Found: " +
              val + (ext != null ? "\n  Flags: " + ext : ""));
          ok = false;
          wrong++;
          continue;
        }
      } else if(correct) {
        err(qu[0].toString(), proc.info() +
            (ext != null ? "\n  Flags: " + ext : ""));
        wrong++;
        ok = false;
      }
    }

    new Close().execute(CONTEXT);
    DropDB.drop(name);
    return ok;
  }

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
