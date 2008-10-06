package org.basex.query.xpath.expr;

import org.basex.index.FTNode;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.Bool;

/**
 * FTIntersection Expression. 
 * This expresses the intersection of two FTContains results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTIntersection extends FTArrayExpr {
  /** Saving index of positive expressions. */
  private int[] pex;
  /** Saving index of negative expressions (FTNot). */
  private int[] nex;
  /** Temp FTNode.  */
  private FTNode nod2;
  
  /**
   * Constructor.
   * @param e operands joined with the union operator
   * @param pexpr IntList with indexes of positive expressions
   * @param nexpr IntList with indexes of negative expressions
   * @Deprecated
   */
  public FTIntersection(final FTArrayExpr[] e, final int[] pexpr, 
      final int[] nexpr) {
    exprs = e;
    pex = pexpr;
    nex = nexpr;
  }
  
  /**
   * Checks if more values are available.
   * @param n pointer on exprs
   * @return boolean
   */
  private boolean more(final int[] n) {
    for (int i : n) if (!exprs[i].more()) return false;
    return true;
  }
  
  @Override 
  public boolean pos() {
    for (FTArrayExpr i : exprs) if (i.pos()) return true;
    return false;
  }

  @Override
  public boolean more() {
    if(pex.length > 0) return more(pex);
    return more(nex);
  }
  
  /**
   * Calculates FTAnd for the node n and the current node.
   * @param n FTNode
   * @param ctx XPContext
   * @return FTNode as resultnode
   */
  private FTNode calcFTAnd(final int[] n, final XPContext ctx) {
    if (n.length == 0) return null;
    else if (n.length == 1) return exprs[n[0]].next(ctx);
    
    FTNode n1 = exprs[n[0]].next(ctx);
    FTNode n2;
    for (int i = 1; i < n.length; i++) {
      n2 = exprs[n[i]].next(ctx);
      if (n1.size == 0 || n2.size == 0) return new FTNode();
      int d = n1.getPre() - n2.getPre();
      while(d != 0) {
        if (d < 0) {
          if (i != 1) {
            i = 1;
            n2 = exprs[n[i]].next(ctx);
          }
          if (exprs[n[0]].more())
            n1 = exprs[n[0]].next(ctx);
          else return new FTNode();
        } else {
          if (exprs[n[i]].more())
            n2 = exprs[n[i]].next(ctx);
          else return new FTNode();
        }
        d = n1.getPre() - n2.getPre();
      }
      //if (!n1.merge(n2, 0)) return new FTNode();
    }
    
    for (int i = 1; i < n.length; i++) {
      n2 = exprs[n[i]].next(ctx);
      //n1.merge(n2, i - 1);
      n1.reset();
      n1.merge(n2, 0);
      n1.reset();
    }
    return n1;
  }

  @Override
  public FTNode next(final XPContext ctx) {
    final FTPositionFilter tmp = ctx.ftpos;
    ctx.ftpos = ftpos;
    
    final FTNode n1 = calcFTAnd(pex, ctx);
    
/*    nod2 = (nex.length > 0 && nod2 == null && more(nex)) ? 
        calcFTAnd(nex, ctx) : nod2;
*/
    if (n1 != null) {
      nod2 = (nex.length > 0 && nod2 == null && more(nex)) ? 
          calcFTAnd(nex, ctx) : nod2;
      if (nod2 != null) {
        int d = n1.getPre() - nod2.getPre();
        while (d > 0) {
          if (!more(nex)) break;
          nod2 = calcFTAnd(nex, ctx);
          if (nod2.size > 0) {
            d = n1.getPre() - nod2.getPre();
          } else {
            break;
          }
        }
        if (d != 0) return n1;
        else {
          if (more()) {
            nod2 = null;
            return next(ctx);
          } else return new FTNode();
        }
      }
      ctx.ftpos = tmp;
      return n1;
    }
    final FTNode n2 = calcFTAnd(nex, ctx);  
    ctx.ftpos = tmp;
    return n2;
  }
  
  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    
    // check each positive expression
    for (int i : pex) {
      final Bool it = (Bool) exprs[i].eval(ctx);
      if (!it.bool()) return it;
    }
    
    for (int i : nex) {
      final Bool it = (Bool) exprs[i].eval(ctx);
      if (!it.bool()) return it;
    }
  
    return Bool.TRUE;
  }
}
