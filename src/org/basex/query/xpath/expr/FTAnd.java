package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;
import org.basex.util.IntList;

/**
 * Logical FTAnd expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTAnd extends FTArrayExpr {
  /** Saving index of positive expressions. */
  private int[] pex;
  /** Saving index of negative expressions (FTNot). */
  private int[] nex;
  
  /**
   * Constructor.
   * @param e expressions
   */
  public FTAnd(final FTArrayExpr[] e) {
    exprs = e;
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    for(final Expr e : exprs) if(!ctx.eval(e).bool()) return Bool.get(false);
    return Bool.get(true);
  }

  @Override
  public FTArrayExpr compile(final XPContext ctx) throws QueryException {
    for(int i = 0; i != exprs.length; i++) {
      if (fto != null) {
        if (exprs[i].fto == null) exprs[i].fto = fto;
      }
      exprs[i] = exprs[i].compile(ctx);
    }
    return this;
  }
  
  @Override
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq)
      throws QueryException {
    if (pex.length == 1 && nex.length == 0) 
      exprs[pex[0]].indexEquivalent(ctx, curr, seq);
    
    FTArrayExpr[] indexExprs = new FTArrayExpr[exprs.length];
    for (int i = 0; i < exprs.length; i++) {
      indexExprs[i] = exprs[i].indexEquivalent(ctx, curr, seq); 
    }
    
    return new FTIntersection(indexExprs, pex, nex);
/*    
    // find index equivalents
    for(; i != exprs.length && j < exprs.length; i++) {
      indexExprs[j] = exprs[i].indexEquivalent(ctx, curr);
      if(indexExprs[j] == null) return null;
      if (i > 0 && indexExprs[j - 1] instanceof FTIndex 
          && indexExprs[j] instanceof FTUnaryNotExprs) {
        FTUnaryNotExprs e = (FTUnaryNotExprs) indexExprs[j];
        FTArrayExpr[] ex = new FTArrayExpr[e.exprs.length + 1];
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
      FTArrayExpr[] ie = new FTArrayExpr[j];
      System.arraycopy(indexExprs, 0, ie, 0, j);
      indexExprs = ie; 
    }
    
    // <SG> add compiler infos??
    ctx.compInfo(OPTAND4);
/*
    if (option.ftPosFilt != null) { 
      if (option.ftPosFilt == FTOption.POSFILTER.ORDERED) {
        return new FTIntersection(indexExprs, true);
      } else if (option.ftPosFilt == FTOption.POSFILTER.WINDOW) {
      
      }
    }
*/
   // return new FTIntersection(indexExprs, false);
    
    //return new FTIntersection(indexExprs, option, ctx);
  }
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    IntList i1 = new IntList();
    IntList i2 = new IntList();
    int nmin = min;
    int nrIDs;
    for (int i = 0; i < exprs.length; i++) {
      nrIDs = exprs[i].indexSizes(ctx, curr, min);
      if (!(exprs[i] instanceof FTUnaryNot)) {
        i1.add(i);
        nmin = (nrIDs < nmin) ? nrIDs : nmin;
      } else if (nrIDs > 0) {
        i2.add(i);
      }
    }
    pex = i1.finish();
    nex = i2.finish();
    
    if (i1.size == 0) return Integer.MAX_VALUE;
    return nmin;
  }  
}
