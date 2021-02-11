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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CachedPath extends AxisPath {
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
  protected Iter iterator(final QueryContext qc) throws QueryException {
    return nodes(qc).iter();
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    final ANodeBuilder list = new ANodeBuilder();

    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    final Value rt = root != null ? root.value(qc) : focus.value;
    qc.focus = qf;
    try {
      if(rt != null) {
        final Iter iter = rt.iter(qc);
        for(Item item; (item = iter.next()) != null;) {
          if(root != null && !(item instanceof ANode))
            throw PATHNODE_X_X_X.get(info, steps[0], item.type, item);
          qf.value = item;
          iterate(0, list, qc);
        }
      } else {
        iterate(0, list, qc);
      }
    } finally {
      qc.focus = focus;
    }
    return list.value(this);
  }

  /**
   * Recursive step iterator.
   * @param step current step
   * @param list node cache
   * @param qc query context
   * @throws QueryException query exception
   */
  private void iterate(final int step, final ANodeBuilder list, final QueryContext qc)
      throws QueryException {

    // cast is safe (steps will always return a {@link NodeIter} instance)
    final NodeIter ni = (NodeIter) steps[step].iter(qc);
    if(step + 1 == steps.length) {
      for(ANode node; (node = ni.next()) != null;) list.add(node);
    } else {
      for(ANode node; (node = ni.next()) != null;) {
        qc.focus.value = node;
        iterate(step + 1, list, qc);
      }
    }
  }

  @Override
  public AxisPath copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final Expr rt = root == null ? null : root.copy(cc, vm);
    return copyType(new CachedPath(info, rt, Arr.copyAll(cc, vm, steps)));
  }
}
