package org.basex.test.query;

import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Proc;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.query.xquery.XQResult;

/**
 * XPath Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
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
    Prop.read();
    Prop.textindex = true;
    Prop.attrindex = true;
    Prop.ftindex = true;
    Prop.ftfuzzy = true;
    Prop.chop = true;

    test(Commands.XPATH);
    test(Commands.XQUERY);
  }
  

  /**
   * Tests the specified query implementation.
   * @param cmd query command
   * @return true if everything went alright
   */
  private boolean test(final Commands cmd) {
    System.out.println("Testing " + cmd);
    boolean ok = true;
    ok &= test(cmd, new SimpleTest());
    ok &= test(cmd, new XPathMarkFTTest());
    ok &= test(cmd, new FTTest());
    System.out.println(ok ? "All tests correct.\n" : "Wrong results...\n");
    return ok;
  }

  /**
   * Tests the specified instance.
   * @param cmd query command
   * @param test instance
   * @return true if everything went alright
   */
  private boolean test(final Commands cmd, final AbstractTest test) {
    boolean ok = true;

    String name = test.getClass().getSimpleName();
    System.out.println(name + " (" + test.queries.length + " queries)...");
    out("\nBuilding database...\n");

    final String file = test.doc.replaceAll("\\\"", "\\\\\"");
    Proc proc = Proc.get(context, Commands.CREATEXML, "\"" + file + "\"");
    if(!proc.execute()) {
      err("\n", proc.info());
      return false;
    }

    out("\nRunning tests...\n");

    for(final Object[] qu : test.queries) {
      out("- " + qu[0] + ": ");
      boolean correct = qu.length == 3;
      String query = qu[correct ? 2 : 1].toString();

      proc = Proc.get(context, cmd, query);
      if(proc.execute()) {
        Result value = proc.result();
        if(cmd == Commands.XQUERY) 
          value = ((XQResult) value).xpResult(context.data());
        
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
        String info = proc.info().replaceAll("Stopped.*\\n(\\[.*?\\] )?", "");
        if(correct) {
          err(info + "\n", qu[0].toString());
          ok = false;
        } else {
          out("ok (" + info + ")\n");
        }
      }
    }
    out("\n");

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
