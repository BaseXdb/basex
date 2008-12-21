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
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Dbl;
import org.basex.query.xpath.item.NodeBuilder;
import org.basex.query.xpath.item.Str;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.SeqIter;

/**
 * XPath Test class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
  private static final boolean ALL = true;
  /** Wrong results counter. */
  private static int wrong;

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

    if(ALL) {
      // testing all kinds of combinations 
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
      }
    } else {
      // single test
      Prop.ftindex = true;
      Prop.ftfuzzy = false;
      Prop.ftittr = true;
      Prop.ftst = false;
      Prop.ftdc = false;
      Prop.ftcs = false;
      ok &= test(false);
    }

    System.out.println(ok ? "All tests correct.\n" :
      wrong + " Wrong results...\n");
  }

  /**
   * Tests the specified query implementation.
   * @param xquery use xpath/xquery
   * @return true if everything went alright
   */
  private boolean test(final boolean xquery) {
    boolean ok = true;
    
    for(final AbstractTest test : TESTS) {
     //if (test == TESTS[2]) 
       ok &= test(xquery, test, test.details());
    }
    return ok;
  }

  /**
   * Tests the specified instance.
   * @param xquery use xpath/xquery
   * @param test instance
   * @param ext extended error info
   * @return true if everything went alright
   */
  private boolean test(final boolean xquery, final AbstractTest test,
      final String ext) {
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
      final String cmd = qu[0] + ": " + (xquery ? "xquery " : "xpath ") + query;
      
      if(VERBOSE) err(cmd, ext);

      proc = xquery ? new XQuery(query) : new XPath(query);
      if(proc.execute(CONTEXT)) {
        Result val = proc.result();
        // convert XQuery result to XPath items
        if(val instanceof SeqIter) val = xpath((SeqIter) val);

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
   * Converts the specified XQuery result to an XPath representation.
   * @param val iterator
   * @return xpath item
   */
  private Result xpath(final SeqIter val) {
    try {
      if(val.size == 1) {
        final Item it = val.item[0];
        if(it.type == Type.BLN) return Bln.get(it.bool());
        if(it.n()) return new Dbl(it.dbl());
        if(it.s()) return new Str(it.str());
      }
    
      final NodeBuilder nb = new NodeBuilder();
      for(int i = 0; i < val.size; i++) nb.add(((DBNode) val.item[i]).pre);
      return new Nodes(nb.finish(), CONTEXT.data());
    } catch(final XQException ex) {
      ex.printStackTrace();
      return null;
    }
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
