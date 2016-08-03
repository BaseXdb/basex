package org.basex.query.expr.path;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative path expression for location paths that return sorted and duplicate-free results.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class IterPath extends AxisPath {
  /** Focus. */
  private final QueryFocus focus = new QueryFocus();

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
  protected NodeIter nodeIter(final QueryContext qc) {
    return new NodeIter() {
      final boolean r = root != null;
      final int sz = steps.length + (r ? 1 : 0);
      final Expr[] exprs = r ? new ExprList(sz).add(root).add(steps).finish() : steps;
      final Iter[] iter = new Iter[sz];
      ANode last;
      int pos = -1;

      @Override
      public ANode next() throws QueryException {
        final QueryFocus qf = qc.focus;
        if(pos == -1) {
          iter[++pos] = qc.iter(exprs[0]);
          focus.value = qf.value;
        }
        qc.focus = focus;

        try {
          while(true) {
            final Item it = iter[pos].next();
            if(it == null) {
              if(--pos == -1) return null;
            } else if(pos < sz - 1) {
              // ensure that the root expression yields nodes
              if(pos++ == 0 && r && !(it instanceof ANode))
                throw PATHNODE_X_X_X.get(info, steps[0], it.type, it);
              focus.value = it;
              iter[pos] = qc.iter(exprs[pos]);
            } else {
              // cast is safe (axis steps will always yield nodes); skip identical nodes
              final ANode n = (ANode) it;
              if(last == null || !last.is(n)) {
                last = n;
                return n;
              }
            }
          }
        } finally {
          qc.focus = qf;
        }
      }
    };
  }

  @Override
  public IterPath copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Expr rt = root == null ? null : root.copy(cc, vm);
    return copyType(new IterPath(info, rt,  Arr.copyAll(cc, vm, steps)));
  }
}
