package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeBuilder;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Array;

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
    final NodeBuilder tmp = new NodeBuilder();

    for(int i = 1; i != exprs.length; i++) {
      final int rl = result.length;
      if(rl == 0) break;

      final int[] merge = ((NodeSet) (ctx.eval(exprs[i]))).nodes;
      final int ml = merge.length;
      
      int m = 0;
      int r = 0;
      while(m != ml && r != rl) {
        final int d = merge[m] - result[r];
        if(d >= 0) {
          if(d == 0) tmp.add(merge[m++]);
          r++;
        } else {
          m++;
        }
      }
      result = Array.finish(tmp.nodes, tmp.size);
      tmp.reset();
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
