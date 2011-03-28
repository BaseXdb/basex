package org.basex.query.path;

import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
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
 * @author Sebastian Gath
 */
public final class MixedPath extends Path {
  /** Expression list. */
  private final Expr[] expr;

  /**
   * Constructor.
   * @param ii input info
   * @param r root expression; can be null
   * @param s location steps; will at least have one entry
   */
  public MixedPath(final InputInfo ii, final Expr r, final Expr... s) {
    super(ii, r);
    expr = s;
  }

  @Override
  protected Expr compPath(final QueryContext ctx) throws QueryException {
    for(final Expr e : expr) checkUp(e, ctx);

    for(int e = 0; e != expr.length; ++e) {
      expr[e] = expr[e].comp(ctx);
      if(expr[e].empty()) return Empty.SEQ;
    }
    type = SeqType.get(expr[expr.length - 1].type().type, SeqType.Occ.ZM);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Value v = root != null ? root.value(ctx) : checkCtx(ctx);
    final Value c = ctx.value;
    final long cs = ctx.size;
    final long cp = ctx.pos;
    ctx.value = v;
    final ItemCache ic = eval(ctx);
    ctx.value = c;
    ctx.size = cs;
    ctx.pos = cp;
    return ic;
  }

  /**
   * Evaluates the location path.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private ItemCache eval(final QueryContext ctx) throws QueryException {
    // simple location step traversal...
    ItemCache res = ItemCache.get(ctx.value.iter());
    for(final Expr e : expr) {
      final Iter ir = res;
      final ItemCache ii = new ItemCache();
      ctx.size = ir.size();
      ctx.pos = 1;
      for(Item it; (it = ir.next()) != null;) {
        if(!it.node()) NODESPATH.thrw(input, this, it.type);
        ctx.value = it;
        ii.add(ctx.iter(e));
        ctx.pos++;
      }

      // either nodes or atomic items are allowed in a result set, but not both
      if(ii.size() != 0 && ii.get(0).node()) {
        final NodeCache nc = new NodeCache().random();
        for(Item it; (it = ii.next()) != null;) {
          if(!it.node()) EVALNODESVALS.thrw(input);
          nc.add((ANode) it);
        }
        res = ItemCache.get(nc);
      } else {
        for(Item it; (it = ii.next()) != null;) {
          if(it.node()) EVALNODESVALS.thrw(input);
        }
        res = ii;
      }
    }
    res.reset();
    return res;
  }

  @Override
  public boolean uses(final Use u) {
    return uses(expr, u);
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final Expr e : expr) c += e.count(v);
    return c + super.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr e : expr) if(e.uses(Use.VAR)) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    for(int e = 0; e != expr.length; ++e) expr[e] = expr[e].remove(v);
    return super.remove(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    super.plan(ser, expr);
  }

  @Override
  public String toString() {
    return toString(expr);
  }
}
