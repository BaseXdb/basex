package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.NodeBuilder;

/**
 * Intersect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class InterSect extends Arr {
  /**
   * Constructor.
   * @param l expression list
   */
  public InterSect(final Expr[] l) {
    super(l);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    NodeBuilder seq = new NodeBuilder(false);

    Iter iter = ctx.iter(expr[0]);
    Item it;
    while((it = iter.next()) != null) {
      if(!it.node()) Err.nodes(this);
      seq.add((Nod) it);
    }

    for(int e = 1; e != expr.length; e++) {
      final NodeBuilder res = new NodeBuilder(false);
      iter = ctx.iter(expr[e]);
      while((it = iter.next()) != null) {
        if(!it.node()) Err.nodes(this);
        final Nod node = (Nod) it;
        for(int s = 0; s < seq.size; s++) {
          if(CmpN.Comp.EQ.e(seq.list[s], node)) {
            res.add(node);
            break;
          }
        }
      }
      seq = res;
    }
    return seq.iter();
  }

  @Override
  public String toString() {
    return "(" + toString(" & ") + ")";
  }
}
