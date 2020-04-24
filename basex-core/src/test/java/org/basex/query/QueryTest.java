package org.basex.query;

import static org.junit.jupiter.api.Assertions.*;

import java.math.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the database commands.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public abstract class QueryTest extends SandboxTest {
  /** Queries. */
  protected static Object[][] queries;

  /**
   * Tests the specified instance.
   */
  @Test public void test() {
    final StringBuilder sb = new StringBuilder();
    int fail = 0;

    for(final Object[] qu : queries) {
      if(qu.length == 0) continue;

      final boolean correct = qu.length == 3;
      final String query = qu[correct ? 2 : 1].toString();
      final Value cmp = correct ? (Value) qu[1] : null;

      try {
        final Value value = run(query);
        if(!correct || !eq(value, cmp)) {
          sb.append('[').append(qu[0]).append("] ").append(query);
          sb.append("\n[E] ");
          if(correct) {
            serialize(cmp, sb);
          } else {
            sb.append("error");
          }
          serialize(value, sb.append("\n[F] ")).append(details()).append('\n');
          ++fail;
        }
      } catch(final Exception ex) {
        final String msg = ex.getMessage();
        if(correct || msg == null || msg.contains("mailman")) {
          final String cp = correct && cmp.data() != null ? cmp.toString() : "()";
          sb.append('[').append(qu[0]).append("] ").append(query).append("\n[E] ");
          sb.append(cp).append("\n[F] ").append(msg == null ? Util.className(ex) : normNL(msg));
          sb.append(' ').append(details()).append('\n');
          ex.printStackTrace();
          ++fail;
        }
      }
    }
    if(fail != 0) fail(fail + " Errors. [E] = expected, [F] = found:\n" + sb.toString().trim());
  }

  protected static String ser(final Value v) throws Exception {
    return serialize(v, new StringBuilder()).toString();
  }

  protected static StringBuilder serialize(final Value v, final StringBuilder sb) throws Exception {
    sb.append(v.size()).append(" result(s): ");
    for(final Item item : v) sb.append(item.serialize()).append("; ");
    return sb;
  }

  protected static Value run(final String query) throws Exception {
    try(QueryProcessor qp = new QueryProcessor(query, context)) {
      return qp.value();
    }
  }

  protected static boolean eq(final Value actual, final Value expected) throws Exception {
    return new DeepEqual().equal(actual, expected);
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
   * @param doc document
   */
  protected static void create(final String doc) {
    execute(new CreateDB(Util.className(SandboxTest.class), doc));
  }

  /**
   * Creates a container for the specified node values.
   * @return node array
   */
  protected static Value empty() {
    return Empty.VALUE;
  }

  /**
   * Creates a container for the specified node values.
   * @param nodes node values
   * @return node array
   */
  protected static Value nodes(final int... nodes) {
    return DBNodeSeq.get(nodes, context.data(), null);
  }

  /**
   * Creates a container for the specified string.
   * @param strings string
   * @return iterator
   */
  protected static Value strings(final String... strings) {
    final TokenList tl = new TokenList(strings.length);
    for(final String s : strings) tl.add(s);
    return StrSeq.get(tl);
  }

  /**
   * Creates an iterator for the specified double.
   * @param doubles double value
   * @return iterator
   */
  protected static Value doubles(final double... doubles) {
    return DblSeq.get(doubles);
  }

  /**
   * Creates an iterator for the specified integer.
   * @param integer integer value
   * @return iterator
   */
  protected static Item decimal(final int integer) {
    return Dec.get(new BigDecimal(integer));
  }

  /**
   * Creates an iterator for the specified integer.
   * @param integers double value
   * @return iterator
   */
  protected static Value integers(final long... integers) {
    return IntSeq.get(integers);
  }

  /**
   * Creates an iterator for the specified boolean.
   * @param bool boolean value
   * @return iterator
   */
  protected static Value booleans(final boolean... bool) {
    return BlnSeq.get(bool);
  }
}
