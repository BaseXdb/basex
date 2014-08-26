package org.basex.query.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map expression: iterative evaluation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class IterMap extends SimpleMap {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  IterMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final int sz = exprs.length;
      final Iter[] iter = new Iter[sz];
      final Value[] values = new Value[sz];
      int pos = -1;

      @Override
      public Item next() throws QueryException {
        final Value cv = qc.value;
        // local copy of variable (faster)
        int p = pos;
        if(p == -1) {
          values[++p] = cv;
          iter[p] = qc.iter(exprs[0]);
        }

        try {
          qc.value = values[p];
          while(true) {
            final Item it = iter[p].next();
            if(it == null) {
              iter[p] = null;
              if(--p == -1) {
                pos = p;
                return null;
              }
            } else if(p < sz - 1) {
              qc.value = it;
              values[++p] = it;
              iter[p] = qc.iter(exprs[p]);
            } else {
              pos = p;
              return it;
            }
          }
        } finally {
          qc.value = cv;
        }
      }
    };
  }

  @Override
  public SimpleMap copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new IterMap(info, Arr.copyAll(qc, scp, vs, exprs)));
  }
}
