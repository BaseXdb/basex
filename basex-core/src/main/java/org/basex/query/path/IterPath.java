package org.basex.query.path;

import static org.basex.query.util.Err.*;

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
 * Iterative path expression for location paths which return sorted and
 * duplicate-free results.
 *
 * @author BaseX Team 2005-14, BSD License
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
  public NodeIter iter(final QueryContext qc) {
    return new NodeIter() {
      final boolean r = root != null;
      Expr[] expr;
      Iter[] iter;
      ANode node;
      int p;

      @Override
      public ANode next() throws QueryException {
        if(iter == null) {
          if(expr == null) {
            expr = steps;
            if(r) {
              // add root as first expression
              expr = new Expr[steps.length + 1];
              expr[0] = root;
              System.arraycopy(steps, 0, expr, 1, steps.length);
            }
          }
          // create iterator array
          iter = new Iter[expr.length];
          iter[0] = qc.iter(expr[0]);
        }

        final Value cv = qc.value;
        final long cp = qc.pos;
        final long cs = qc.size;
        try {
          while(true) {
            final Item it = iter[p].next();
            if(it == null) {
              iter[p] = null;
              if(--p == -1) {
                node = null;
                break;
              }
            } else if(p < iter.length - 1) {
              // ensure that root only returns nodes
              if(r && p == 0 && !(it instanceof ANode))
                throw PATHNODE_X_X_X.get(info, steps[0], it.type, it);
              qc.value = it;
              ++p;
              if(iter[p] == null || !iter[p].reset()) iter[p] = qc.iter(expr[p]);
            } else {
              // remaining steps will always yield nodes
              final ANode n = (ANode) it;
              if(node == null || !node.is(n)) {
                node = n;
                break;
              }
            }
          }
          return node;
        } finally {
          // reset context and return result
          qc.value = cv;
          qc.pos = cp;
          qc.size = cs;
        }
      }

      @Override
      public boolean reset() {
        iter = null;
        node = null;
        p = 0;
        return true;
      }
    };
  }

  @Override
  public IterPath copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Expr rt = root == null ? null : root.copy(qc, scp, vs);
    return copyType(new IterPath(info, rt,  Arr.copyAll(qc, scp, vs, steps)));
  }
}