package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.util.InputInfo;
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
   * @param ii input info
   * @param l expression list
   */
  public List(final InputInfo ii, final Expr... l) {
    super(ii, l);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(int e = 0; e != expr.length; e++) expr[e] = expr[e].comp(ctx);
    checkUp(ctx, expr);

    for(final Expr e : expr) if(!e.value()) return this;

    // return simple sequence if all values are items or empty sequences
    final SeqIter seq = new SeqIter(expr.length);
    for(final Expr e : expr) seq.add(ctx.iter(e));
    return seq.finish();
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
  public SeqType returned(final QueryContext ctx) {
    final int size = expr.length;
    Type t = expr[0].returned(ctx).type;
    for(int s = 1; s < size && t != Type.ITEM; s++) {
      if(t != expr[s].returned(ctx).type) t = Type.ITEM;
    }
    return new SeqType(t, SeqType.Occ.OM);
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
