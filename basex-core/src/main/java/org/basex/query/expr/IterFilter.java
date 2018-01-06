package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative filter expression without numeric predicates.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class IterFilter extends Filter {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param preds predicates
   */
  IterFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, root, preds);
  }

  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      Iter iter;

      @Override
      public Item next() throws QueryException {
        // first call - initialize iterator
        if(iter == null) iter = root.iter(qc);
        // filter sequence
        for(Item item; (item = qc.next(iter)) != null;) {
          if(preds(item, qc)) return item;
        }
        return null;
      }
    };
  }

  @Override
  public IterFilter copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new IterFilter(info, root.copy(cc, vm), Arr.copyAll(cc, vm, exprs)));
  }
}
