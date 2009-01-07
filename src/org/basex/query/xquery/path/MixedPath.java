package org.basex.query.xquery.path;

import static org.basex.query.xquery.XQText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.NodeBuilder;

/**
 * Path expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public class MixedPath extends Path {
  /** Expression list. */
  private final Expr[] step;

  /**
   * Constructor.
   * @param r root expression; can be null
   * @param s location steps; will at least have one entry
   */
  public MixedPath(final Expr r, final Expr[] s) {
    super(r);
    step = s;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    for(int i = 0; i != step.length; i++) {
      step[i] = step[i].comp(ctx);
      if(step[i].e()) return step[i];
    }
    return super.comp(ctx);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Item it = root != null ? ctx.iter(root).finish() : ctx.item;
    final Item c = ctx.item;
    final int cs = ctx.size;
    final int cp = ctx.pos;
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
   * @throws XQException evaluation exception
   */
  protected Item eval(final XQContext ctx) throws XQException {
    // simple location step traversal...
    Item it = ctx.item;
    for(final Expr s : step) {
      final SeqIter sb = new SeqIter();
      final Iter ir = it.iter();
      ctx.size = it.size();
      ctx.pos = 1;
      Item i;
      while((i = ir.next()) != null) {
        if(!i.node()) Err.or(NODESPATH, this, i.type);
        ctx.item = i;
        sb.add(ctx.iter(s));
        ctx.pos++;
      }
      it = sb.finish();
    }

    // either nodes or atomic items are allowed in a result set, but not both
    final Iter ir = it.iter();
    Item i = ir.next();
    if(i != null) {
      if(i.node()) {
        final NodeBuilder nb = new NodeBuilder(false);
        nb.add((Nod) i);
        while((i = ir.next()) != null) {
          if(!i.node()) Err.or(EVALNODESVALS);
          nb.add((Nod) i);
        }
        return nb.finish();
      }
      while((i = ir.next()) != null) if(i.node()) Err.or(EVALNODESVALS);
    }
    return it;
  }
  
  @Override
  public boolean uses(final Using u) {
    return super.uses(u, step);
  }

  @Override
  public Type returned() {
    return step[step.length - 1].returned();
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
