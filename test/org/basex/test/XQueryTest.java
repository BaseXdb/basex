package org.basex.test;

import org.basex.core.Commands;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.Proc;
import org.basex.data.Result;
import org.basex.query.xquery.XQResult;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Str;
import org.basex.query.xquery.util.SeqBuilder;
import org.basex.util.Token;

/**
 * XQuery Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
public final class XQueryTest {
  /** Database Context. */
  private final Context context = new Context();
  /** Verbose flag. */
  private static boolean verbose;

  /** Tests. */
  Object[][] queries = new Object[][] {
    { "Int 1", "17", Itr.get(17) },
    { "Dec 1", "0.123", Str.get(Token.token("0.123")) },
    { "Dbl 1", ".456E2", Dbl.get(45.6) },
    { "String 1", "\"Hallo\"", Str.get(Token.token("Hallo")) },
    { "Seq 1", "17,31", new Seq(new Item[] { Itr.get(17), Itr.get(31) }) },
    { "Par 1", "(17)", Itr.get(17) },
    { "Par 2", "(((17)))", Itr.get(17) },
    { "Or 1", "1 or 2", Bln.TRUE },
    { "Or 2", "0 or 0", Bln.FALSE },
    { "Or 3", "0.123 or '!'", Bln.TRUE },
    { "And 1", "1 and ''", Bln.FALSE },
    { "And 2", "5.797 and '!'", Bln.TRUE },
    { "ValueComp 1", "9 lt 9", Bln.FALSE },
    { "ValueComp 2", "9 lt 10", Bln.TRUE },
    { "ValueComp 3", "'9' lt '10'", Bln.FALSE },
    { "ValueComp 4", "'9' lt '10'", Bln.FALSE  },
    { "ValueComp 5", "1 lt '2'" },
    { "ValueComp 6", "'1' lt 2" },
    { "ValueComp 7", "2 lt ('1', 2)" },
    { "ValueComp 8", "'2' lt (1, '2')" },
    { "ValueComp 9", "() lt (1)", Seq.EMPTY },
    { "ValueComp 10", "1 lt ()", Seq.EMPTY },
    { "GeneralComp 1", "2 < (1,2,3)", Bln.TRUE },
    { "GeneralComp 2", "(3,2,1) < 2", Bln.TRUE },
    { "GeneralComp 3", "(1,2,3) < (2,3,4)", Bln.TRUE },
    { "GeneralComp 4", "'2' < (2,3,4)" },
    { "GeneralComp 5", "1 < (2,'3')", Bln.TRUE },
    { "GeneralComp 6", "1 < ('2',3)" },
    { "GeneralComp 7", "'1' < ('2',3)", Bln.TRUE },
    { "GeneralComp 8", "'1' < (2,'3')" },
    { "GeneralComp 9", "'1' < ()", Bln.FALSE }
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
    new XQueryTest();
  }

  /**
   * Constructor.
   */
  private XQueryTest() {
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

    System.out.println(queries.length + " " + " XQuery Tests...");

    out("\nRunning Tests...\n");

    for(final Object[] qu : queries) {
      final String desc = qu[0].toString();
      final String query = qu[1].toString();

      out("- " + desc + " (\"" + query + "\"): ");

      final Proc proc = Proc.get(context, Commands.XQUERY, query);
      if(proc.execute()) {
        final Result value = proc.result();
        final SeqBuilder iter = new SeqBuilder();
        iter.a((Item) qu[2]);
        if(qu.length == 2 || !value.same(new XQResult(null, iter))) {
          err((qu.length == 2 ? "Error" : qu[2]) + " expected, " +
              value + " returned.\n", desc);
          ok = false;
          continue;
        }
        out("ok\n");
      } else {
        final String info = proc.info();
        if(qu.length == 3) {
          err("Error: '" + info + "'\n", desc);
          ok = false;
        } else {
          out("ok\n");
        }
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
    System.out.print((verbose ? "" : info + ": ") + string);
  };
}
