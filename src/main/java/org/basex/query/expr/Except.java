package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.Err;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * Except expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Except extends Set {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  public Except(final InputInfo ii, final Expr[] e) {
    super(ii, e);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(expr[0].empty()) return optPre(Empty.SEQ, ctx);

    for(int e = 1; e < expr.length; ++e) {
      if(expr[e].empty()) {
        expr = Array.delete(expr, e--);
        ctx.compInfo(OPTREMOVE, desc(), Type.EMP);
      }
    }
    // results must always be sorted
    return expr.length == 1 && !dupl ? expr[0] : this;
  }

  @Override
  protected NodeIter eval(final Iter[] iter) throws QueryException {
    final NodIter ni = new NodIter().random();

    Item it;
    while((it = iter[0].next()) != null) {
      if(!it.node()) Err.type(this, Type.NOD, it);
      ni.add((Nod) it);
    }

    for(int e = 1; e != expr.length; ++e) {
      final Iter ir = iter[e];
      while((it = ir.next()) != null) {
        if(!it.node()) Err.type(this, Type.NOD, it);
        final Nod node = (Nod) it;
        for(int s = 0; s < ni.size(); ++s) {
          if(ni.get(s).is(node)) ni.delete(s--);
        }
      }
    }
    return ni.sort();
  }

  @Override
  protected NodeIter iter(final Iter[] iter) {
    return new NodeIter() {
      Nod[] items;

      @Override
      public Nod next() throws QueryException {
        if(items == null) {
          items = new Nod[iter.length];
          for(int i = 0; i != iter.length; ++i) next(i);
        }

        for(int i = 1; i != items.length; ++i) {
          if(items[0] == null) return null;
          if(items[i] == null) continue;
          final int d = items[0].diff(items[i]);

          if(d < 0) {
            if(i + 1 == items.length) {
              break;
            }
          }
          if(d == 0) {
            next(0);
            i = 0;
          }
          if(d > 0) {
            next(i--);
          }
        }
        final Nod temp = items[0];
        next(0);
        return temp;
      }

      private void next(final int i) throws QueryException {
        final Item it = iter[i].next();
        if(it != null && !it.node()) Err.type(Except.this, Type.NOD, it);
        items[i] = (Nod) it;
      }
    };
  }

  @Override
  public String toString() {
    return "(" + toString(" " + EXCEPT + " ") + ")";
  }
}
