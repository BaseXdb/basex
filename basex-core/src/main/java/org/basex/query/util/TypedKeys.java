package org.basex.query.util;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Sort keys of a common type, which are compared as primitive values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TypedKeys {
  /** Private constructor. */
  private TypedKeys() { }

  /**
   * Returns a comparator for integers, doubles, or strings and untyped values without collation.
   * @param size number of keys
   * @param keys function returning the key at the specified index ({@link Empty#VALUE}: no key)
   * @param coll collation (can be {@code null})
   * @param least empty keys precede all other keys
   * @param info input info (can be {@code null})
   * @return comparator, or {@code null} if the keys have no common type
   * @throws QueryException query exception
   */
  public static IntBinaryOperator comparator(final int size, final IntFunction<Item> keys,
      final Collation coll, final boolean least, final InputInfo info) throws QueryException {

    // check if all keys have a common type and are mutually comparable
    boolean itr = size > 0, dbl = size > 0, str = Collation.get(coll, info) == null;
    Item first = null;
    for(int i = 0; i < size && (itr || dbl || str); i++) {
      final Item item = keys.apply(i);
      final boolean empty = item == Empty.VALUE;
      if(!empty) {
        if(first == null) first = item;
        else if(!first.comparable(item)) return null;
      }
      itr &= item instanceof Itr;
      dbl &= item instanceof Dbl;
      str &= empty || item instanceof AStr || item instanceof Atm;
    }

    if(itr) {
      final long[] values = new long[size];
      for(int i = 0; i < size; i++) values[i] = keys.apply(i).itr(info);
      return (x, y) -> Long.compare(values[x], values[y]);
    }
    if(dbl) {
      final double[] values = new double[size];
      for(int i = 0; i < size; i++) values[i] = keys.apply(i).dbl(info);
      return (x, y) -> Dbl.compare(values[x], values[y], true);
    }
    if(str) {
      final byte[][] values = new byte[size][];
      for(int i = 0; i < size; i++) {
        final Item item = keys.apply(i);
        if(item != Empty.VALUE) values[i] = item.string(info);
      }
      return (x, y) -> {
        final byte[] a = values[x], b = values[y];
        return a == null ? b == null ? 0 : least ? -1 : 1 : b == null ? least ? 1 : -1 :
          Token.compare(a, b);
      };
    }
    return null;
  }
}
