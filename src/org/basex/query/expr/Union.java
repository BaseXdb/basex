package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.query.util.NodeBuilder;
import org.basex.util.Array;

/**
 * Union Expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Union extends Arr {
  /**
   * Constructor.
   * @param e expression list
   */
  public Union(final Expr[] e) {
    super(e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final NodeBuilder nb = new NodeBuilder(false);
    for(final Expr e : expr) {
      final Iter iter = ctx.iter(e);
      Item it;
      while((it = iter.next()) != null) {
        if(!it.node()) Err.nodes(this);
        nb.add((Nod) it);
      }
    }
    return nb.iter();
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    final int el = expr.length;
    for(int e = 0; e != expr.length; e++) {
      if(expr[e].e()) expr = Array.delete(expr, e--);
    }
    if(el != expr.length) ctx.compInfo(OPTEMPTY);
    return this;
  }
  
  @Override
  public String toString() {
    return "(" + toString(" | ") + ")";
  }
}
