package org.basex.query.ft;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;
import org.basex.util.TokenBuilder;

/**
 * FTAnd expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FTIntersection extends FTExpr {
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
  /** Collect each pointer once for a result.*/
  private TokenBuilder col = new TokenBuilder();
  
  /**
   * Constructor.
   * @param p pointer on positive expression
   * @param n pointer on negative expression
   * @param e expression list
   */
  FTIntersection(final int[] p, final int[] n, final FTExpr... e) {
    super(e);
    pex = p;
    nex = n;
    cp = new FTNodeItem[p.length];
    cn = new FTNodeItem[n.length];
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    return super.comp(ctx);
  }
  
  @Override
  public FTNodeIter iter(final QueryContext ctx) {
    return new FTNodeIter(){
      @Override
      public FTNodeItem next() throws QueryException {
        return FTIntersection.this.next(ctx);
      }
    };
  }

  /**
   * Gets next FTNodeItem.
   * @param ctx current context
   * @return FTNodeItem
   * @throws QueryException Exception
   */
  FTNodeItem next(final QueryContext ctx) throws QueryException {
    if(pex.length > 0) {
      moreP(ctx);
    } else {
      moreN(ctx);
    }
    
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
        if (d != 0) {
          return n1;
        } else {
            nod2 = null;
            return next(ctx);
        }
      }
      if (col != null && ctx.ftdata != null) 
        ctx.ftdata.addFTAndCol(col.finish());
      col = null;
      return n1;
    } else if (cp.length == 0) {
      final FTNodeItem n2 = calcFTAnd(cn, ctx, false);
      return n2;
    }
    return new FTNodeItem();
  }

  /**
   * Calculates FTAnd for the node n and the current node.
   * @param n FTNode
   * @param ctx XPContext
   * @param p flag for positive expression
   * @return FTNode as result node
   * @throws QueryException Exception
   */
  private FTNodeItem calcFTAnd(final FTNodeItem[] n, final QueryContext ctx,
      final boolean p) throws QueryException {
    if (n.length == 0) return  new FTNodeItem();
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

    for(int i = 0; i < n.length; i++) {
      // color highlighting - limit number of tokens to 128
      if(col != null) col.add((byte) (n[i].ftn.getNumTokens() & 0x7F));
      if(i == 0) continue;
      n2 = n[i];
      n1.ftn.reset();
      n1.merge(n2, 0);
      n1.ftn.reset();
    }
    return n1;
  }

  /**
   * Checks if more values are available.
   * @param ctx QueryContext
   * @return boolean
   * @throws QueryException XQException
   */
  private boolean moreN(final QueryContext ctx) throws QueryException {
    for (int i = 0; i < cn.length; i++) {
      cn[i] = expr[nex[i]].iter(ctx).next();
      if (cn[i].ftn.size == 0) return false;
    }
    return true;
  }

  /**
   * Checks if more values are available.
   * @param ctx QueryContext
   * @return boolean
   * @throws QueryException XQException
   */
  private boolean moreP(final QueryContext ctx) throws QueryException {
    for (int i = 0; i < cp.length; i++) {
      cp[i] = expr[pex[i]].iter(ctx).next();
      if (cp[i].ftn.size == 0) return false;
    }
    return true;
  }

  /**
   * Gets next FTNodeItem.
   * @param n list with cached items
   * @param i pointer on n
   * @param p flag if n is positive or negative
   * @param ctx current context
   * @return FTNodeItem
   * @throws QueryException Exception
   */
  private FTNodeItem more(final FTNodeItem[] n, final int i, final boolean p,
      final QueryContext ctx) throws QueryException {
    //final FTNodeItem tmp = n[i];
    if (p) n[i] = expr[pex[i]].iter(ctx).next();
    else n[i] = expr[nex[i]].iter(ctx).next();
    return n[i]; //tmp;
  }

  @Override
  public String toString() {
    return toString(" ftintersection ");
  }
}
