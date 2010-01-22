package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.util.TokenBuilder;

/**
 * Expression list.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class List extends Arr {
  /**
   * Constructor.
   * @param l expression list
   */
  public List(final Expr... l) {
    super(l);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);
    updating(ctx, expr);

    for(final Expr e : expr) if(!e.i() && !e.e()) return this;

    // return simple sequence if all values are items
    final SeqIter seq = new SeqIter(expr.length);
    for(final Expr e : expr) seq.add(ctx.iter(e));
    return seq.finish();
  }

  /**
   * Returns if the specified expressions are updating or vacuous.
   * @param ctx query context
   * @param expr expression array
   * @throws QueryException query exception
   */
  static void updating(final QueryContext ctx, final Expr[] expr)
      throws QueryException {
    if(!ctx.updating) return;
    int s = 0;
    for(final Expr e : expr) {
      if(e.v()) continue;
      final boolean u = e.uses(Use.UPD, ctx);
      if(u && s == 2 || !u && s == 1) Err.or(UPNOT);
      s = u ? 1 : 2;
    }
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      Iter ir;
      int e;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(ir == null) {
            if(e == expr.length) return null;
            ir = ctx.iter(expr[e++]);
          }
          final Item it = ir.next();
          if(it != null) return it;
          ir = null;
        }
      }
    };
  }

  @Override
  public long size(final QueryContext ctx) throws QueryException {
    long s = 0;
    for(final Expr e : expr) {
      final long c = e.size(ctx);
      if(c == -1) return -1;
      s += c;
    }
    return s;
  }

  @Override
  public boolean duplicates(final QueryContext ctx) {
    return true;
  }

  @Override
  public String toString() {
    final TokenBuilder sb = new TokenBuilder("(");
    for(int v = 0; v != expr.length; v++) {
      sb.add((v != 0 ? ", " : "") + expr[v]);
    }
    return sb.add(')').toString();
  }
}
