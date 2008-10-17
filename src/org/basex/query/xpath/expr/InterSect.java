package org.basex.query.xpath.expr;

import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;

/**
 * XPath Intersect Expression. This expresses the intersection of two node sets.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class InterSect extends ArrayExpr {
  /**
   * Constructor.
   * @param e operands expression
   */
  public InterSect(final Expr[] e) {
    exprs = e;
  }

  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    int[] result = ((NodeSet) (ctx.eval(exprs[0]))).nodes;

    for(int i = 1; i != exprs.length; i++) {
      result = Nodes.intersect(((NodeSet) (ctx.eval(exprs[i]))).nodes, result);
      if(result.length == 0) break;
    }
    return new NodeSet(result, ctx);
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e != el; e++) exprs[e] = exprs[e].compile(ctx);
    return el == 0 ? new NodeSet(ctx) : el == 1 ? exprs[0] : this;
  }
}
