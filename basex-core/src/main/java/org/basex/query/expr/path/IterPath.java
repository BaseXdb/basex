package org.basex.query.expr.path;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative path expression for location paths that return sorted and duplicate-free results.
 *
 * @author BaseX Team 2005-19, BSD License
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
    return new NodeIter() {
      QueryFocus focus;
      Expr[] exprs;
      Iter[] iter;
      ANode last;
      int pos, sz;
      boolean rt;

      @Override
      public ANode next() throws QueryException {
        final QueryFocus qf = qc.focus;
        if(iter == null) init(qf);
        qc.focus = focus;

        try {
          do {
            final Item item = qc.next(iter[pos]);
            if(item == null) {
              if(--pos == -1) return null;
            } else if(pos < sz - 1) {
              // ensure that the root expression yields nodes
              if(pos++ == 0 && rt && !(item instanceof ANode))
                throw PATHNODE_X_X_X.get(info, steps[0], item.type, item);
              focus.value = item;
              iter[pos] = exprs[pos].iter(qc);
            } else {
              // cast is safe (axis steps will always yield nodes); skip identical nodes
              final ANode n = (ANode) item;
              if(last == null || !last.is(n)) {
                last = n;
                return n;
              }
            }
          } while(true);
        } finally {
          qc.focus = qf;
        }
      }

      private void init(final QueryFocus qf) throws QueryException {
        rt = root != null;
        sz = steps.length + (rt ? 1 : 0);
        exprs = rt ? new ExprList(sz).add(root).add(steps).finish() : steps;
        iter = new Iter[sz];
        iter[0] = exprs[0].iter(qc);
        focus = qf.copy();
      }
    };
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    return iterator(qc).value(qc);
  }

  @Override
  public IterPath copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Expr rt = root == null ? null : root.copy(cc, vm);
    return copyType(new IterPath(info, rt,  Arr.copyAll(cc, vm, steps)));
  }
}
