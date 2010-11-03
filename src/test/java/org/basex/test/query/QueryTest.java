package org.basex.test.query;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.iter.ItemIter;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the database commands.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class QueryTest {
  /** Document. */
  protected static String doc;
  /** Queries. */
  protected static Object[][] queries;
  /** Database context. */
  protected static Context context;

  /** Prepares tests. */
  @BeforeClass
  public static void startTest() {
    context = new Context();
    context.prop.set(Prop.CACHEQUERY, true);
  }

  /**
   * Finish the test.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void stopTest() throws BaseXException {
    new DropDB(Util.name(QueryTest.class)).execute(context);
    context.close();
  }

  /**
   * Tests the specified instance.
   * @throws BaseXException database exception
   */
  @Test
  public void test() throws BaseXException {
    final String file = doc.replaceAll("\\\"", "\\\\\"");
    final String name = Util.name(this);
    final boolean up = this instanceof XQUPTest;
    new CreateDB(name, file).execute(context);

    final StringBuilder sb = new StringBuilder();
    int fail = 0;

    for(final Object[] qu : queries) {
      // added to renew document after each update test
      final String title = (String) qu[0];
      if(up && title.startsWith("xxx")) {
        new CreateDB(name, file).execute(context);
      }

      final boolean correct = qu.length == 3;
      final String query = qu[correct ? 2 : 1].toString();

      final Command c = new XQuery(query);
      try {
        c.execute(context);
        final Result val = c.result();
        final Result cmp = correct ? (Result) qu[1] : null;
        if(val instanceof Nodes && cmp instanceof Nodes) {
          ((Nodes) cmp).data = ((Nodes) val).data;
        }
        if(!correct || !val.sameAs(cmp)) {
          sb.append("-- " + qu[0] + ": " + query + "\n[E] " + (correct ?
              qu[1] : "error") + "\n[F] " + val + " " + details() + "\n");
          ++fail;
        }
      } catch(final BaseXException ex) {
        if(correct) {
          sb.append("-- " + qu[0] + ": " + query + "\n[E] " +
              qu[1] + "\n[F] " + ex.getMessage() + " " + details() + "\n");
          ++fail;
        }
      }
    }
    if(fail != 0) fail(fail + " wrong queries; [E] expected, [F] found:\n" +
        sb.toString().trim());
  }

  /**
   * Returns property details.
   * @return details
   */
  protected String details() {
    return "";
  }

  /**
   * Creates a container for the specified node values.
   * @return node array
   */
  static ItemIter empty() {
    return new ItemIter(new Item[] {}, 0);
  }

  /**
   * Creates a container for the specified node values.
   * @param nodes node values
   * @return node array
   */
  static Nodes nod(final int... nodes) {
    return new Nodes(nodes);
  }

  /**
   * Creates an iterator for the specified string.
   * @param str string
   * @return iterator
   */
  static ItemIter str(final String... str) {
    final ItemIter ii = new ItemIter();
    for(final String s : str) ii.add(Str.get(s));
    return ii;
  }

  /**
   * Creates an iterator for the specified double.
   * @param d double value
   * @return iterator
   */
  static ItemIter dbl(final double d) {
    return item(Dbl.get(d));
  }

  /**
   * Creates an iterator for the specified integer.
   * @param d double value
   * @return iterator
   */
  static ItemIter itr(final long... d) {
    final ItemIter ii = new ItemIter();
    for(final long dd : d) ii.add(Itr.get(dd));
    return ii;
  }

  /**
   * Creates an iterator for the specified boolean.
   * @param b boolean value
   * @return iterator
   */
  static ItemIter bool(final boolean b) {
    return item(Bln.get(b));
  }

  /**
   * Creates an iterator for the specified item.
   * @param i item
   * @return iterator
   */
  private static ItemIter item(final Item i) {
    return new ItemIter(new Item[] { i }, 1);
  }
}
