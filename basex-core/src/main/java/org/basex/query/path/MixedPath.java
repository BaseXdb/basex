package org.basex.query.path;

import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Mixed path expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class MixedPath extends Path {
  /**
   * Constructor.
   * @param ii input info
   * @param r root expression; can be a {@code null} reference
   * @param s axis steps
   */
  public MixedPath(final InputInfo ii, final Expr r, final Expr... s) {
    super(ii, r, s);
  }

  @Override
  protected Expr compilePath(final QueryContext ctx, final VarScope scp) throws QueryException {
    voidStep(steps, ctx);

    for(int s = 0; s != steps.length; ++s) {
      steps[s] = steps[s].compile(ctx, scp);
      if(steps[s].isEmpty()) return optPre(Empty.SEQ, ctx);
    }
    optSteps(ctx);

    // rewrite to child steps
    final Data data = ctx.data();
    if(data != null && ctx.value.type == NodeType.DOC) {
      final Expr e = children(ctx, data);
      // return optimized expression
      if(e != this) return e.compile(ctx, scp);
    }

    size = size(ctx);
    type = SeqType.get(steps[steps.length - 1].type().type, size);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // creates an iterator from the root value
    final Value v = root != null ? ctx.value(root) : checkCtx(ctx);
    Iter res = v.iter();

    final Value cv = ctx.value;
    final long cs = ctx.size;
    final long cp = ctx.pos;
    try {
      // loop through all expressions
      final int sl = steps.length;
      for(int s = 0; s < sl; s++) {
        final Expr e = steps[s];
        final ValueBuilder vb = new ValueBuilder();

        // map operator: don't remove duplicates and check for nodes
        final boolean path = !(e instanceof Bang);
        ctx.size = res.size();
        ctx.pos = 1;

        // loop through all input items
        int nodes = 0;
        for(Item it; (it = res.next()) != null;) {
          if(path && !(it instanceof ANode)) throw PATHNODE.get(info, it.type);
          ctx.value = it;

          // loop through all resulting items
          final Iter ir = ctx.iter(e);
          for(Item i; (i = ir.next()) != null;) {
            if(i instanceof ANode) nodes++;
            vb.add(i);
          }
          ctx.pos++;
        }

        final long vs = vb.size();
        if(nodes < vs) {
          // check if both nodes and atomic values occur in last result
          if(path && nodes > 0) throw EVALNODESVALS.get(info);
          // check if input for next axis step consists items other than nodes
          if(s + 1 < sl && !(steps[s + 1] instanceof Bang))
            throw PATHNODE.get(info, vb.get(0).type);
        }

        if(path && nodes == vs) {
          // remove potential duplicates from node sets
          final NodeSeqBuilder nc = new NodeSeqBuilder().check();
          for(Item it; (it = vb.next()) != null;) nc.add((ANode) it);
          res = nc.value().cache();
        } else {
          res = vb;
        }
      }
      return res;
    } finally {
      ctx.value = cv;
      ctx.size = cs;
      ctx.pos = cp;
    }
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new MixedPath(info, root == null ? null : root.copy(ctx, scp, vs),
        Arr.copyAll(ctx, scp, vs, steps));
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr e : steps) if(e.uses(v)) return false;
    return super.removable(v);
  }
}
