package org.basex.test;

import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Proc;
import org.basex.data.Nodes;
import org.basex.data.Result;

/**
 * XPath Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class XPathTest {
  /** Database Context. */
  private final Context context = new Context();

  /** Test Information. */
  protected static final String TESTINFO =
    "\nUsage: Test [options]" +
    "\n -h  show this help" +
    "\n -v  show query information";
  /** Verbose flag. */
  private static boolean verbose;

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
    new XPathTest();
  }

  /**
   * Constructor.
   */
  private XPathTest() {
    Prop.read();
    Prop.textindex = true;
    Prop.attrindex = true;
    Prop.ftindex = true;
    Prop.chop = true;

    System.out.println("******** RUN TESTS ********\n");
    boolean ok = true;
    ok &= test(new XPathSimpleTest());
    ok &= test(new XPathMarkFTTest());

    if(ok) System.out.println("All tests successfully passed.");
    else System.out.println("Check your parser..");
  }

  /**
   * Tests the specified instance.
   * @param test instance
   * @return true if everything went alright
   */
  private boolean test(final AbstractTest test) {
    boolean ok = true;

    System.out.println(test.queries.length + " " + test.title + " Tests...");
    out("\nBuilding \"" + test.title + "\"...\n");

    final String file = test.doc.replaceAll("\\\"", "\\\\\"");
    Proc proc = Proc.get(context, Commands.CREATEXML, "\"" + file + "\"");
    if(!proc.execute()) {
      err("\n", proc.info());
      return false;
    }
    
    out("\nRunning Tests...\n");

    for(final Object[] qu : test.queries) {
      out("- " + qu[0] + ": ");

      proc = Proc.get(context, Commands.XPATH, qu[1].toString());
      if(proc.execute()) {
        final Result value = proc.result();
        final Result cmp = (Result) qu[2];
        if(value instanceof Nodes && cmp instanceof Nodes) {
          ((Nodes) cmp).data = ((Nodes) value).data;
        }
        if(qu.length == 2 || !value.same(cmp)) {
          err("\"" + qu[1] + "\":  " + value + " found, " +  (qu.length == 2
              ? "error" : qu[2]) + " expected.\n", qu[0].toString());
          ok = false;
          continue;
        }
        out("ok\n");
      } else {
        final String info = proc.info();
        if(qu.length == 3) {
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
  private void err(final String string, final String info) {
    System.out.print((verbose ? "" : "- " + info + ", ") + string);
  };
}
