package org.basex.query;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the consistency of random and sequential iterator access.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IterTest extends SandboxTest {
  /** Consumers that request items of their input by position. */
  private static final String[] CONSUMERS = {
    "reverse(%)", "%[last()]", "items-at(%, (1, 3))", "subsequence(%, 2, 2)", "foot(%)", "head(%)"
  };

  /**
   * Returns expressions that yield an iterator with a known size.
   * @return generators
   */
  private static String[] generators() {
    final String w = wrap(9);
    return new String[] {
      "(" + w + ", 1, 2, 3) ! string()",
      "for-each((" + w + ", 1, 2, 3), string#1)",
      "remove((" + w + ", 1, 2, 3) ! string(), 2)",
      "replicate(" + w + ", 4)",
      "for-each-pair((" + w + ", 1, 2), ('a', 'b', 'c'), concat#2)",
      "map:for-each({ 1: " + w + ", 2: 'b', 3: 'c' }, concat#2)",
      "array:members([ " + w + ", 1, 2 ]) ! [ jvalue() ]",
      "array:split([ " + w + ", 1, 2 ])",
      "map:entries({ 1: " + w + ", 2: 'b' })"
    };
  }

  /** Random access to an iterator must return the items of sequential access. */
  @Test public void randomAccess() {
    for(final String generator : generators()) {
      final List<String> items = items(generator);
      final Context ctx = context;
      try(QueryProcessor qp = new QueryProcessor(generator, ctx)) {
        qp.compile();
        qp.register(ctx);
        try {
          final Iter iter = qp.iter();
          final long size = iter.size();
          // random access is only available for sized iterators that are not based on a value
          if(size == -1 || iter.valueIter()) continue;
          assertEquals(items.size(), size, "Wrong size: " + generator);
          for(int i = 0; i < size; i++) {
            final Item item = iter.get(i);
            // random access is not implemented
            if(item == null) break;
            assertEquals(items.get(i), item.toString(),
                "Wrong item at position " + (i + 1) + ": " + generator);
          }
        } finally {
          qp.close();
          qp.unregister(ctx);
        }
      } catch(final QueryException ex) {
        Util.stack(ex);
        fail(ex);
      }
    }
  }

  /** Positional consumers must return the same results for lazy and materialized input. */
  @Test public void consumers() {
    for(final String generator : generators()) {
      final String literals = String.join(", ", items(generator));
      for(final String consumer : CONSUMERS) {
        // the input is parenthesized: a predicate must not bind to the last step of the generator
        final String lazy = consumer.replace("%", '(' + generator + ')');
        assertEquals(query(consumer.replace("%", '(' + literals + ')')), query(lazy), lazy);
      }
    }
  }

  /**
   * Returns the items of an expression, requested one by one.
   * @param query query
   * @return string representations of all items
   */
  private static List<String> items(final String query) {
    final List<String> items = new ArrayList<>();
    final Context ctx = context;
    try(QueryProcessor qp = new QueryProcessor(query, ctx)) {
      qp.compile();
      qp.register(ctx);
      try {
        final Iter iter = qp.iter();
        for(Item item; (item = iter.next()) != null;) items.add(item.toString());
      } finally {
        qp.close();
        qp.unregister(ctx);
      }
    } catch(final QueryException ex) {
      Util.stack(ex);
      fail(ex);
    }
    return items;
  }
}
