package org.basex.query.path;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.expr.Context;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Var;

/**
 * Path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class MixedPath extends Path {
  /** Expression list. */
  private final Expr[] step;

  /**
   * Constructor.
   * @param i query info
   * @param r root expression; can be null
   * @param s location steps; will at least have one entry
   */
  public MixedPath(final QueryInfo i, final Expr r, final Expr... s) {
    super(i, r);
    step = s;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    for(final Expr e : step) checkUp(e, ctx);
    if(root instanceof Context) root = null;

    final Item ci = ctx.item;
    ctx.item = root(ctx);
    final Type ct = ctx.item != null ? ctx.item.type : null;
    // expressions will not necessarily start from the document node..
    if(ct == Type.DOC) ctx.item.type = Type.ELM;

    Expr e = this;
    for(int i = 0; i != step.length; i++) {
      step[i] = step[i].comp(ctx);
      if(step[i].empty()) {
        e = step[i];
        break;
      }
    }
    if(ct != null) ctx.item.type = ct;
    ctx.item = ci;
    return e;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Item it = root != null ? ctx.iter(root).finish() : checkCtx(ctx);
    final Item c = ctx.item;
    final long cs = ctx.size;
    final long cp = ctx.pos;
    ctx.item = it;
    final Item i = eval(ctx);
    ctx.item = c;
    ctx.size = cs;
    ctx.pos = cp;
    return i.iter();
  }

  /**
   * Evaluates the location path.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item eval(final QueryContext ctx) throws QueryException {
    // simple location step traversal...
    Item it = ctx.item;
    for(final Expr s : step) {
      final SeqIter si = new SeqIter();
      final Iter ir = it.iter();
      ctx.size = it.size(ctx);
      ctx.pos = 1;
      Item i;
      while((i = ir.next()) != null) {
        if(!i.node()) error(NODESPATH, this, i.type);
        ctx.item = i;
        si.add(ctx.iter(s));
        ctx.pos++;
      }

      // either nodes or atomic items are allowed in a result set, but not both
      if(si.size() != 0 && si.get(0).node()) {
        final NodIter ni = new NodIter(true);
        while((i = si.next()) != null) {
          if(!i.node()) error(EVALNODESVALS);
          ni.add((Nod) i);
        }
        it = ni.finish();
      } else {
        while((i = si.next()) != null) {
          if(i.node()) error(EVALNODESVALS);
        }
        it = si.finish();
      }
    }
    return it;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return uses(step, u, ctx);
  }

  @Override
  public boolean removable(final Var v, final QueryContext ctx) {
    for(final Expr s : step) if(s.uses(Use.VAR, ctx)) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    for(int s = 0; s != step.length; s++) step[s] = step[s].remove(v);
    return super.remove(v);
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    final SeqType ret = step[step.length - 1].returned(ctx);
    return new SeqType(ret.type, SeqType.Occ.ZM);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    super.plan(ser, step);
  }

  @Override
  public String toString() {
    return toString(step);
  }
}
