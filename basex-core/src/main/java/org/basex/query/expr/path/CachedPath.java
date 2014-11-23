package org.basex.query.expr.path;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Axis path expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class CachedPath extends AxisPath {
  /** Flag for result caching. */
  private boolean cache;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression; can be a {@code null} reference
   * @param steps axis steps
   */
  CachedPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
    // analyze if result set can be cached: no root node, no variables...
    cache = root != null && !hasFreeVars();
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value cv = qc.value;
    final long cp = qc.pos, cs = qc.size;
    final Value r = root != null ? qc.value(root) : cv;

    try {
      final NodeSeqBuilder nb = new NodeSeqBuilder().check();
      if(r != null) {
        final Iter ir = qc.iter(r);
        for(Item it; (it = ir.next()) != null;) {
          // ensure that root only returns nodes
          if(root != null && !(it instanceof ANode))
            throw PATHNODE_X_X_X.get(info, steps[0], it.type, it);
          qc.value = it;
          iter(0, nb, qc);
        }
      } else {
        qc.value = null;
        iter(0, nb, qc);
      }
      return nb.sort();
    } finally {
      qc.value = cv;
      qc.size = cs;
      qc.pos = cp;
    }
  }

  /**
   * Recursive step iterator.
   * @param l current step
   * @param nc node cache
   * @param qc query context
   * @throws QueryException query exception
   */
  private void iter(final int l, final NodeSeqBuilder nc, final QueryContext qc)
      throws QueryException {

    // cast is safe (steps will always return a {@link NodeIter} instance)
    final NodeIter ni = (NodeIter) qc.iter(steps[l]);
    final boolean more = l + 1 != steps.length;
    for(ANode node; (node = ni.next()) != null;) {
      if(more) {
        qc.value = node;
        iter(l + 1, nc, qc);
      } else {
        qc.checkStop();
        nc.add(node);
      }
    }
  }

  @Override
  public AxisPath copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr rt = root == null ? null : root.copy(qc, scp, vs);
    final CachedPath ap = copyType(new CachedPath(info, rt, Arr.copyAll(qc, scp, vs, steps)));
    ap.cache = cache;
    return ap;
  }
}
