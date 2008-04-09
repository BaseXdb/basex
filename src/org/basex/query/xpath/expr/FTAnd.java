package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.OPTAND4;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.internal.FTIndex;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;

/**
 * Logical FTAnd expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTAnd extends FTArrayExpr {
  /** Flag for order preserving. */
  boolean pres = false;
  
  /**
   * Constructor.
   * @param e expressions
   * @param options FTOptions
   */
  public FTAnd(final Expr[] e, final FTOption options) {
    exprs = e;
    super.fto = options;
  }
  
  /**
   * Constructor.
   * @param e expressions
   * @param preserving flag for order preserving
   */
  public FTAnd(final Expr[] e, final boolean preserving) {
    exprs = e;
    pres = preserving;
  }
  
  

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    for(final Expr e : exprs) if(!ctx.eval(e).bool()) return Bool.get(false);
    return Bool.get(true);
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

    Expr[] indexExprs = new Expr[exprs.length];
    int j = 0;
    int i = 0;
    
    // find index equivalents
    for(; i != exprs.length && j < exprs.length; i++) {
      indexExprs[j] = exprs[i].indexEquivalent(ctx, curr);
      if(indexExprs[j] == null) return null;
      if (i > 0 && indexExprs[j - 1] instanceof FTIndex 
          && indexExprs[j] instanceof FTUnaryNotExprs) {
        FTUnaryNotExprs e = (FTUnaryNotExprs) indexExprs[j];
        Expr[] ex = new Expr[e.exprs.length + 1];
        ex[0] = indexExprs[j - 1];
        System.arraycopy(e.exprs, 0, ex, 1, e.exprs.length);
        e.exprs = ex;
        
        // there are no other expressions contained in FTAnd
        if (exprs.length == 2) {
          return e;
        } else {
          indexExprs[j - 1] = e;
          j--;
        }
       
      }
      j++;
    }
   
    if (j != i) { 
      Expr[] ie = new Expr[j];
      System.arraycopy(indexExprs, 0, ie, 0, j);
      indexExprs = ie; 
    }
    
    // <SG> add compiler infos??
    ctx.compInfo(OPTAND4);
/*
    if (option.ftPosFilt != null) { 
      if (option.ftPosFilt.equals(FTOption.POSFILTER.ORDERED)) {
        return new FTIntersection(indexExprs, true);
      } else if (option.ftPosFilt.equals(FTOption.POSFILTER.WINDOW)) {
      
      }
    }
*/
    return new FTIntersection(indexExprs, pres, ctx);
    
    //return new FTIntersection(indexExprs, option, ctx);
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
