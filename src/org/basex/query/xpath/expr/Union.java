package org.basex.query.xpath.expr;

import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;

/**
 * XPath Union Expression. This expresses the union of two node sets.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Union extends ArrayExpr {
  /**
   * Constructor.
   * @param e operands joined with the union operator
   */
  public Union(final Expr[] e) {
    exprs = e;
  }

  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    int[] result = ((NodeSet) (ctx.eval(exprs[0]))).nodes;
    for(int i = 1; i != exprs.length; i++) {
      result = Nodes.union(((NodeSet) (ctx.eval(exprs[i]))).nodes, result);
    }
    return new NodeSet(result, ctx);
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    int el = exprs.length;
    for(int e = 0; e != el; e++) {
      exprs[e] = exprs[e].comp(ctx);
      if(exprs[e] instanceof NodeSet && ((NodeSet) exprs[e]).size == 0) {
        final Expr[] tmp = new Expr[exprs.length - 1];
        System.arraycopy(exprs, 0, tmp, 0, e);
        System.arraycopy(exprs, e + 1, tmp, e, --el - --e - 1);
        exprs = tmp;
      }
    }
    return el == 0 ? new NodeSet(ctx) : el == 1 ? exprs[0] : this;
  }
}
