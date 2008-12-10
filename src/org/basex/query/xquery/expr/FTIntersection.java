package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.FTNodeItem;
import org.basex.query.xquery.iter.FTNodeIter;

/**
 * FTAnd expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTIntersection extends FTExpr {
  /** Saving index of positive expressions. */
  private int[] pex;
  /** Saving index of negative expressions (FTNot). */
  private int[] nex;
  /** Temp FTNode.  */
  private FTNodeItem nod2;
  /** Cache for positive expression. */
  private FTNodeItem[] cp;
  /** Cache for negative expression. */
  private FTNodeItem[] cn;
  
  /**
   * Constructor.
   * @param p pointer on positive expr
   * @param n pointer on negative expr
   * @param e expression list
   */
  public FTIntersection(final int[] p, final int[] n, final FTExpr... e) {
    super(e);
    pex = p;
    nex = n;
    cp = new FTNodeItem[p.length];
    cn = new FTNodeItem[n.length];
  }

  @Override
  public FTNodeIter iter(final XQContext ctx) {
    return new FTNodeIter(){
      @Override
      public FTNodeItem next() throws XQException { 
        return FTIntersection.this.next(ctx); 
        }
    };
  }
  
  /**
   * Calculates FTAnd for the node n and the current node.
   * @param n FTNode
   * @param ctx XPContext
   * @param p flag for positive expr
   * @return FTNode as resultnode
   * @throws XQException Exception
   */
  private FTNodeItem calcFTAnd(final FTNodeItem[] n, final XQContext ctx, 
      final boolean p) throws XQException {
    if (n.length == 0) return  new FTNodeItem();
//    else if (n.length == 1) return (FTNodeItem) ctx.iter(expr[n[0]]).next();
    else if (n.length == 1) return n[0];
    
    FTNodeItem n1 = n[0];
    FTNodeItem n2;
    for (int i = 1; i < n.length; i++) {
      n2 = n[i];
      if (n1.ftn.size == 0) return n1;
      if (n2.ftn.size == 0) return n2;
      int d = n1.ftn.getPre() - n2.ftn.getPre();
      while(d != 0) {
        if (d < 0) {
          if (i != 1) {
            i = 1;
            n2 = n[i];            
          }
          
          n1 = more(n, 0, p, ctx);
          if(n1.ftn.size == 0) return n1;
        } else {
          n2 = more(n, i, p, ctx); 
          if (n2.ftn.size == 0) return n2;
        }
        d = n1.ftn.getPre() - n2.ftn.getPre();
      }
    }
    
    for (int i = 1; i < n.length; i++) {
      n2 = n[i];
      n1.ftn.reset();
      n1.merge(n2, 0);
      n1.ftn.reset();
    }
    return n1;
  }
  
  /**
   * Get next FTNodeItem.
   * 
   * @param ctx current context
   * @return FTNodeItem
   * @throws XQException Exception
   */
  public FTNodeItem next(final XQContext ctx) throws XQException {
    more(ctx);
    final FTNodeItem n1 = calcFTAnd(cp, ctx, true);
    if (n1.ftn.size > 0) {
      nod2 = (nex.length > 0 && nod2 == null && moreN(ctx)) ? 
          calcFTAnd(cn, ctx, false) : nod2;
      if (nod2 != null) {
        int d = n1.ftn.getPre() - nod2.ftn.getPre();
        while (d > 0) {
          if (!moreN(ctx)) break;
          nod2 = calcFTAnd(cn, ctx, false);
          if (nod2.ftn.size > 0) {
            d = n1.ftn.getPre() - nod2.ftn.getPre();
          } else {
            break;
          }
        }
        if (d != 0) return n1;
        else {
            nod2 = null;
            return next(ctx);
        }
      }
      return n1;
    } else if (cp.length == 0) {
      final FTNodeItem n2 = calcFTAnd(cn, ctx, false);  
      return n2;
    } 
    
    return new FTNodeItem();
  }
  
  /**
   * Checks if more values are available.
   * @param ctx XQContext
   * @return boolean
   * @throws XQException XQException
   */
  private boolean moreN(final XQContext ctx) throws XQException {
    for (int i = 0; i < cn.length; i++) {
      cn[i] = (FTNodeItem) ctx.iter(expr[nex[i]]).next();
      if (cn[i].ftn.size == 0) return false;
    }
    return true;
  }

  /**
   * Checks if more values are available.
   * @param ctx XQContext
   * @return boolean
   * @throws XQException XQException
   */
  private boolean moreP(final XQContext ctx) throws XQException {
    for (int i = 0; i < cp.length; i++) {
      cp[i] = (FTNodeItem) ctx.iter(expr[pex[i]]).next();
      if (cp[i].ftn.size == 0) return false;
    }
    return true;
  }
  

  /**
   * Checks whether more results exist.
   * 
   * @param ctx current context
   * @return boolean more
   * @throws XQException Exception
   */
  public boolean more(final XQContext ctx) throws XQException {
    if(pex.length > 0) return moreP(ctx);
    return moreN(ctx);
  }
  
  /**
   * Gext next FTNodeItem.
   * 
   * @param n list with cached items
   * @param i pointer on n
   * @param p flag if n is positive or negative
   * @param ctx current context
   * @return FTNodeItem
   * @throws XQException Exception
   */
  public FTNodeItem more(final FTNodeItem[] n, final int i, final boolean p, 
      final XQContext ctx) throws XQException {
    //final FTNodeItem tmp = n[i];
    if (p) n[i] = (FTNodeItem) ctx.iter(expr[pex[i]]).next();
    else n[i] = (FTNodeItem) ctx.iter(expr[nex[i]]).next();
    return n[i]; //tmp;
  }

  @Override
  public String toString() {
    return toString(" ftintersection ");
  }
  
}
