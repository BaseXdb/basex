package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * Union expression.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class Union extends Set {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  public Union(final InputInfo ii, final Expr... e) {
    super(ii, e);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);

    for(int e = 0; e != expr.length; ++e) {
      // remove empty operands
      if(expr[e].empty()) {
        expr = Array.delete(expr, e--);
        ctx.compInfo(OPTREMOVE, desc(), Type.EMP);
      }
    }

    // results must always be sorted
    return expr.length == 0 ? Empty.SEQ :
      expr.length == 1 && !dupl ? expr[0] : this;
  }

  @Override
  protected NodIter eval(final Iter[] iter) throws QueryException {
    final NodIter ni = new NodIter().random();
    for(final Iter ir : iter) {
      Item it;
      while((it = ir.next()) != null) ni.add(checkNode(it));
    }
    return ni;
  }

  @Override
  protected NodeIter iter(final Iter[] iter) {
    return new SetIter(iter) {
      @Override
      public Nod next() throws QueryException {
        if(item == null) {
          item = new Nod[iter.length];
          for(int i = 0; i != iter.length; ++i) next(i);
        }

        int m = -1;
        for(int i = 0; i != item.length; ++i) {
          if(item[i] == null) continue;
          final int d = m == -1 ? 1 : item[m].diff(item[i]);
          if(d == 0) {
            next(i--);
          } else if(d > 0) {
            m = i;
          }
        }
        if(m == -1) return null;

        final Nod it = item[m];
        next(m);
        return it;
      }
    };
  }
}
