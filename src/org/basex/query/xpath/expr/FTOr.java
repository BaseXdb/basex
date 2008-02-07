package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.OPTOR4;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.util.ExprList;
import org.basex.query.xpath.locpath.PredSimple;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;

/**
 * Logical FTOr expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTOr extends FTArrayExpr {
  /**
   * Constructor.
   * @param e expressions
   */
  public FTOr(final Expr[] e) {
    exprs = e;
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    for(final Expr e : exprs) if(e.eval(ctx).bool()) return Bool.TRUE;
    return Bool.FALSE;
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
   
    Expr tmp;
    ExprList ftun = new ExprList();
    ExprList other = new ExprList();

    // find index equivalents
    for(int i = 0; i != exprs.length; i++) {
      if (exprs[i] instanceof FTUnaryNot) { 
        ftun.add(exprs[i]);
      } else {
        tmp = exprs[i].indexEquivalent(ctx, curr);
        if(tmp == null) return null;
        other.add(tmp);
      }
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
      }*/
    }

    
       
    if (ftun.get().length > 0 && other.get().length > 0) {
      for (int i = 0; i < curr.preds.size(); i++) {
        if (curr.preds.get(i) instanceof PredSimple) {
          PredSimple ps = (PredSimple) curr.preds.get(i);
          
          if (ps.getExpr() instanceof FTContains) {
            Expr[] e = new Expr[ftun.get().length + 1];
            e[0] = new FTUnion(other.get());
            
            System.arraycopy(ftun.get(), 0, e, 1, ftun.get().length);
            Or o = new Or(e);
            
            System.out.println(o);
          }
          
        }
      }

      
      
      Expr[] e = new Expr[ftun.get().length + 1];
      e[0] = new FTUnion(other.get());
      
      System.arraycopy(ftun.get(), 0, e, 1, ftun.get().length);
      
      return new Or(e);
    }
    /*
    // perform path step only once if all path expressions are the same
    final Expr[] ex = XPOptimizer.getIndexExpr(indexExprs);
    if(ex != null) return new Path(new Union(ex),
        ((Path) indexExprs[0]).expr2);
     return new FTUnion(indexExprs);
     */
    // <SG> add compiler infos??
    ctx.compInfo(OPTOR4);
    return new FTUnion(other.get());
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
