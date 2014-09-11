package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnSort extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);

    final int al = (int) value.size();
    final Value[] values = new Value[al];
    if(exprs.length > 1) {
      final FItem key = checkArity(exprs[1], 1, qc);
      for(int v = 0; v < al; v++) values[v] = key.invokeValue(qc, info, value.itemAt(v));
    } else {
      for(int v = 0; v < al; v++) values[v] = value.itemAt(v);
    }

    final Integer[] perm = new Integer[al];
    for(int p = 0; p < al; p++) perm[p] = p;
    try {
      Arrays.sort(perm, new Comparator<Integer>() {
        @Override
        public int compare(final Integer i1, final Integer i2) {
          try {
            final Value v1 = values[i1], v2 = values[i2];
            final long s1 = v1.size(), s2 = v2.size(), sl = Math.min(s1, s2);
            for(int v = 0; v < sl; v++) {
              final int d = v1.itemAt(v).diff(v2.itemAt(v), sc.collation, info);
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

    final Item[] result = new Item[al];
    for(int r = 0; r < al; r++) result[r] = value.itemAt(perm[r]);
    return Seq.get(result);
  }
}
