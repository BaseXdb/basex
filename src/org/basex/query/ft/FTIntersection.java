package org.basex.query.ft;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;
import org.basex.util.TokenBuilder;

/**
 * FTIntersection expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
final class FTIntersection extends FTExpr {
  /** Saving index of positive expressions. */
  final int[] pex;
  /** Saving index of negative expressions (FTNot). */
  final int[] nex;
  /** Temporary node.  */
  FTNodeItem nod2;
  /** Cache for positive expression. */
  final FTNodeItem[] cp;
  /** Cache for negative expression. */
  final FTNodeItem[] cn;
  /** Collect each pointer once for a result.*/
  TokenBuilder col = new TokenBuilder();
  
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
  public FTNodeIter iter(final QueryContext ctx) {
    return new FTNodeIter(){
      @Override
      public FTNodeItem next() throws QueryException {
        if(pex.length > 0) {
          moreP();
        } else {
          moreN();
        }
        
        final FTNodeItem n1 = calcFTAnd(cp, true);
        if(!n1.ftn.empty()) {
          nod2 = nex.length > 0 && nod2 == null && moreN() ?
              calcFTAnd(cn, false) : nod2;
          if(nod2 != null) {
            int d = n1.ftn.pre() - nod2.ftn.pre();
            while(d > 0) {
              if(!moreN()) break;
              nod2 = calcFTAnd(cn, false);
              if(nod2.ftn.empty()) break;
              d = n1.ftn.pre() - nod2.ftn.pre();
            }
            if(d != 0) return n1;
            nod2 = null;
            return next();
          }
          // add color to visualization
          if(ctx.ftpos != null && col != null) ctx.ftpos.addCol(col.finish());
          col = null;
          return n1;
        }
        return cp.length == 0 ? calcFTAnd(cn, false) : new FTNodeItem(0);
      }

      /**
       * Calculates FTAnd for the node n and the current node.
       * @param n FTNode
       * @param p flag for positive expression
       * @return FTNode as result node
       * @throws QueryException Exception
       */
      private FTNodeItem calcFTAnd(final FTNodeItem[] n, final boolean p)
          throws QueryException {

        if(n.length == 0) return new FTNodeItem(0);
        if(n.length == 1) return n[0];

        FTNodeItem n1 = n[0];
        FTNodeItem n2;
        for(int i = 1; i < n.length; i++) {
          n2 = n[i];
          if(n1.ftn.empty()) return n1;
          if(n2.ftn.empty()) return n2;
          int d = n1.ftn.pre() - n2.ftn.pre();
          while(d != 0) {
            if(d < 0) {
              if(i != 1) {
                i = 1;
                n2 = n[i];
              }
              n1 = more(n, 0, p);
              if(n1.ftn.empty()) return n1;
            } else {
              n2 = more(n, i, p);
              if(n2.ftn.empty()) return n2;
            }
            d = n1.ftn.pre() - n2.ftn.pre();
          }
        }

        for(int i = 0; i < n.length; i++) {
          // color highlighting - limit number of tokens to 128
          if(col != null) col.add((byte) (n[i].ftn.getNumTokens() & 0x7F));
          if(i == 0) continue;
          n2 = n[i];
          n1.ftn.reset();
          n1.union(ctx, n2, 0);
          n1.ftn.reset();
        }
        return n1;
      }

      /**
       * Checks if more values are available.
       * @return boolean
       * @throws QueryException XQException
       */
      private boolean moreN() throws QueryException {
        for(int i = 0; i < cn.length; i++) {
          cn[i] = expr[nex[i]].iter(ctx).next();
          if(cn[i].ftn.empty()) return false;
        }
        return true;
      }

      /**
       * Checks if more values are available.
       * @return boolean
       * @throws QueryException XQException
       */
      private boolean moreP() throws QueryException {
        for(int i = 0; i < cp.length; i++) {
          cp[i] = expr[pex[i]].iter(ctx).next();
          if(cp[i].ftn.empty()) return false;
        }
        return true;
      }

      /**
       * Gets next FTNodeItem.
       * @param n list with cached items
       * @param i pointer on n
       * @param p flag if n is positive or negative
       * @return FTNodeItem
       * @throws QueryException Exception
       */
      private FTNodeItem more(final FTNodeItem[] n, final int i,
           final boolean p) throws QueryException {

        if(p) n[i] = expr[pex[i]].iter(ctx).next();
        else n[i] = expr[nex[i]].iter(ctx).next();
        return n[i];
      }
    };
  }

  @Override
  public String toString() {
    return toString(" ftintersection ");
  }
}
