package org.basex.query;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.List;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the consistency of random and sequential iterator access.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IterTest extends SandboxTest {
  /** Class file suffix. */
  private static final String SUFFIX = ".class";
  /** Consumers that request items of their input by position. */
  private static final String[] CONSUMERS = {
    "reverse(%)", "%[last()]", "items-at(%, (1, 3))", "subsequence(%, 2, 2)", "foot(%)", "head(%)",
    "tail(%)", "trunk(%)", "remove(%, 2)", "count(%)", "one-or-more(%)", "replicate(%, 2)",
    "if(" + wrap(1) + " = 1) then % else ()"
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
          if(size == -1 || iter.value() != null) continue;
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

  /** Lazy consumers must not materialize their input. */
  @Test public void lazy() {
    final String input = "(1 to 100_000_000)[. > 0]";
    query("head(" + input + ')', 1);
    query("items-at(" + input + ", 2)", 2);
    query('(' + input + ")[1]", 1);
    query("exists(" + input + ')', true);
    query("head(tail(" + input + "))", 2);
    query("head(subsequence(" + input + ", 3))", 3);
    query("head(one-or-more(" + input + "))", 1);
    query("head(replicate(" + input + ", 2, true()))", 1);
    query("head(" + input + " otherwise 1)", 1);
    query("head(if(" + wrap(1) + " = 1) then " + input + " else ())", 1);
    query("head(switch(" + wrap(1) + ") case '1' return " + input + " default return ())", 1);
    query("head(typeswitch(" + wrap(1) + ") case xs:untypedAtomic return " + input +
        " default return ())", 1);
  }

  /**
   * Expressions that claim eager evaluation must be able to return a value.
   * @throws Exception exception
   */
  @Test public void eager() throws Exception {
    final StringList errors = new StringList();
    final List<Class<?>> classes = classes("target/classes/org/basex/query");
    int eager = 0;
    for(final Class<?> clazz : classes) {
      // an expression that overrides eager() must implement value(): otherwise, its iterator
      // implementation will call value(), which will call the iterator implementation again
      if(!declares(clazz, "eager")) continue;
      eager++;
      if(!declares(clazz, "value", QueryContext.class)) errors.add(clazz.getSimpleName());
    }
    assertTrue(errors.isEmpty(), "No value() implementation: " + errors);
    // ensure that the class scan was successful
    assertTrue(eager > 0, "No eager() implementations found in " + classes.size() + " classes");
  }

  /**
   * Returns all expression classes of a directory.
   * @param path directory path
   * @return classes
   * @throws Exception exception
   */
  private static List<Class<?>> classes(final String path) throws Exception {
    final List<Class<?>> classes = new ArrayList<>();
    for(final String desc : new IOFile(path).descendants()) {
      if(!desc.endsWith(SUFFIX)) continue;
      final String name = "org.basex.query." +
          desc.substring(0, desc.length() - SUFFIX.length()).replace('/', '.');
      final Class<?> clazz = Class.forName(name, false, IterTest.class.getClassLoader());
      if(Expr.class.isAssignableFrom(clazz)) classes.add(clazz);
    }
    return classes;
  }

  /**
   * Indicates if a class declares a method itself (base classes are ignored).
   * @param clazz class
   * @param name method name
   * @param args argument types
   * @return result of check
   */
  private static boolean declares(final Class<?> clazz, final String name,
      final Class<?>... args) {
    for(Class<?> clz = clazz; clz != null && clz != Expr.class && clz != ParseExpr.class &&
        clz != Value.class; clz = clz.getSuperclass()) {
      try {
        if(clz.getMethod(name, args).getDeclaringClass() == clz) return true;
      } catch(final Exception ignore) { }
    }
    return false;
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
