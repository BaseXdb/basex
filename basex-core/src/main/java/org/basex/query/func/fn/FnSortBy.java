package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnSortBy extends StandardFunc {
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

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return iter(arg(0).value(qc), qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    return input.seqType().zero() ? input : adoptType(input);
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    // count(sort(A))  ->  count(A)
    return cc.simplify(this, mode == Simplify.COUNT ? arg(0) : this, mode);
  }

  /**
   * Sort the input data and returns an iterator.
   * @param input items to be sorted
   * @param qc query context
   * @return iterator with ordered items
   * @throws QueryException query exception
   */
  Iter iter(final Value input, final QueryContext qc) throws QueryException {
    if(input.isEmpty()) return input.iter();

    final long is = input.size();
    final ItemList list = new ItemList(is);
    for(final Item item : input) list.add(item);
    final Item[] values = list.finish();
    final Integer[] index = index(values, qc);
    return sorted(index) ? input.iter() : new BasicIter<>(is) {
      @Override
      public Item get(final long l) {
        return values[index[(int) l]];
      }
    };
  }

  /**
   * Checks if the index is sorted.
   * @param index index
   * @return result of check
   */
  protected static boolean sorted(final Integer[] index) {
    final int il = index.length;
    for(int i = 0; i < il; i++) {
      if(index[i] != i) return false;
    }
    return true;
  }

  /**
   * Returns an array with an index to the original values.
   * @param values values
   * @param qc query context
   * @return index
   * @throws QueryException query exception
   */
  protected Integer[] index(final Value[] values, final QueryContext qc) throws QueryException {
    Value maps = arg(1).value(qc);
    if(maps.isEmpty()) maps = XQMap.empty();

    final int ms = (int) maps.size();
    final FItem[] keys = new FItem[ms];
    final Collation[] collations = new Collation[ms];
    final boolean[] invert = new boolean[ms];
    int m = 0;
    for(final Item item : maps) {
      final XQMap map = toMap(item);
      if(map.contains(KEY)) keys[m] = toFunction(map.get(KEY), 1, qc);
      collations[m] = toCollation(map.get(COLLATION), qc);
      if(map.contains(ORDER)) {
        invert[m] = toEnum(map.get(ORDER).item(qc, info), Order.class) == Order.DESCENDING;
      }
      m++;
    }
    return index(values, keys, collations, invert, qc);
  }

  /**
   * Returns an array with an index to the original values.
   * @param values values
   * @param keys keys
   * @param collations collations
   * @param invert ascending/descending order
   * @param qc query context
   * @return index
   * @throws QueryException query exception
   */
  protected final Integer[] index(final Value[] values, final FItem[] keys,
      final Collation[] collations, final boolean[] invert, final QueryContext qc)
      throws QueryException {

    final int levels = keys.length, size = values.length;
    final Value[][] cached = new Value[levels][];
    for(int l = 0; l < levels; l++) cached[l] = new Value[size];
    final Integer[] indexes = new Integer[size];
    for(int o = 0; o < size; o++) indexes[o] = o;
    try {
      Arrays.sort(indexes, (i1, i2) -> {
        qc.checkStop();
        try {
          for(int l = 0; l < levels; l++) {
            final int ll = l;
            final QueryFunction<Integer, Value> value = i -> {
              Value val = cached[ll][i];
              if(val == null) {
                final FItem k = keys[ll];
                val = (k == null ? values[i] : k.invoke(qc, info, values[i])).atomValue(qc, info);
                cached[ll][i] = val;
              }
              return val;
            };
            final int diff = compare(value.apply(i1), value.apply(i2), collations[l], info);
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
    return indexes;
  }

  /**
   * Compares two values.
   * @param value1 first value
   * @param value2 second value
   * @param info input info (can be {@code null})
   * @param collation collation (can be {@code null})
   * @return result of comparison (-1, 0, 1)
   * @throws QueryException query exception
   */
  static int compare(final Value value1, final Value value2, final Collation collation,
      final InputInfo info) throws QueryException {
    final long size1 = value1.size(), size2 = value2.size(), il = Math.min(size1, size2);
    for(int i = 0; i < il; i++) {
      final Item item1 = value1.itemAt(i), item2 = value2.itemAt(i);
      if(!item1.comparable(item2)) throw compareError(item1, item2, info);
      final int diff = item1.compare(item2, collation, true, info);
      if(diff != 0) return diff;
    }
    return Long.signum(size1 - size2);
  }
}
