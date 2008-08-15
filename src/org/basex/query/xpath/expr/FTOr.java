package org.basex.query.xpath.expr;


import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;
import org.basex.util.IntList;

/**
 * Logical FTOr expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTOr extends FTArrayExpr {
  /** Saving index of expressions. */
  private int[] pex;
  

  /**
   * Constructor.
   * @param e expressions
   */
  public FTOr(final FTArrayExpr[] e) {
    exprs = e;
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    for(final Expr e : exprs) if(ctx.eval(e).bool()) return Bool.TRUE;
    return Bool.FALSE;
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
    
    if (pex.length == 1 && seq) 
      exprs[pex[0]].indexEquivalent(ctx, curr, seq);
    
    FTArrayExpr[] indexExprs = new FTArrayExpr[exprs.length];
    for (int i = 0; i < exprs.length; i++) {
      indexExprs[i] = exprs[i].indexEquivalent(ctx, curr, seq); 
    }
    
    return new FTUnion(indexExprs, pex);

    
   /* FTArrayExpr tmp;
    FTArrayExpr[] ftun = {};
    FTArrayExpr[] other = {};
*/
    // find index equivalents
    /*for(int i = 0; i != exprs.length; i++) {
      
      /*
      if (exprs[i] instanceof FTUnaryNot) { 
        ftun = Array.add(ftun, exprs[i]);
      } else {
        tmp = exprs[i].indexEquivalent(ctx, curr);
        if(tmp == null) return null;
        other = Array.add(other, tmp);
      }*/
      /*
      tmp = exprs[i].indexEquivalent(ctx, curr);
      if(tmp == null) return null;
      
      if (tmp instanceof FTUnaryNotExprs) {
        ftun.add(tmp);
        
        /*if (i-j-1 > 0 ) {
          es = new Expr[i - j - 1];
          System.arraycopy(exprs, j, es, 0, i -j - 1);
          ftune.add(new FTUnion(es));
        }
        j = i;
      } else {
        other.add(tmp);
      }
    }*/

    
       /*
    if (ftun.length > 0 && other.length > 0) {
      for (int i = 0; i < curr.preds.size(); i++) {
        if (curr.preds.get(i) instanceof PredSimple) {
          PredSimple ps = (PredSimple) curr.preds.get(i);
          
          if (ps.getExpr() instanceof FTContains) {
            FTArrayExpr[] e = new FTArrayExpr[ftun.length + 1];
            e[0] = new FTUnion(other);
            
            System.arraycopy(ftun, 0, e, 1, ftun.length);
            //Or o = new Or(e);
          }
          
        }
      }

      
      
      FTArrayExpr[] e = new FTArrayExpr[ftun.length + 1];
      e[0] = new FTUnion(other);
      System.arraycopy(ftun, 0, e, 1, ftun.length);
      return new FTOr(e); */
    //}
    
    /*
    // perform path step only once if all path expressions are the same
    final Expr[] ex = XPOptimizer.getIndexExpr(indexExprs);
    if(ex != null) return new Path(new Union(ex),
        ((Path) indexExprs[0]).expr2);
     return new FTUnion(indexExprs);
     */
    // <SG> add compiler infos??
    //ctx.compInfo(OPTOR4);
    //return new FTUnion(other);
  }
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    IntList i1 = new IntList();
    boolean seq = false;
    int sum = 0;
    int nrIDs = 0;
    
    for (int i = 0; i < exprs.length; i++) {
      nrIDs = exprs[i].indexSizes(ctx, curr, min);
      if (!(exprs[i] instanceof FTUnaryNot)) {
        i1.add(i);
        sum += nrIDs;
        //if(sum > min) return min;
      } else if (nrIDs > 0) {
        i1.add(i);
        seq = true;
      }
    }
    
    pex = i1.finish();
    if (seq) return Integer.MAX_VALUE;
    return sum > min ? min : sum;
    /*
    for(final Expr expr : exprs) {
      final int nrIDs = expr.indexSizes(ctx, curr, min);
      if(nrIDs == Integer.MAX_VALUE) return nrIDs;
      sum += nrIDs;
      if(sum > min) return min;
    }
    return sum > min ? min : sum;
    */
  }
}
