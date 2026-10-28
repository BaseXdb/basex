package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Sorting functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class SortFn extends StandardFunc {
  /** Key. */
  private static final Str KEY = Str.get("key");
  /** Collation. */
  private static final Str COLLATION = Str.get("collation");
  /** Order. */
  private static final Str ORDER = Str.get("order");

  /** Enumeration. */
  private enum Order {
    /** Ascending sort. */ ASCENDING,
    /** Descending sort. */ DESCENDING;

    @Override
    public String toString() {
      return Enums.string(this);
    }
  }

  /**
   * Returns the record type of the sort key definitions.
   * @return record type
   */
  protected RecordType keyRecord() {
    return Records.SORT_KEY.get();
  }

  /**
   * Coerces static key records at compile time to avoid function wrappers at runtime.
   * @param cc compilation context
   * @throws QueryException query exception
   */
  protected final void coerceKeys(final CompileContext cc) throws QueryException {
    if(arg(1) instanceof Value) {
      arg(1, arg -> new TypeCheck(info, arg, definition.types[1]).optimize(cc));
    }
  }

  /**
   * Returns the arguments of an equivalent sort call for a single static sort key definition.
   * @param desc descending order
   * @return arguments, or {@code null} if no single static key with this order is defined
   * @throws QueryException query exception
   */
  protected final Expr[] sortArgs(final boolean desc) throws QueryException {
    if(!(arg(1) instanceof final Value keys)) return null;
    final XQMap key = keys.isEmpty() ? XQMap.empty() : keys instanceof final XQMap map ? map : null;
    if(key == null || descending(key) != desc) return null;

    final Value collation = key.get(COLLATION), function = key.get(KEY);
    if(function.isEmpty()) {
      return collation.isEmpty() ? new Expr[] { arg(0) } : new Expr[] { arg(0), collation };
    }
    return new Expr[] { arg(0), collation, function };
  }

  /**
   * Checks if a coerced sort key definition specifies descending order.
   * @param key sort key definition
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean descending(final XQMap key) throws QueryException {
    final Value order = key.get(ORDER);
    return !order.isEmpty() && toEnum((Item) order, Order.class) == Order.DESCENDING;
  }

  /**
   * Checks if the index is sorted.
   * @param index index
   * @return result of check
   */
  protected static boolean sorted(final int[] index) {
    final int il = index.length;
    for(int i = 0; i < il; i++) {
      if(index[i] != i) return false;
    }
    return true;
  }

  /**
   * Returns the sort key definitions.
   * @param qc query context
   * @return key definitions
   * @throws QueryException query exception
   */
  protected Keys keys(final QueryContext qc) throws QueryException {
    Value maps = arg(1).unwrappedValue(qc);
    if(maps.isEmpty()) maps = XQMap.empty();

    final int ms = (int) maps.size();
    final FItem[] keys = new FItem[ms];
    final Collation[] collations = new Collation[ms];
    final boolean[] invert = new boolean[ms];
    int m = 0;
    for(final Item item : maps) {
      final XQMap map = toRecord(item, keyRecord(), qc);
      final Value key = map.get(KEY);
      if(!key.isEmpty()) keys[m] = toFunction(key, 1, qc);
      collations[m] = toCollation(map.get(COLLATION), qc);
      invert[m] = descending(map);
      m++;
    }
    return new Keys(keys, collations, invert);
  }

  /**
   * Returns the ascending key definition of the collation and key arguments.
   * @param qc query context
   * @return key definition
   * @throws QueryException query exception
   */
  protected final Keys sortKeys(final QueryContext qc) throws QueryException {
    return new Keys(new FItem[] { toFunctionOrNull(arg(2), 1, qc) },
        new Collation[] { toCollation(arg(1), qc) }, new boolean[1]);
  }

  /**
   * Returns an array with an index to the original values.
   * @param values values
   * @param qc query context
   * @return index
   * @throws QueryException query exception
   */
  protected final int[] index(final Value[] values, final QueryContext qc)
      throws QueryException {

    final Keys defs = keys(qc);
    final FItem[] keys = defs.keys;
    final Collation[] collations = defs.collations;
    final boolean[] invert = defs.invert;
    final int levels = keys.length, size = values.length;
    final SortKey[][] cached = new SortKey[levels][size];
    final IntBinaryOperator typed = size > 1 && levels > 0 ?
      typed(cached, values, keys[0], collations[0], qc) : null;
    try {
      return Array.createOrder(size, (i1, i2) -> {
        qc.checkStop();
        try {
          for(int l = 0; l < levels; l++) {
            final int diff;
            if(l == 0 && typed != null) {
              diff = typed.applyAsInt(i1, i2);
            } else {
              final SortKey sk1 = sortKey(cached[l], i1, values[i1], keys[l], collations[l], qc);
              final SortKey sk2 = sortKey(cached[l], i2, values[i2], keys[l], collations[l], qc);
              diff = compare(sk1.value, sk2.value, sk1.colls, sk2.colls, collations[l], qc, info);
            }
            if(diff != 0) return invert[l] ? -diff : diff;
          }
          return 0;
        } catch(final QueryException ex) {
          throw new QueryRTException(ex);
        }
      });
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
  }

  /**
   * Computes the sort keys of the first level and returns a comparator if they have a common type.
   * @param cached cached sort keys
   * @param values values
   * @param key key function (can be {@code null})
   * @param collation collation (can be {@code null})
   * @param qc query context
   * @return comparator, or {@code null} if the keys are no single items of a common type
   * @throws QueryException query exception
   */
  private IntBinaryOperator typed(final SortKey[][] cached, final Value[] values, final FItem key,
      final Collation collation, final QueryContext qc) throws QueryException {
    // the keys are computed anyway when two or more values are compared
    final int size = values.length;
    final Item[] items = new Item[size];
    for(int i = 0; i < size; i++) {
      final Value value = sortKey(cached[0], i, values[i], key, collation, qc).value;
      if(value.size() > 1) return null;
      items[i] = value.isEmpty() ? Empty.VALUE : value.itemAt(0);
    }
    final IntBinaryOperator typed = TypedKeys.comparator(size, i -> items[i], collation, true,
        info);
    // primitive values: release the sort keys
    if(typed != null) Arrays.fill(cached[0], null);
    return typed;
  }

  /**
   * Returns the cached sort key of a value, or computes it.
   * @param cached cached sort keys
   * @param index index of the sort key
   * @param value value
   * @param key key function (can be {@code null})
   * @param collation collation (can be {@code null})
   * @param qc query context
   * @return sort key
   * @throws QueryException query exception
   */
  private SortKey sortKey(final SortKey[] cached, final int index, final Value value,
      final FItem key, final Collation collation, final QueryContext qc) throws QueryException {
    SortKey sk = cached[index];
    if(sk == null) {
      final Value val = (key == null ? value : key.invoke(qc, info, value)).atomValue(qc, info);
      sk = new SortKey(val, collationKeys(val, collation, info));
      cached[index] = sk;
    }
    return sk;
  }

  /**
   * Returns the smallest items of the input in ascending order.
   * @param input input
   * @param limit maximum number of items
   * @param qc query context
   * @return items
   * @throws QueryException query exception
   */
  protected final Value smallest(final Iter input, final int limit, final QueryContext qc)
      throws QueryException {
    final Keys defs = keys(qc);
    final TopK<Entry> top = new TopK<>(limit, (e1, e2) -> compare(e1, e2, defs, qc));
    for(Item item; (item = qc.next(input)) != null;) {
      top.add(new Entry(item, new SortKey[defs.keys.length]));
    }
    final List<Entry> entries = top.finish();
    final ValueBuilder vb = new ValueBuilder(qc, entries.size());
    for(final Entry entry : entries) vb.add(entry.item);
    return vb.value(this);
  }

  /**
   * Compares two entries.
   * @param entry1 first entry
   * @param entry2 second entry
   * @param defs key definitions
   * @param qc query context
   * @return result of comparison
   * @throws QueryException query exception
   */
  private int compare(final Entry entry1, final Entry entry2, final Keys defs,
      final QueryContext qc) throws QueryException {
    final int levels = defs.keys.length;
    for(int l = 0; l < levels; l++) {
      final FItem key = defs.keys[l];
      final Collation collation = defs.collations[l];
      final SortKey sk1 = sortKey(entry1.keys, l, entry1.item, key, collation, qc);
      final SortKey sk2 = sortKey(entry2.keys, l, entry2.item, key, collation, qc);
      final int diff = compare(sk1.value, sk2.value, sk1.colls, sk2.colls, collation, qc, info);
      if(diff != 0) return defs.invert[l] ? -diff : diff;
    }
    return 0;
  }

  /**
   * Compares two values.
   * @param value1 first value
   * @param value2 second value
   * @param collation collation (can be {@code null})
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return result of comparison (-1, 0, 1)
   * @throws QueryException query exception
   */
  static int compare(final Value value1, final Value value2, final Collation collation,
      final QueryContext qc, final InputInfo info) throws QueryException {
    return compare(value1, value2, null, null, collation, qc, info);
  }

  /**
   * Compares two values, using collation keys if available.
   * @param value1 first value
   * @param value2 second value
   * @param colls1 collation keys of the first value (can be {@code null})
   * @param colls2 collation keys of the second value (can be {@code null})
   * @param collation collation (can be {@code null})
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return result of comparison (-1, 0, 1)
   * @throws QueryException query exception
   */
  private static int compare(final Value value1, final Value value2, final byte[][] colls1,
      final byte[][] colls2, final Collation collation, final QueryContext qc,
      final InputInfo info) throws QueryException {
    final long size1 = value1.size(), size2 = value2.size(), il = Math.min(size1, size2);
    for(long i = 0; i < il; i++) {
      final Item item1 = value1.itemAt(i), item2 = value2.itemAt(i);
      if(!item1.comparable(item2)) throw compareError(item1, item2, info);
      final int diff = Collation.compare(item1, colls1 != null ? colls1[(int) i] : null,
          item2, colls2 != null ? colls2[(int) i] : null, collation, qc, info);
      if(diff != 0) return diff;
    }
    return Long.signum(size1 - size2);
  }

  /**
   * Returns the collation keys of the strings of a value.
   * @param value atomized value
   * @param collation collation (can be {@code null})
   * @param info input info (can be {@code null})
   * @return keys (with {@code null} entries for other items), or {@code null} if none exist
   * @throws QueryException query exception
   */
  private static byte[][] collationKeys(final Value value, final Collation collation,
      final InputInfo info) throws QueryException {
    if(collation == null) return null;
    final int size = (int) value.size();
    byte[][] colls = null;
    for(int i = 0; i < size; i++) {
      final byte[] coll = Collation.key(value.itemAt(i), collation, info);
      if(coll != null) {
        if(colls == null) colls = new byte[size][];
        colls[i] = coll;
      }
    }
    return colls;
  }

  /**
   * Atomized sort key.
   * @param value atomized value
   * @param colls collation keys of its strings (can be {@code null})
   */
  private record SortKey(Value value, byte[][] colls) { }

  /**
   * Sort key definitions.
   * @param keys key functions (entries can be {@code null})
   * @param collations collations (entries can be {@code null})
   * @param invert descending order
   */
  protected record Keys(FItem[] keys, Collation[] collations, boolean[] invert) { }

  /**
   * Heap entry.
   * @param item item
   * @param keys sort keys, computed on demand
   */
  private record Entry(Item item, SortKey[] keys) { }

  /**
   * Sorts values with user-defined comparators.
   * @param values values to be sorted
   * @param qc query context
   * @throws QueryException query exception
   */
  protected final void sort(final ValueList values, final QueryContext qc) throws QueryException {
    final Value comparators = arg(1).value(qc);
    if(comparators.isEmpty()) throw typeError(comparators, Types.ITEM_OM, info);

    final FItem[] cmps = new FItem[(int) comparators.size()];
    int c = 0;
    for(final Item item : comparators) cmps[c++] = toFunction(item, 2, qc);

    final Comparator<Value> comparator = (value1, value2) -> {
      try {
        for(final FItem cmp : cmps) {
          final long diff = toLong(cmp.invoke(qc, info, value1, value2).atomItem(qc, info));
          if(diff != 0) return Long.signum(diff);
        }
        return 0;
      } catch(final QueryException ex) {
        throw new QueryRTException(ex);
      }
    };

    // sort a copy: an aborted sort may leave the array in an inconsistent state
    final Value[] list = Arrays.copyOf(values.list, values.size());
    try {
      Arrays.sort(list, comparator);
      System.arraycopy(list, 0, values.list, 0, list.length);
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    } catch(final IllegalArgumentException ex) {
      // inconsistent comparator: the order is unpredictable, the input order is kept
      Util.debug(ex);
    }
  }
}
