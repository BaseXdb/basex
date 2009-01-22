package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.query.util.NodeBuilder;

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
    final NodeBuilder nodes = new NodeBuilder(false);

    Iter iter = ctx.iter(expr[0]);
    Item it;
    while((it = iter.next()) != null) {
      if(!(it.node())) Err.nodes(this);
      nodes.add((Nod) it);
    }

    for(int e = 1; e != expr.length; e++) {
      iter = ctx.iter(expr[e]);
      while((it = iter.next()) != null) {
        if(!(it.node())) Err.nodes(this);
        final Nod node = (Nod) it;
        for(int s = 0; s < nodes.size; s++) {
          if(CmpN.Comp.EQ.e(nodes.list[s], node)) nodes.delete(s--);
        }
      }
    }
    return nodes.iter();
  }

  @Override
  public String toString() {
    return "(" + toString(" except ") + ")";
  }
}
