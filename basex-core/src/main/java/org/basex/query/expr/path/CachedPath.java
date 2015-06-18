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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class CachedPath extends AxisPath {
  /** Flag for result caching. */
  private boolean cache;
  /** Cached results. */
  private ANodeList clist;
  /** Cached context value. */
  private Value cvalue;

  /**
   * Constructor.
   * @param info input info
   * @param root root expression; can be a {@code null} reference
   * @param steps axis steps
   */
  public CachedPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
    // cache values if expression has no free variables, is deterministic and performs no updates
    cache = root instanceof Root && !hasFreeVars() &&
        !has(Flag.NDT) && !has(Flag.UPD) && !has(Flag.HOF);
  }

  @Override
  public BasicNodeIter iter(final QueryContext qc) throws QueryException {
    final Value cv = qc.value;
    ANodeList nl = clist;
    if(nl == null || cv != cvalue) {
      final long cp = qc.pos, cs = qc.size;
      final Value r = root != null ? qc.value(root) : cv;
      nl = new ANodeList().check();
      try {
        if(r != null) {
          final Iter ir = qc.iter(r);
          for(Item it; (it = ir.next()) != null;) {
            // ensure that root only returns nodes
            if(root != null && !(it instanceof ANode))
              throw PATHNODE_X_X_X.get(info, steps[0], it.type, it);
            qc.value = it;
            iter(0, nl, qc);
          }
        } else {
          qc.value = null;
          iter(0, nl, qc);
        }
      } finally {
        qc.value = cv;
        qc.size = cs;
        qc.pos = cp;
      }
      if(cache) {
        clist = nl;
        cvalue = cv;
      }
    }
    return nl.iter();
  }

  /**
   * Recursive step iterator.
   * @param step current step
   * @param nl node cache
   * @param qc query context
   * @throws QueryException query exception
   */
  private void iter(final int step, final ANodeList nl, final QueryContext qc)
      throws QueryException {

    // cast is safe (steps will always return a {@link NodeIter} instance)
    final NodeIter ni = (NodeIter) qc.iter(steps[step]);
    final boolean more = step + 1 != steps.length;
    for(ANode node; (node = ni.next()) != null;) {
      if(more) {
        qc.value = node;
        iter(step + 1, nl, qc);
      } else {
        qc.checkStop();
        nl.add(node);
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
