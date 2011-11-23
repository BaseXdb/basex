package org.basex.query.path;

import static org.basex.query.util.Err.*;
import org.basex.data.Data;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.ItemCache;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Mixed path expression.
 *
 * @author BaseX Team 2005-11, BSD License
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
  protected Expr compPath(final QueryContext ctx) throws QueryException {
    for(final Expr s : steps) checkUp(s, ctx);
    final AxisStep v = voidStep(steps);
    if(v != null) COMPSELF.thrw(input, v);

    for(int s = 0; s != steps.length; ++s) {
      steps[s] = steps[s].comp(ctx);
      if(steps[s].isEmpty()) return Empty.SEQ;
    }
    optSteps(ctx);

    // rewrite to child steps
    final Data data = ctx.data();
    if(data != null && ctx.value.type == NodeType.DOC) {
      final Expr e = children(ctx, data);
      // return optimized expression
      if(e != this) return e.comp(ctx);
    }

    size = size(ctx);
    type = SeqType.get(steps[steps.length - 1].type().type, size);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // creates an iterator from the root value
    final Value v = root != null ? root.value(ctx) : checkCtx(ctx);
    Iter res = v.iter();

    final Value cv = ctx.value;
    final long cs = ctx.size;
    final long cp = ctx.pos;
    try {
      // loop through all expressions
      final int el = steps.length;
      for(int ex = 0; ex < el; ex++) {
        final Expr e = steps[ex];
        final boolean last = ex + 1 == el;
        final ItemCache ic = new ItemCache();

        // this flag indicates if the resulting items contain nodes
        boolean nodes = false;
        ctx.size = res.size();
        ctx.pos = 1;

        // loop through all input items
        for(Item it; (it = res.next()) != null;) {
          if(!it.type.isNode()) NODESPATH.thrw(input, this, it.type);
          ctx.value = it;

          // loop through all resulting items
          final Iter ir = ctx.iter(e);
          for(Item i; (i = ir.next()) != null;) {
            // set node flag
            if(ic.size() == 0) nodes = i.type.isNode();
            // check if both nodes and atomic values occur in last result
            else if(last && nodes != i.type.isNode()) EVALNODESVALS.thrw(input);
            ic.add(i);
          }
          ctx.pos++;
        }

        if(nodes) {
          // remove potential duplicates from node sets
          final NodeCache nc = new NodeCache().random();
          for(Item it; (it = ic.next()) != null;) nc.add((ANode) it);
          res = nc.value().cache();
        } else {
          res = ic;
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
  public int count(final Var v) {
    int c = 0;
    for(final Expr e : steps) c += e.count(v);
    return c + super.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr e : steps) if(e.uses(Use.VAR)) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    for(int e = 0; e != steps.length; ++e) steps[e] = steps[e].remove(v);
    return super.remove(v);
  }
}
