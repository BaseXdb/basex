package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.util.TokenBuilder;

/**
 * Expression List.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class List extends Arr {
  /**
   * Constructor.
   * @param l expression list
   */
  public List(final Expr[] l) {
    super(l);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    for(final Expr e : expr) if(!e.i() && !e.e()) return this;
    // all values are items - return simple sequence
    return iter(ctx).finish();
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final SeqIter seq = new SeqIter();
    for(final Expr e : expr) seq.add(ctx.iter(e));
    return seq;
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
  public String toString() {
    final TokenBuilder sb = new TokenBuilder(name()).add('(');
    for(int v = 0; v != expr.length; v++) {
      sb.add((v != 0 ? ", " : "") + expr[v]);
      /*if(sb.size > 15 && v + 1 != expr.length) {
        sb.add(", ...");
        break;
      }*/
    }
    return sb.add(')').toString();
  }
}
