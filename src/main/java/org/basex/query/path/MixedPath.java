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
    for(final Expr e : step) checkUp(e, ctx);
    final AxisStep s = voidStep(step);
    if(s != null) COMPSELF.thrw(input, s);

    for(int e = 0; e != step.length; ++e) {
      step[e] = step[e].comp(ctx);
      if(step[e].empty()) return Empty.SEQ;
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
    type = SeqType.get(step[step.length - 1].type().type, size);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Value v = root != null ? root.value(ctx) : checkCtx(ctx);
    final Value c = ctx.value;
    final long cs = ctx.size;
    final long cp = ctx.pos;
    ctx.value = v;
    final Iter ir = eval(ctx);
    ctx.value = c;
    ctx.size = cs;
    ctx.pos = cp;
    return ir;
  }

  /**
   * Evaluates the mixed path expression.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Iter eval(final QueryContext ctx) throws QueryException {
    // creates an initial item cache
    Iter res = ctx.value.iter();
    // loop through all expressions
    final int el = step.length;
    for(int ex = 0; ex < el; ex++) {
      final Expr e = step[ex];
      final boolean last = ex + 1 == el;
      final ItemCache ii = new ItemCache();
      ctx.size = res.size();
      ctx.pos = 1;
      // this flag indicates if the resulting items contain nodes
      boolean nodes = false;
      // loop through all input items
      for(Item it; (it = res.next()) != null;) {
        if(!it.node()) NODESPATH.thrw(input, this, it.type);
        ctx.value = it;
        // loop through all resulting items
        final Iter ir = ctx.iter(e);
        for(Item i; (i = ir.next()) != null;) {
          // set node flag
          if(ii.size() == 0) nodes = i.node();
          // check if both nodes and atomic values occur in last result
          else if(last && nodes != i.node()) EVALNODESVALS.thrw(input);
          ii.add(i);
        }
        ctx.pos++;
      }
      if(nodes) {
        // remove potential duplicates from node sets
        final NodeCache nc = new NodeCache().random();
        for(Item it; (it = ii.next()) != null;) nc.add((ANode) it);
        res = nc.finish().cache();
      } else {
        res = ii;
      }
    }
    return res;
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final Expr e : step) c += e.count(v);
    return c + super.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr e : step) if(e.uses(Use.VAR)) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    for(int e = 0; e != step.length; ++e) step[e] = step[e].remove(v);
    return super.remove(v);
  }
}
