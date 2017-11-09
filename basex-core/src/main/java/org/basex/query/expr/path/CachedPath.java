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
 * Axis path expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class CachedPath extends AxisPath {
  /**
   * Constructor.
   * @param info input info
   * @param root root expression; can be a {@code null} reference
   * @param steps axis steps
   */
  CachedPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
  }

  @Override
  protected NodeIter nodeIter(final QueryContext qc) throws QueryException {
    final QueryFocus qf = qc.focus, focus = new QueryFocus();
    final Value rt = root != null ? qc.value(root) : qf.value;
    qc.focus = focus;
    final ANodeBuilder list = new ANodeBuilder();
    try {
      if(rt != null) {
        final Iter iter = qc.iter(rt);
        for(Item it; (it = iter.next()) != null;) {
          // check if root returns nodes
          if(root != null && !(it instanceof ANode))
            throw PATHNODE_X_X_X.get(info, steps[0], it.type, it);
          focus.value = it;
          iter(0, list, qc);
        }
      } else {
        focus.value = null;
        iter(0, list, qc);
      }
    } finally {
      qc.focus = qf;
    }
    return list.iter();
  }

  /**
   * Recursive step iterator.
   * @param step current step
   * @param list node cache
   * @param qc query context
   * @throws QueryException query exception
   */
  private void iter(final int step, final ANodeBuilder list, final QueryContext qc)
      throws QueryException {

    // cast is safe (steps will always return a {@link NodeIter} instance)
    final NodeIter ni = (NodeIter) qc.iter(steps[step]);
    final boolean more = step + 1 != steps.length;
    for(ANode node; (node = ni.next()) != null;) {
      if(more) {
        qc.focus.value = node;
        iter(step + 1, list, qc);
      } else {
        qc.checkStop();
        list.add(node);
      }
    }
  }

  @Override
  public AxisPath copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Expr rt = root == null ? null : root.copy(cc, vm);
    return copyType(new CachedPath(info, rt, Arr.copyAll(cc, vm, steps)));
  }
}
