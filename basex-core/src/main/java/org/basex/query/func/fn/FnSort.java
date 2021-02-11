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

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnSort extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc), v = quickValue(value);
    return v != null ? v.iter() : iter(value, qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc), v = quickValue(value);
    return v != null ? v : iter(value, qc).value(qc, this);
  }

  /**
   * Sort the input data and returns an iterator.
   * @param value value
   * @param qc query context
   * @return iterator with ordered items
   * @throws QueryException query exception
   */
  private Iter iter(final Value value, final QueryContext qc) throws QueryException {
    Collation coll = sc.collation;
    if(exprs.length > 1) {
      final byte[] token = toTokenOrNull(exprs[1], qc);
      if(token != null) coll = Collation.get(token, qc, sc, info, WHICHCOLL_X);
    }
    final FItem key = exprs.length > 2 ? checkArity(exprs[2], 1, qc) : null;

    final long size = value.size();
    final ValueList values = new ValueList(size);
    final Iter iter = value.iter();
    for(Item item; (item = qc.next(iter)) != null;) {
      values.add((key == null ? item : key.invoke(qc, info, item)).atomValue(qc, info));
    }

    final Integer[] order = sort(values, this, coll, qc);
    return new BasicIter<Item>(size) {
      @Override
      public Item get(final long i) {
        return value.itemAt(order[(int) i]);
      }
    };
  }

  /**
   * Sort the input data and returns integers representing the item order.
   * @param values value list
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
    try {
      Arrays.sort(order, (i1, i2) -> {
        qc.checkStop();
        try {
          final Value value1 = values.get(i1), value2 = values.get(i2);
          final long size1 = value1.size(), size2 = value2.size(), il = Math.min(size1, size2);
          for(int i = 0; i < il; i++) {
            Item item1 = value1.itemAt(i), item2 = value2.itemAt(i);
            if(item1 == Dbl.NAN || item1 == Flt.NAN) item1 = null;
            if(item2 == Dbl.NAN || item2 == Flt.NAN) item2 = null;
            if(item1 != null && item2 != null && !item1.comparable(item2))
              throw diffError(item1, item2, sf.info);

            final int diff = item1 == null ? item2 == null ? 0 : -1 : item2 == null ? 1 :
              item1.diff(item2, coll, sf.info);
            if(diff != 0 && diff != Item.UNDEF) return diff;
          }
          return (int) (size1 - size2);
        } catch(final QueryException ex) {
          throw new QueryRTException(ex);
        }
      });
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
    return order;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // optimize sort on sequences
    final Expr expr1 = exprs[0];
    final SeqType st1 = expr1.seqType();
    if(st1.zero()) return expr1;

    // enforce pre-evaluation as remaining arguments may not be values
    if(expr1 instanceof Value) {
      final Value value = quickValue((Value) expr1);
      if(value != null) return value;
    }
    if(_UTIL_REPLICATE.is(expr1)) {
      final SeqType st = expr1.arg(0).seqType();
      if(st.zeroOrOne() && st.type.isSortable()) return expr1;
    }
    if(exprs.length == 3) {
      exprs[2] = coerceFunc(exprs[2], cc, SeqType.ANY_ATOMIC_TYPE_ZM, st1.with(Occ.EXACTLY_ONE));
    }
    return adoptType(expr1);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode == Simplify.DISTINCT && seqType().type.isSortable()) {
      expr = cc.simplify(this, exprs[0]);
    }
    return expr == this ? super.simplifyFor(mode, cc) : expr.simplifyFor(mode, cc);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.HOF.in(flags) && exprs.length > 2 || super.has(flags);
  }

  /**
   * Evaluates value arguments.
   * @param value value
   * @return sorted value or {@code null}
   */
  private Value quickValue(final Value value) {
    if(exprs.length < 2) {
      // range values
      if(value instanceof RangeSeq) {
        final RangeSeq seq = (RangeSeq) value;
        return seq.asc ? seq : seq.reverse(null);
      }
      // sortable single or singleton values
      final SeqType st = value.seqType();
      if(st.type.isSortable() && (st.one() || (value instanceof SingletonSeq &&
          ((SingletonSeq) value).singleItem()))) return value;
    }
    // no quick evaluation possible
    return null;
  }
}
