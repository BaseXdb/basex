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
 * @author BaseX Team 2005-17, BSD License
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
      QueryFocus focus;
      Value[] values;
      Iter[] iter;
      int pos, sz;

      @Override
      public Item next() throws QueryException {
        final QueryFocus qf = qc.focus;
        if(iter == null) init(qf);
        qc.focus = focus;

        try {
          do {
            focus.value = values[pos];
            final Item it = iter[pos].next();
            if(it == null) {
              if(--pos == -1) return null;
            } else if(pos < sz - 1) {
              focus.value = it;
              values[++pos] = it;
              iter[pos] = qc.iter(exprs[pos]);
            } else {
              return it;
            }
            qc.checkStop();
          } while(true);
        } finally {
          qc.focus = qf;
        }
      }

      private void init(final QueryFocus qf) throws QueryException {
        sz = exprs.length;
        iter = new Iter[sz];
        iter[0] = qc.iter(exprs[0]);
        focus = qf.copy();
        values = new Value[sz];
        values[0] = qf.value;
      }
    };
  }

  @Override
  public IterMap copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new IterMap(info, Arr.copyAll(cc, vm, exprs)));
  }

  @Override
  public String description() {
    return "iterative " + super.description();
  }
}
