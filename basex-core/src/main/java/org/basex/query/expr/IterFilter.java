package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative filter expression without numeric predicates.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IterFilter extends Filter {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param preds predicate expressions
   */
  IterFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, root, preds);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter iter = root.iter(qc);

      @Override
      public Item next() throws QueryException {
        final QueryContext q = qc;
        final Iter ir = iter;
        for(Item item; (item = q.next(ir)) != null;) {
          if(test(item, q)) return item;
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter iter = root.iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(test(item, qc)) vb.add(item);
    }
    return vb.value(this);
  }

  @Override
  public IterFilter copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new IterFilter(info, root.copy(cc, vm), copyAll(cc, vm, exprs)));
  }
}
