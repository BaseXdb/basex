package org.basex.query.xpath.expr;

import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Nod;

/**
 * Union Expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Union extends Arr {
  /**
   * Constructor.
   * @param e operands joined with the union operator
   */
  public Union(final Expr[] e) {
    super(e);
  }

  @Override
  public Nod eval(final XPContext ctx) throws QueryException {
    int[] result = ((Nod) (ctx.eval(expr[0]))).nodes;
    for(int i = 1; i != expr.length; i++) {
      result = Nodes.union(((Nod) (ctx.eval(expr[i]))).nodes, result);
    }
    return new Nod(result, ctx);
  }

  @Override
  public String toString() {
    return toString(" | ");
  }
}
