package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnSort extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);

    final int sz = (int) value.size();
    final ValueList vl = new ValueList(sz);
    if(exprs.length > 1) {
      final FItem key = checkArity(exprs[1], 1, qc);
      for(final Value v : value) vl.add(key.invokeValue(qc, info, v));
    } else {
      for(final Value v : value) vl.add(v);
    }

    final Integer[] order = sort(vl, this);
    return new ValueIter() {
      int c;
      @Override
      public Item get(final long i) { return value.itemAt(order[(int) i]); }
      @Override
      public Item next() { return c < sz ? get(c++) : null; }
      @Override
      public long size() { return sz; }
      @Override
      public Value value() {
        final ValueBuilder vb = new ValueBuilder(sz);
        for(int r = 0; r < sz; r++) vb.add(get(r));
        return vb.value();
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
   * @return item order
   * @throws QueryException query exception
   */
  public static Integer[] sort(final ValueList vl, final StandardFunc sf) throws QueryException {
    final int al = vl.size();
    final Integer[] order = new Integer[al];
    for(int o = 0; o < al; o++) order[o] = Integer.valueOf(o);
    try {
      Arrays.sort(order, new Comparator<Integer>() {
        @Override
        public int compare(final Integer i1, final Integer i2) {
          try {
            final Value v1 = vl.get(i1), v2 = vl.get(i2);
            final long s1 = v1.size(), s2 = v2.size(), sl = Math.min(s1, s2);
            for(int v = 0; v < sl; v++) {
              final Item it1 = v1.itemAt(v), it2 = v2.itemAt(v);
              if(!it1.comparable(it2)) {
                if(it1 instanceof FItem) throw FIEQ_X.get(sf.info, it1.type);
                if(it2 instanceof FItem) throw FIEQ_X.get(sf.info, it2.type);
                throw diffError(sf.info, it1, it2);
              }
              final int d = it1.diff(it2, sf.sc.collation, sf.info);
              if(d != 0) return d;
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
