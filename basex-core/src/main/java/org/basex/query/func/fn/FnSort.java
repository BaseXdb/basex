package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnSort extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = qc.value(exprs[0]), v = value(value);
    return v != null ? v : iter(value, qc).value(qc);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value value = qc.value(exprs[0]), v = value(value);
    return v != null ? v.iter() : iter(value, qc);
  }

  /**
   * Sort the input data.
   * @param value value
   * @param qc query context
   * @return item order
   * @throws QueryException query exception
   */
  private Iter iter(final Value value, final QueryContext qc) throws QueryException {
    Collation coll = sc.collation;
    if(exprs.length > 1) {
      final byte[] tok = toTokenOrNull(exprs[1], qc);
      if(tok != null) coll = Collation.get(tok, qc, sc, info, WHICHCOLL_X);
    }
    final FItem key = exprs.length > 2 ? checkArity(exprs[2], 1, qc) : null;

    final long sz = value.size();
    final ValueList vl = new ValueList((int) Math.min(Integer.MAX_VALUE, sz));
    for(final Item it : value) {
      vl.add((key == null ? it : key.invokeValue(qc, info, it)).atomValue(info));
    }

    final Integer[] order = sort(vl, this, coll);
    return new BasicIter<Item>(sz) {
      @Override
      public Item get(final long i) {
        return value.itemAt(order[(int) i]);
      }
    };
  }

  /**
   * Sort the input data.
   * @param vl value list
   * @param sf calling function
   * @param coll collation
   * @return item order
   * @throws QueryException query exception
   */
  public static Integer[] sort(final ValueList vl, final StandardFunc sf, final Collation coll)
      throws QueryException {

    final int al = vl.size();
    final Integer[] order = new Integer[al];
    for(int o = 0; o < al; o++) order[o] = o;
    try {
      Arrays.sort(order, (i1, i2) -> {
        try {
          final Value v1 = vl.get(i1), v2 = vl.get(i2);
          final long s1 = v1.size(), s2 = v2.size(), sl = Math.min(s1, s2);
          for(int v = 0; v < sl; v++) {
            Item m = v1.itemAt(v), n = v2.itemAt(v);
            if(m == Dbl.NAN || m == Flt.NAN) m = null;
            if(n == Dbl.NAN || n == Flt.NAN) n = null;
            if(m != null && n != null && !m.comparable(n)) throw diffError(m, n, sf.info);
            final int d = m == null ? n == null ? 0 : -1 : n == null ? 1 :
              m.diff(n, coll, sf.info);
            if(d != 0 && d != Item.UNDEF) return d;
          }
          return (int) (s1 - s2);
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
    final Expr ex1 = exprs[0];
    final int el = exprs.length;
    final SeqType st1 = ex1.seqType();
    if(st1.zero()) return ex1;

    if(ex1 instanceof Value) {
      final Value v = value((Value) ex1);
      if(v != null) return v;
    }
    if(el == 3) coerceFunc(2, cc, SeqType.AAT_ZM, st1.type.seqType());

    return adoptType(ex1);
  }

  /**
   * Evaluate value arguments.
   * @param value value
   * @return sorted value or {@code null}
   */
  private Value value(final Value value) {
    if(exprs.length < 2) {
      // range values
      if(value instanceof RangeSeq) {
        final RangeSeq seq = (RangeSeq) value;
        return seq.asc ? seq : seq.reverse();
      }
      // sortable single or singleton values
      final SeqType st = value.seqType();
      if(st.type.isSortable() && (st.one() || value instanceof SingletonSeq)) return value;
    }
    // no pre-evaluation possible
    return null;
  }
}
