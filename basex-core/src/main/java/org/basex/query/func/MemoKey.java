package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Key of a memoized function result.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MemoKey {
  /** Function. */
  private final StaticFunc func;
  /** Arguments. */
  private final Value[] args;
  /** Hash code. */
  private final int hash;

  /**
   * Constructor.
   * @param func function
   * @param args arguments
   */
  MemoKey(final StaticFunc func, final Value[] args) {
    this.func = func;
    this.args = args;
    int h = System.identityHashCode(func);
    for(final Value arg : args) {
      h = 31 * h + (int) arg.size();
      for(final Item item : arg) h = 31 * h + hash(item);
    }
    hash = h;
  }

  /**
   * Computes the hash code of an item.
   * @param item item
   * @return hash code
   */
  private static int hash(final Item item) {
    return item instanceof final DBNode node ?
      31 * System.identityHashCode(node.data()) + node.pre() :
      item instanceof final JNode node ? node.root().id : item.hashCode();
  }

  /**
   * Compares two items.
   * @param item1 first item
   * @param item2 second item
   * @return result of check
   * @throws QueryException query exception
   */
  private static boolean equal(final Item item1, final Item item2) throws QueryException {
    if(item1 == item2) return true;
    if(item1 instanceof final GNode node1) {
      return item2 instanceof final GNode node2 && node1.is(node2);
    }
    if(item1 instanceof FItem || !item1.type.eq(item2.type) || !item1.atomicEqual(item2)) {
      return false;
    }
    // distinguish equal values that may yield different results (-0, timezones, prefixes)
    return item1 instanceof AStr || item1 instanceof Itr ||
        Token.eq(item1.string(null), item2.string(null));
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof final MemoKey key) || func != key.func || hash != key.hash) return false;
    final int al = args.length;
    try {
      for(int a = 0; a < al; a++) {
        final Value arg1 = args[a], arg2 = key.args[a];
        if(arg1 == arg2) continue;
        final long size = arg1.size();
        if(size != arg2.size()) return false;
        for(long i = 0; i < size; i++) {
          if(!equal(arg1.itemAt(i), arg2.itemAt(i))) return false;
        }
      }
      return true;
    } catch(final QueryException ex) {
      Util.debug(ex);
      return false;
    }
  }

  @Override
  public int hashCode() {
    return hash;
  }
}
