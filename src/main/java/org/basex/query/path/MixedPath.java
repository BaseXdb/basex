package org.basex.query.path;

import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Mixed path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    final Value c = ctx.resource.value;
    final long cs = ctx.size;
    final long cp = ctx.pos;
    ctx.resource.value = v;
    final ItemIter ir = eval(ctx);
    ctx.resource.value = c;
    ctx.size = cs;
    ctx.pos = cp;
    return ir;
  }

  /**
   * Evaluates the location path.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private ItemIter eval(final QueryContext ctx) throws QueryException {
    // simple location step traversal...
    ItemIter res = ItemIter.get(ctx.resource.value.iter(ctx));
    for(final Expr e : expr) {
      final Iter ir = res;
      final ItemIter ii = new ItemIter();
      ctx.size = ir.size();
      ctx.pos = 1;
      Item it;
      while((it = ir.next()) != null) {
        if(!it.node()) NODESPATH.thrw(input, this, it.type);
        ctx.resource.value = it;
        ii.add(ctx.iter(e));
        ctx.pos++;
      }

      // either nodes or atomic items are allowed in a result set, but not both
      if(ii.size() != 0 && ii.get(0).node()) {
        final NodIter ni = new NodIter().random();
        while((it = ii.next()) != null) {
          if(!it.node()) EVALNODESVALS.thrw(input);
          ni.add((Nod) it);
        }
        res = ItemIter.get(ni);
      } else {
        while((it = ii.next()) != null) {
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
