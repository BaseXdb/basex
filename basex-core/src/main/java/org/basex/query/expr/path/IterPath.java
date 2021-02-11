package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative expression for paths that return nodes in distinct document order.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IterPath extends AxisPath {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression
   * @param steps axis steps
   */
  IterPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
  }

  @Override
  protected Iter iterator(final QueryContext qc) {
    final boolean rt = root != null;

    return new Iter() {
      final int sz = steps.length + (rt ? 0 : -1);
      final Expr[] exprs = rt ? new ExprList(sz + 1).add(root).add(steps).finish() : steps;
      final Iter[] iter = new Iter[sz + 1];
      int pos;

      @Override
      public Item next() throws QueryException {
        if(iter[0] == null) iter[0] = exprs[0].iter(qc);

        final QueryFocus qf = qc.focus;
        final Value value = qf.value;
        try {
          do {
            final Item item = qc.next(iter[pos]);
            if(item == null) {
              if(--pos == -1) return null;
            } else if(pos < sz) {
              qf.value = item;
              pos++;
              iter[pos] = exprs[pos].iter(qc);
            } else {
              return item;
            }
          } while(true);
        } finally {
          qf.value = value;
        }
      }
    };
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    return iterator(qc).value(qc, this);
  }

  @Override
  public IterPath copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Expr rt = root == null ? null : root.copy(cc, vm);
    return copyType(new IterPath(info, rt, Arr.copyAll(cc, vm, steps)));
  }
}
