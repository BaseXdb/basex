package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.NodeBuilder;
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
  public Iter iter(final XQContext ctx) throws XQException {
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
  public Expr comp(final XQContext ctx) throws XQException {
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
