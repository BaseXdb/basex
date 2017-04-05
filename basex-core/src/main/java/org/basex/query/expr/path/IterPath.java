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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class IterPath extends AxisPath {
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
      Expr[] exprs;
      Iter[] iter;
      QueryFocus focus;
      ANode last;
      int pos, sz;
      boolean r;

      @Override
      public ANode next() throws QueryException {
        final QueryFocus qf = qc.focus;
        if(exprs == null) init(qf);
        qc.focus = focus;

        try {
          do {
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
            qc.checkStop();
          } while(true);
        } finally {
          qc.focus = qf;
        }
      }

      private void init(final QueryFocus qf) throws QueryException {
        r = root != null;
        sz = steps.length + (r ? 1 : 0);
        exprs = r ? new ExprList(sz).add(root).add(steps).finish() : steps;
        iter = new Iter[sz];
        iter[0] = qc.iter(exprs[0]);
        focus = new QueryFocus();
        focus.value = qf.value;
      }
    };
  }

  @Override
  public IterPath copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Expr rt = root == null ? null : root.copy(cc, vm);
    return copyType(new IterPath(info, rt,  Arr.copyAll(cc, vm, steps)));
  }
}
