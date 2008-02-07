package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;

/**
 * Logical FTMildNot expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTMildNot extends FTArrayExpr {
  /**
   * Constructor.
   * @param e expressions
   */
  public FTMildNot(final Expr[] e) {
    exprs = e;
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    boolean found = false;
    for(final Expr e : exprs) {
      if(e.eval(ctx).bool()) {
        found = true;
      } 
    }
    
    return Bool.get(found);
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    for(int i = 0; i != exprs.length; i++) {
      if (fto != null && exprs[i] instanceof FTArrayExpr) {
        FTArrayExpr ftae = (FTArrayExpr) exprs[i];
        if (ftae.fto == null) ftae.fto = fto;
      }
      exprs[i] = exprs[i].compile(ctx);
    }
    return this;
   }
  
  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr)
      throws QueryException {
    
    final Expr[] indexExprs = new Expr[exprs.length];
    
    // find index equivalents
    for(int i = 0; i != exprs.length; i++) {
      indexExprs[i] = exprs[i].indexEquivalent(ctx, curr);
      if(indexExprs[i] == null) indexExprs[i] = exprs[i];
    }

    // perform path step only once if all path expressions are the same
    final Expr[] ex = XPOptimizer.getIndexExpr(indexExprs);
    if(ex != null) return new Path(new FTMildNotExprs(ex),
        ((Path) indexExprs[0]).expr2);
    // <SG> add compiler infos??
    return new FTMildNotExprs(indexExprs);
  }
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    int sum = 0;
    for(final Expr expr : exprs) {
      final int nrIDs = expr.indexSizes(ctx, curr, min);
      if(nrIDs == Integer.MAX_VALUE) return nrIDs;
      sum += nrIDs;
      if(sum > min) return min;
    }
    return sum > min ? min : sum;
  }
}
