package org.basex.test.query;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.test.*;
import org.basex.test.query.simple.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the database commands.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class QueryTest extends SandboxTest {
  /** Document. */
  protected static String doc;
  /** Queries. */
  protected static Object[][] queries;

  /** Prepares the tests. */
  @BeforeClass
  public static void startTest() {
    context.prop.set(Prop.CACHEQUERY, true);
  }

  /**
   * Tests the specified instance.
   * @throws BaseXException database exception
   */
  @Test
  public void test() throws BaseXException {
    final String file = doc.replaceAll("\"", "\\\\\"");
    final String name = Util.name(QueryTest.class);
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
      final Result cmp = correct ? (Result) qu[1] : null;

      final Command c = new XQuery(query);
      try {
        c.execute(context);
        final Result val = c.result();
        if(cmp instanceof Nodes) {
          ((Nodes) cmp).data = context.data();
        }

        if(!correct || !val.sameAs(cmp)) {
          sb.append("[" + qu[0] + "] " + query);
          String s = correct && cmp.size() != 1 ? "#" + cmp.size() : "";
          sb.append("\n[E" + s + "] ");
          if(correct) {
            final String cp = cmp.toString();
            sb.append('\'');
            sb.append(cp.length() > 1000 ? cp.substring(0, 1000) + "..." : cp);
            sb.append('\'');
          } else {
            sb.append("error");
          }
          final TokenBuilder types = new TokenBuilder();
          if(val instanceof ValueBuilder) {
            final ValueBuilder vb = (ValueBuilder) val;
            for(final Item i : vb) types.add(i.type.toString()).add(" ");
          } else {
            types.add(Util.name(val));
          }
          s = val.size() != 1 ? "#" + val.size() : "";
          sb.append("\n[F" + s + "] '" + val + "', " + types +
            details() + '\n');
          ++fail;
        }
      } catch(final Exception ex) {
        final String msg = ex.getMessage();
        if(correct || msg == null || msg.contains("mailman")) {
          final String cp = correct && (!(cmp instanceof Nodes) ||
              ((Nodes) cmp).data != null) ? cmp.toString() : "()";
          sb.append("[" + qu[0] + "] " + query + "\n[E] " +
              cp + "\n[F] " +
              (msg == null ? Util.name(ex) : msg.replaceAll("\r\n?|\n", " ")) + ' ' +
              details() + '\n');
          ++fail;
        }
      }
    }
    if(fail != 0) fail(fail + " Errors. [E] = expected, [F] = found:\n" +
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
  protected static ValueBuilder empty() {
    return new ValueBuilder();
  }

  /**
   * Creates a container for the specified node values.
   * @param nodes node values
   * @return node array
   */
  protected static Nodes node(final int... nodes) {
    return new Nodes(nodes);
  }

  /**
   * Creates an iterator for the specified string.
   * @param str string
   * @return iterator
   */
  protected static ValueBuilder str(final String... str) {
    final ValueBuilder ii = new ValueBuilder();
    for(final String s : str) ii.add(Str.get(s));
    return ii;
  }

  /**
   * Creates an iterator for the specified double.
   * @param d double value
   * @return iterator
   */
  protected static ValueBuilder dbl(final double d) {
    return item(Dbl.get(d));
  }

  /**
   * Creates an iterator for the specified decimal.
   * @param d decimal value
   * @return iterator
   */
  protected static ValueBuilder dec(final double d) {
    return item(Dec.get(d));
  }

  /**
   * Creates an iterator for the specified integer.
   * @param d double value
   * @return iterator
   */
  protected static ValueBuilder itr(final long... d) {
    final ValueBuilder ii = new ValueBuilder();
    for(final long dd : d) ii.add(Int.get(dd));
    return ii;
  }

  /**
   * Creates an iterator for the specified boolean.
   * @param b boolean value
   * @return iterator
   */
  protected static ValueBuilder bool(final boolean... b) {
    final ValueBuilder ii = new ValueBuilder();
    for(final boolean bb : b) ii.add(Bln.get(bb));
    return ii;
  }

  /**
   * Creates an iterator for the specified item.
   * @param i item
   * @return iterator
   */
  private static ValueBuilder item(final Item i) {
    final ValueBuilder vb = new ValueBuilder();
    vb.add(i);
    return vb;
  }
}
