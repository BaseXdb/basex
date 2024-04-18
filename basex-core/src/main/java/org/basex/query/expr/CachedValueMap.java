package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Map expression for processing values.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CachedValueMap extends ValueMap {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  public CachedValueMap(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      QueryFocus qf;
      Iter iter;

      @Override
      public Item next() throws QueryException {
        final QueryFocus focus = qc.focus;
        try {
          if(iter == null) {
            qf = map(qc);
            iter = exprs[exprs.length - 1].iter(qc);
          }
          qc.focus = qf;
          return iter.next();
        } finally {
          qc.focus = focus;
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final QueryFocus focus = qc.focus;
    try {
      map(qc);
      return exprs[exprs.length - 1].value(qc);
    } finally {
      qc.focus = focus;
    }
  }

  /**
   * Performs value mapping for all operands except for the last.
   * @param qc query context
   * @return new query focus
   * @throws QueryException query exception
   */
  private QueryFocus map(final QueryContext qc) throws QueryException {
    final QueryFocus qf = new QueryFocus();
    qf.value = exprs[0].value(qc);
    qc.focus = qf;
    final int last = exprs.length - 1;
    for(int e = 1; e < last; e++) {
      qf.value = exprs[e].value(qc);
    }
    return qf;
  }

  @Override
  public CachedValueMap copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CachedValueMap(info, Arr.copyAll(cc, vm, exprs)));
  }
}
