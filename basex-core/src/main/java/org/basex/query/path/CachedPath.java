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
 * Axis path expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class CachedPath extends AxisPath {
  /** Flag for result caching. */
  private boolean cache;
  /** Cached result. */
  private NodeSeqBuilder citer;
  /** Last visited item. */
  private Value lvalue;

  /**
   * Constructor.
   * @param ii input info
   * @param r root expression; can be a {@code null} reference
   * @param s axis steps
   */
  CachedPath(final InputInfo ii, final Expr r, final Expr... s) {
    super(ii, r, s);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    final Expr e = super.optimize(ctx, scp);
    if(e != this) return e;

    // analyze if result set can be cached - no predicates/variables...
    cache = root != null && !hasFreeVars();
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Value cv = ctx.value;
    final long cs = ctx.size;
    final long cp = ctx.pos;
    final Value r = root != null ? ctx.value(root) : cv;

    try {
      /* cache values if:
       * - caching is desirable
       * - the code is called for the first time
       * - the value has changed and the underlying node is not the same
       */
      citer = new NodeSeqBuilder().check();
      if(r != null) {
        final Iter ir = ctx.iter(r);
        for(Item it; (it = ir.next()) != null;) {
          // ensure that root only returns nodes
          if(root != null && !(it instanceof ANode)) throw PATHNODE.get(info, it.type);
          ctx.value = it;
          iter(0, citer, ctx);
        }
      } else {
        ctx.value = null;
        iter(0, citer, ctx);
      }
      citer.sort();
      return citer;
    } finally {
      ctx.value = cv;
      ctx.size = cs;
      ctx.pos = cp;
    }
  }

  /**
   * Recursive step iterator.
   * @param l current step
   * @param nc node cache
   * @param ctx query context
   * @throws QueryException query exception
   */
  private void iter(final int l, final NodeSeqBuilder nc, final QueryContext ctx)
      throws QueryException {

    // cast is safe (steps will always return a {@link NodeIter} instance
    final NodeIter ni = (NodeIter) ctx.iter(steps[l]);
    final boolean more = l + 1 != steps.length;
    for(ANode node; (node = ni.next()) != null;) {
      if(more) {
        ctx.value = node;
        iter(l + 1, nc, ctx);
      } else {
        ctx.checkStop();
        nc.add(node);
      }
    }
  }

  @Override
  public AxisPath copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    final Expr[] stps = new Expr[steps.length];
    for(int s = 0; s < steps.length; ++s) stps[s] = step(s).copy(ctx, scp, vs);
    final CachedPath ap = copyType(
        new CachedPath(info, root == null ? null : root.copy(ctx, scp, vs), stps));
    ap.cache = cache;
    if(citer != null) ap.citer = citer.copy();
    if(lvalue != null) ap.lvalue = lvalue;
    return ap;
  }
}
