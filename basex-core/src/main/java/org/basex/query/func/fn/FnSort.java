package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnSort extends StandardFunc {
  /** Enumeration. */
  private enum Order {
    /** Ascending sort. */ ASCENDING,
    /** Descending sort. */ DESCENDING;

    @Override
    public String toString() {
      return EnumOption.string(name());
    }
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc), value = quickValue(input);
    return value != null ? value.iter() : iter(input, qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc), value = quickValue(input);
    return value != null ? value : iter(input, qc).value(qc, this);
  }

  /**
   * Sort the input data and returns an iterator.
   * @param input items to be sorted
   * @param qc query context
   * @return iterator with ordered items
   * @throws QueryException query exception
   */
  private Iter iter(final Value input, final QueryContext qc) throws QueryException {
    final ValueList list = new ValueList(input.size());
    for(final Item item : input) list.add(item);
    final Value[] values = list.finish();

    final Integer[] index = index(values, qc);
    return new BasicIter<>(values.length) {
      @Override
      public Item get(final long i) {
        return (Item) values[index[(int) i]];
      }
    };
  }

  /**
   * Returns an array with an index to the original values.
   * @param values values
   * @param qc query context
   * @return index
   * @throws QueryException query exception
   */
  protected final Integer[] index(final Value[] values, final QueryContext qc)
      throws QueryException {

    final Value key = arg(2).value(qc);
    final int levels = (int) Math.max(1, key.size()), size = values.length;
    final Value collation = arg(1).value(qc), order = arg(3).value(qc);

    final Value[][] cached = new Value[levels][];
    final FItem[] keys = new FItem[levels];
    final Collation[] collations = new Collation[levels];
    final boolean[] invert = new boolean[levels];

    for(int l = 0; l < levels; l++) {
      cached[l] = new Value[size];
      if(l < key.size()) keys[l] = toFunction(key.itemAt(l), 1, qc);
      collations[l] = l < collation.size() ? toCollation(collation.itemAt(l), qc) :
        l > 0 ? collations[l - 1] : null;
      invert[l] = l < order.size() ? toEnum(order.itemAt(l), Order.class) == Order.DESCENDING :
        l > 0 ? invert[l - 1] : false;
    }

    // single value: atomize to check type
    if(size == 1) values[0].atomValue(qc, info);

    final Integer[] indexes = new Integer[size];
    for(int o = 0; o < size; o++) indexes[o] = o;
    try {
      Arrays.sort(indexes, (i1, i2) -> {
        qc.checkStop();
        try {
          for(int l = 0; l < levels; l++) {
            final int ll = l;
            final QueryFunction<Integer, Value> value = i -> {
              Value v = cached[ll][i];
              if(v == null) {
                final FItem k = keys[ll];
                v = (k == null ? values[i] : eval(k, qc, values[i])).atomValue(qc, info);
                cached[ll][i] = v;
              }
              return v;
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
   * @param coll collation
   * @param info input info
   * @return result of comparison (greater than 0, smaller than 0, or 0)
   * @throws QueryException query exception
   */
  static int compare(final Value value1, final Value value2, final Collation coll,
      final InputInfo info) throws QueryException {
    final long size1 = value1.size(), size2 = value2.size(), il = Math.min(size1, size2);
    for(int i = 0; i < il; i++) {
      final Item item1 = value1.itemAt(i), item2 = value2.itemAt(i);
      if(!item1.comparable(item2)) throw diffError(item1, item2, info);

      int diff = item1.diff(item2, coll, info);
      if(diff == Item.UNDEF) {
        final boolean nan1 = item1 == Dbl.NAN || item1 == Flt.NAN;
        final boolean nan2 = item2 == Dbl.NAN || item2 == Flt.NAN;
        diff = nan1 ? nan2 ? 0 : -1 : 1;
      }
      if(diff != 0) return diff;
    }
    final long diff = size1 - size2;
    return diff < 0 ? -1 : diff > 0 ? 1 : 0;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    return cc.simplify(this, mode == Simplify.COUNT ? arg(0) : this, mode);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // optimize sort on sequences
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    if(defined(2) && arg(2).size() == 1) {
      arg(2, arg -> coerceFunc(arg, cc, SeqType.ANY_ATOMIC_TYPE_ZM, st.with(Occ.EXACTLY_ONE)));
    } else if(exprs.length == 1) {
      if(st.zeroOrOne() && st.type.isSortable()) return input;
      // enforce pre-evaluation as remaining arguments may not be values
      if(input instanceof Value) {
        final Value value = quickValue((Value) input);
        if(value != null) return value;
      } else if(REVERSE.is(input) || SORT.is(input)) {
        final Expr[] args = exprs.clone();
        args[0] = args[0].arg(0);
        return cc.function(SORT, info, args);
      } else if(REPLICATE.is(input) && ((FnReplicate) input).singleEval(false)) {
        final SeqType rst = input.arg(0).seqType();
        if(rst.zeroOrOne() && rst.type.isSortable()) return input;
      }
    }
    return adoptType(input);
  }

  @Override
  public final boolean has(final Flag... flags) {
    return Flag.HOF.in(flags) && defined(2) || super.has(flags);
  }

  /**
   * Evaluates value arguments.
   * @param input input value
   * @return sorted value or {@code null}
   */
  private Value quickValue(final Value input) {
    if(exprs.length == 1) {
      // range values
      if(input instanceof RangeSeq) {
        final RangeSeq seq = (RangeSeq) input;
        return seq.asc ? seq : seq.reverse(null);
      }
      // sortable single or singleton values
      final SeqType st = input.seqType();
      if(st.type.isSortable() && (st.one() || input instanceof SingletonSeq &&
          ((SingletonSeq) input).singleItem())) return input;
    }
    // no quick evaluation possible
    return null;
  }
}
