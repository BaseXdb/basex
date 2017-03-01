package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnSort extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);
    Collation coll = sc.collation;
    if(exprs.length > 1) {
      final byte[] token = toEmptyToken(exprs[1], qc);
      if(token.length > 0) coll = Collation.get(token, qc, sc, info, WHICHCOLL_X);
    }

    final long sz = value.size();
    final ValueList vl = new ValueList((int) Math.min(Integer.MAX_VALUE, sz));
    if(exprs.length > 1) {
      final FItem key = checkArity(exprs[2], 1, qc);
      for(final Item it : value) vl.add(key.invokeValue(qc, info, it));
    } else {
      for(final Item it : value) vl.add(it.atomValue(info));
    }

    final Integer[] order = sort(vl, this, coll);
    return new BasicIter<Item>(sz) {
      @Override
      public Item get(final long i) {
        return value.itemAt(order[(int) i]);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value();
  }

  /**
   * Sort the input data.
   * @param vl value list.
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
      Arrays.sort(order, new Comparator<Integer>() {
        @Override
        public int compare(final Integer i1, final Integer i2) {
          try {
            final Value v1 = vl.get(i1), v2 = vl.get(i2);
            final long s1 = v1.size(), s2 = v2.size(), sl = Math.min(s1, s2);
            for(int v = 0; v < sl; v++) {
              Item m = v1.itemAt(v), n = v2.itemAt(v);
              if(m == Dbl.NAN || m == Flt.NAN) m = null;
              if(n == Dbl.NAN || n == Flt.NAN) n = null;
              if(m != null && n != null && !m.comparable(n)) {
                throw m instanceof FItem ? FIEQ_X.get(sf.info, m.type) :
                      n instanceof FItem ? FIEQ_X.get(sf.info, n.type) :
                      diffError(m, n, sf.info);
              }
              final int d = m == null ? n == null ? 0 : -1 : n == null ? 1 :
                m.diff(n, coll, sf.info);
              if(d != 0 && d != Item.UNDEF) return d;
            }
            return (int) (s1 - s2);
          } catch(final QueryException ex) {
            throw new QueryRTException(ex);
          }
        }
      });
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
    return order;
  }
}
