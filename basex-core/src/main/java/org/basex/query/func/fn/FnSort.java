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

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnSort extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value input = exprs[0].value(qc), value = quickValue(input);
    return value != null ? value.iter() : iter(input, qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = exprs[0].value(qc), value = quickValue(input);
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
    final Collation coll = toCollation(1, false, qc);
    final FItem key = exprs.length > 2 ? toFunction(exprs[2], 1, qc) : null;

    final long size = input.size();
    final ValueList values = new ValueList(size);
    final Iter iter = input.iter();
    for(Item item; (item = qc.next(iter)) != null;) {
      values.add((key == null ? item : key.invoke(qc, info, item)).atomValue(qc, info));
    }

    final Integer[] order = sort(values, this, coll, qc);
    return new BasicIter<>(size) {
      @Override
      public Item get(final long i) {
        return input.itemAt(order[(int) i]);
      }
    };
  }

  /**
   * Sort the input data and returns integers representing the item order.
   * @param values values to sort
   * @param sf calling function
   * @param coll collation
   * @param qc query context
   * @return item order
   * @throws QueryException query exception
   */
  public static Integer[] sort(final ValueList values, final StandardFunc sf, final Collation coll,
      final QueryContext qc) throws QueryException {

    final int al = values.size();
    final Integer[] order = new Integer[al];
    for(int o = 0; o < al; o++) order[o] = o;
    final InputInfo info = sf.info();
    try {
      Arrays.sort(order, (i1, i2) -> {
        qc.checkStop();
        try {
          return compare(values.get(i1), values.get(i2), coll, info);
        } catch(final QueryException ex) {
          throw new QueryRTException(ex);
        }
      });
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
    return order;
  }

  /**
   * Compares two values.
   * @param value1 first value
   * @param value2 second value
   * @param coll collation
   * @param info input info
   * @return result of comparison
   * @throws QueryException query exception
   */
  static int compare(final Value value1, final Value value2, final Collation coll,
      final InputInfo info) throws QueryException {
    final long size1 = value1.size(), size2 = value2.size(), il = Math.min(size1, size2);
    for(int i = 0; i < il; i++) {
      Item item1 = value1.itemAt(i), item2 = value2.itemAt(i);
      if(item1 == Dbl.NAN || item1 == Flt.NAN) item1 = null;
      if(item2 == Dbl.NAN || item2 == Flt.NAN) item2 = null;
      if(item1 != null && item2 != null && !item1.comparable(item2))
        throw diffError(item1, item2, info);

      final int diff = item1 == null ? item2 == null ? 0 : -1 : item2 == null ? 1 :
        item1.diff(item2, coll, info);
      if(diff != 0 && diff != Item.UNDEF) return diff;
    }
    final long diff = size1 - size2;
    return diff < 0 ? -1 : diff > 0 ? 1 : 0;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    return mode.oneOf(Simplify.COUNT)
        ? cc.simplify(this, exprs[0]).simplifyFor(mode, cc)
        : super.simplifyFor(mode, cc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // optimize sort on sequences
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    if(exprs.length < 2) {
      if(st.zeroOrOne() && st.type.isSortable()) return input;
      // enforce pre-evaluation as remaining arguments may not be values
      if(input instanceof Value) {
        final Value value = quickValue((Value) input);
        if(value != null) return value;
      }
      if(REPLICATE.is(input) && ((FnReplicate) input).singleEval(false)) {
        final SeqType rst = input.arg(0).seqType();
        if(rst.zeroOrOne() && rst.type.isSortable()) return input;
      }
      if(REVERSE.is(input) || SORT.is(input)) {
        final Expr[] args = exprs.clone();
        args[0] = args[0].arg(0);
        return cc.function(SORT, info, args);
      }
    } else if(exprs.length == 3) {
      exprs[2] = coerceFunc(exprs[2], cc, SeqType.ANY_ATOMIC_TYPE_ZM, st.with(Occ.EXACTLY_ONE));
    }
    return adoptType(input);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.HOF.in(flags) && exprs.length > 2 || super.has(flags);
  }

  /**
   * Evaluates value arguments.
   * @param input input value
   * @return sorted value or {@code null}
   */
  private Value quickValue(final Value input) {
    if(exprs.length < 2) {
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
