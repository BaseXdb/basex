package org.basex.query.ft;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.util.TokenBuilder;

/**
 * FTIntersection expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
final class FTIntersection extends FTExpr {
  /** Index of positive expressions. */
  final int[] pex;
  /** Index of negative (ftnot) expressions. */
  final int[] nex;
  
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
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    // initialize iterators
    final FTIter[] ir = new FTIter[expr.length];
    for(int i = 0; i < expr.length; i++) ir[i] = expr[i].iter(ctx);

    return new FTIter() {
      /** Cache for positive expressions. */
      final FTItem[] cp = new FTItem[pex.length];
      /** Cache for negative expressions. */
      final FTItem[] cn = new FTItem[nex.length];
      /** Collect each pointer once for a result.*/
      TokenBuilder col = new TokenBuilder();
      /** Temporary node.  */
      FTItem nod2;

      @Override
      public FTItem next() throws QueryException {
        if(pex.length > 0) {
          moreP();
        } else {
          moreN();
        }
        
        final FTItem n1 = calcFTAnd(cp, true);
        if(!n1.empty()) {
          nod2 = nex.length > 0 && nod2 == null && moreN() ?
              calcFTAnd(cn, false) : nod2;
          if(nod2 != null) {
            int d = n1.fte.pre() - nod2.fte.pre();
            while(d > 0) {
              if(!moreN()) break;
              nod2 = calcFTAnd(cn, false);
              if(nod2.empty()) break;
              d = n1.fte.pre() - nod2.fte.pre();
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
        return cp.length == 0 ? calcFTAnd(cn, false) : new FTItem();
      }

      /**
       * Calculates FTAnd for the node n and the current node.
       * @param n FTNode
       * @param p flag for positive expression
       * @return FTNode as result node
       * @throws QueryException Exception
       */
      private FTItem calcFTAnd(final FTItem[] n, final boolean p)
          throws QueryException {

        if(n.length == 0) return new FTItem();
        if(n.length == 1) return n[0];

        FTItem n1 = n[0];
        FTItem n2;
        for(int i = 1; i < n.length; i++) {
          n2 = n[i];
          if(n1.empty()) return n1;
          if(n2.empty()) return n2;
          int d = n1.fte.pre() - n2.fte.pre();
          while(d != 0) {
            if(d < 0) {
              if(i != 1) {
                i = 1;
                n2 = n[i];
              }
              n1 = more(n, 0, p);
              if(n1.empty()) return n1;
            } else {
              n2 = more(n, i, p);
              if(n2.empty()) return n2;
            }
            d = n1.fte.pre() - n2.fte.pre();
          }
        }

        for(int i = 0; i < n.length; i++) {
          // color highlighting - limit number of tokens to 128
          if(col != null) col.add((byte) (n[i].fte.getTokenNum() & 0x7F));
          if(i != 0) n1.union(ctx, n[i], 0);
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
          cn[i] = ir[nex[i]].next();
          if(cn[i].empty()) return false;
        }
        return true;
      }

      /**
       * Checks if more values are available.
       * @throws QueryException XQException
       */
      private void moreP() throws QueryException {
        for(int i = 0; i < cp.length; i++) {
          cp[i] = ir[pex[i]].next();
          if(cp[i].empty()) break;
        }
      }

      /**
       * Gets next FTNodeItem.
       * @param n list with cached items
       * @param i pointer on n
       * @param p flag if n is positive or negative
       * @return FTNodeItem
       * @throws QueryException Exception
       */
      private FTItem more(final FTItem[] n, final int i, final boolean p)
          throws QueryException {

        n[i] = ir[p ? pex[i] : nex[i]].next();
        return n[i];
      }
    };
  }

  @Override
  public String toString() {
    return toString(" ftintersection ");
  }
}
