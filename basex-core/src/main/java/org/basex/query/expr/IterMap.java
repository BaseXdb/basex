package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple map expression: iterative evaluation (no positional access).
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class IterMap extends SimpleMap {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  IterMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      final int sz = exprs.length;
      final Iter[] iter = new Iter[sz];
      final Value[] values = new Value[sz];
      int pos = -1;

      @Override
      public Item next() throws QueryException {
        final QueryFocus qf = qc.focus;
        final Value cv = qf.value;
        if(pos == -1) {
          iter[++pos] = qc.iter(exprs[0]);
          values[pos] = cv;
        }
        qf.value = values[pos];

        try {
          while(true) {
            final Item it = iter[pos].next();
            if(it == null) {
              if(--pos == -1) return null;
            } else if(pos < sz - 1) {
              qf.value = it;
              values[++pos] = it;
              iter[pos] = qc.iter(exprs[pos]);
            } else {
              return it;
            }
          }
        } finally {
          qf.value = cv;
        }
      }
    };
  }

  @Override
  public IterMap copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new IterMap(info, Arr.copyAll(cc, vm, exprs)));
  }
}
