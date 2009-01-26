package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.util.Err;

/**
 * Except Expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Except extends Arr {
  /**
   * Constructor.
   * @param l expression list
   */
  public Except(final Expr[] l) {
    super(l);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final NodIter ni = new NodIter(false);
    Iter iter = ctx.iter(expr[0]);
    Item it;
    while((it = iter.next()) != null) {
      if(!(it.node())) Err.nodes(this);
      ni.add((Nod) it);
    }

    for(int e = 1; e != expr.length; e++) {
      iter = ctx.iter(expr[e]);
      while((it = iter.next()) != null) {
        if(!(it.node())) Err.nodes(this);
        final Nod node = (Nod) it;
        for(int s = 0; s < ni.size; s++) {
          if(CmpN.Comp.EQ.e(ni.list[s], node)) ni.delete(s--);
        }
      }
    }
    return ni;
  }

  @Override
  public String toString() {
    return "(" + toString(" except ") + ")";
  }
}
