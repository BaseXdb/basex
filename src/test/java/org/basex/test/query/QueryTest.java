package org.basex.test.query;

import static org.junit.Assert.*;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Proc;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.XQuery;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.query.item.Bln;
import org.basex.query.item.Dec;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;
import org.basex.util.Token;
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

  /** Database Context. */
  protected static Context context;

  /** Prepares tests. */
  @BeforeClass
  public static void startTest() {
    context = new Context();
    context.prop.set(Prop.CACHEQUERY, true);
  }

  /** Finish test. */
  @AfterClass
  public static void stopTest() {
    new DropDB(Main.name(QueryTest.class)).exec(context);
    context.close();
  }

  /**
   * Tests the specified instance.
   */
  @Test
  public void test() {
    final String file = doc.replaceAll("\\\"", "\\\\\"");
    final String name = Main.name(this);
    final boolean up = this instanceof XQUPTest;
    exec(new CreateDB(name, file));

    for(final Object[] qu : queries) {
      // added to renew document after each update test
      if(up && ((String) qu[0]).startsWith("xxx")) {
        exec(new CreateDB(name, file));
      }

      final boolean correct = qu.length == 3;
      final String query = qu[correct ? 2 : 1].toString();
      final String cmd = qu[0] + ": " + query;

      final Proc proc = new XQuery(query);
      if(proc.exec(context)) {
        final Result val = proc.result();
        final Result cmp = correct ? (Result) qu[1] : null;
        if(val instanceof Nodes && cmp instanceof Nodes) {
          ((Nodes) cmp).data = ((Nodes) val).data;
        }
        if(!correct || !val.same(cmp)) {
          fail(cmd + ": Right: " + (correct ? qu[1] : "error") + "\n  Found: " +
              val + "\n" + details());
        }
      } else if(correct) {
        fail(qu[0] + ": " + proc.info() + details());
      }
    }
  }

  /**
   * Runs a command.
   * @param proc process to be run
   */
  private static void exec(final Proc proc) {
    if(!proc.exec(context)) fail(proc.info());
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
   * @param nodes node values
   * @return node array
   */
  static Nodes nodes(final int... nodes) {
    return new Nodes(nodes);
  }

  /**
   * Creates an iterator for the specified string.
   * @param str string
   * @return iterator
   */
  static SeqIter string(final String str) {
    return item(Str.get(Token.token(str)));
  }

  /**
   * Creates an iterator for the specified double.
   * @param d double value
   * @return iterator
   */
  static SeqIter itr(final long d) {
    return item(Itr.get(d));
  }

  /**
   * Creates an iterator for the specified double.
   * @param d double value
   * @return iterator
   */
  static SeqIter dec(final double d) {
    return item(Dec.get(d));
  }

  /**
   * Creates an iterator for the specified boolean.
   * @param b boolean value
   * @return iterator
   */
  static SeqIter bool(final boolean b) {
    return item(Bln.get(b));
  }

  /**
   * Creates an iterator for the specified item.
   * @param i item
   * @return iterator
   */
  private static SeqIter item(final Item i) {
    return new SeqIter(new Item[] { i }, 1);
  }
}
