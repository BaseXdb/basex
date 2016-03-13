package org.basex.query;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.Test;

/**
 * This class tests the database commands.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class QueryTest extends SandboxTest {
  /** Queries. */
  protected static Object[][] queries;

  /**
   * Tests the specified instance.
   */
  @Test
  public void test() {
    final StringBuilder sb = new StringBuilder();
    int fail = 0;

    for(final Object[] qu : queries) {
      final boolean correct = qu.length == 3;
      final String query = qu[correct ? 2 : 1].toString();
      final Value cmp = correct ? (Value) qu[1] : null;

      try(final QueryProcessor qp = new QueryProcessor(query, context)) {
        final Value val = qp.value();
        if(!correct || !new DeepEqual().equal(val, cmp)) {
          sb.append("[" + qu[0] + "] " + query);
          sb.append("\n[E] " + cmp.size() + " result(s): ");
          if(correct) {
            for(final Item it : cmp) sb.append(it.serialize()).append(", ");
          } else {
            sb.append("error");
          }
          sb.append("\n[F] " + val.size() + " result(s): ");
          for(final Item it : val) sb.append(it.serialize()).append(", ");
          sb.append(details()).append('\n');
          ++fail;
        }
      } catch(final Exception ex) {
        final String msg = ex.getMessage();
        if(correct || msg == null || msg.contains("mailman")) {
          final String cp = correct && cmp.data() != null ? cmp.toString() : "()";
          sb.append("[" + qu[0] + "] " + query + "\n[E] " + cp + "\n[F] "
              + (msg == null ? Util.className(ex) : normNL(msg)) + ' ' + details() + '\n');
          ex.printStackTrace();
          ++fail;
        }
      }
    }
    if(fail != 0) fail(fail + " Errors. [E] = expected, [F] = found:\n" + sb.toString().trim());
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
    return Empty.SEQ;
  }

  /**
   * Creates a container for the specified node values.
   * @param nodes node values
   * @return node array
   */
  protected static Value nodes(final int... nodes) {
    return DBNodeSeq.get(new IntList(nodes), context.data(), false, false);
  }

  /**
   * Creates a container for the specified string.
   * @param strings string
   * @return iterator
   */
  protected static Value strings(final String... strings) {
    final TokenList tl = new TokenList(strings.length);
    for(final String s : strings) tl.add(s);
    return StrSeq.get(tl.finish());
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
   * Creates an iterator for the specified decimal.
   * @param d decimal value
   * @return iterator
   */
  protected static Item decimal(final double d) {
    return Dec.get(d);
  }

  /**
   * Creates an iterator for the specified integer.
   * @param integers double value
   * @return iterator
   */
  protected static Value integers(final long... integers) {
    return IntSeq.get(integers, AtomType.ITR);
  }

  /**
   * Creates an iterator for the specified boolean.
   * @param b boolean value
   * @return iterator
   */
  protected static Value booleans(final boolean... b) {
    return BlnSeq.get(b);
  }
}
