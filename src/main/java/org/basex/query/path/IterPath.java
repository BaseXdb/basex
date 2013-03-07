package org.basex.query.path;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative path expression for location paths which return sorted and
 * duplicate-free results.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class IterPath extends AxisPath {
  /**
   * Constructor.
   * @param ii input info
   * @param r root expression
   * @param s axis steps
   * @param t return type
   * @param c cardinality
   */
  IterPath(final InputInfo ii, final Expr r, final Expr[] s, final SeqType t,
      final long c) {
    super(ii, r, s);
    type = t;
    size = c;
  }

  @Override
  public NodeIter iter(final QueryContext ctx) {
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
          iter[0] = ctx.iter(expr[0]);
        }

        final Value cv = ctx.value;
        final long cp = ctx.pos;
        final long cs = ctx.size;
        try {
          while(true) {
            final Item it = iter[p].next();
            if(it == null) {
              if(--p == -1) {
                node = null;
                break;
              }
            } else if(p < iter.length - 1) {
              // ensure that root only returns nodes
              if(r && p == 0 && !(it instanceof ANode)) PATHNODE.thrw(info, it.type);
              ctx.value = it;
              ++p;
              if(iter[p] == null || !iter[p].reset()) iter[p] = ctx.iter(expr[p]);
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
          ctx.value = cv;
          ctx.pos = cp;
          ctx.size = cs;
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
  public IterPath copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return copyType(new IterPath(info, root == null ? null : root.copy(ctx, scp, vs),
        Arr.copyAll(ctx, scp, vs, steps), type, size));
  }
}