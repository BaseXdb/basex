package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.util.IntList;

/**
 * FTAnd expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTAnd extends FTExpr {
  /**
   * Sequential constructor.
   * @param e expression list
   */
  public FTAnd(final FTExpr[] e) {
    super(e);
  }

  /**
   * Index constructor.
   * @param e expression list
   * @param p pointer on positive expression
   * @param n pointer on negative expression
   */
  public FTAnd(final FTExpr[] e, final int[] p, final int[] n) {
    super(e);
    pex = p;
    nex = n;
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    FTItem it = null; 
    for(final FTExpr e : expr) {
      final FTItem i = e.atomic(ctx);
      if(it != null) {
        it.all = it.all.and(i.all);
        it.score(i.score() == 0 ? 0 : ctx.score.and(i.score(), it.score()));
      } else {
        it = i;
      }
      if(it.score() == 0) break;
    }
    return it;
  }

  @Override
  public String toString() {
    return toString(" " + FTAND + " ");
  }



  // [CG] FT: to be revised...
  
  /** Index of positive expressions. */
  int[] pex;
  /** Index of negative (ftnot) expressions. */
  int[] nex;

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
        if(n1 != null) {
          nod2 = nex.length > 0 && nod2 == null && moreN() ?
              calcFTAnd(cn, false) : nod2;
          if(nod2 != null) {
            int d = n1.pre - nod2.pre;
            while(d > 0) {
              if(!moreN()) break;
              nod2 = calcFTAnd(cn, false);
              if(nod2 == null) break;
              d = n1.pre - nod2.pre;
            }
            if(d != 0) return n1;
            nod2 = null;
            return next();
          }
          return n1;
        }
        return cp.length == 0 ? calcFTAnd(cn, false) : null;
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

        if(n.length == 0) return null;
        if(n.length == 1) return n[0];

        FTItem n1 = n[0];
        FTItem n2;
        for(int i = 1; i < n.length; i++) {
          n2 = n[i];
          if(n1 == null || n2 == null) return null;
          int d = n1.pre - n2.pre;
          while(d != 0) {
            if(d < 0) {
              if(i != 1) {
                i = 1;
                n2 = n[i];
              }
              n1 = more(n, 0, p);
              if(n1 == null) return null;
            } else {
              n2 = more(n, i, p);
              if(n2 == null) return null;
            }
            d = n1.pre - n2.pre;
          }
        }

        for(int i = 0; i < n.length; i++) if(i != 0) n1.union(ctx, n[i], 0);
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
          if(cn[i] == null) return false;
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
          if(cp[i] == null) break;
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
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    // [CG] FT: skip index access
    if(1 == 1) return false;

    final IntList ip = new IntList();
    final IntList in = new IntList();
    int nmin = ic.is;
    for(int i = 0; i < expr.length; i++) {
      final boolean ftnot = ic.ftnot;
      ic.ftnot = false;
      final boolean ia = expr[i].indexAccessible(ic);
      final boolean ftn = ic.ftnot;
      ic.ftnot = ftnot;
      if(!ia) return false;
      
      if(!ftn) {
        ip.add(i);
        nmin = ic.is < nmin ? ic.is : nmin;
      } else if(ic.is > 0) {
        in.add(i);
      }
    }
    pex = ip.finish();
    nex = in.finish();
    ic.seq |= ip.size == 0;
    ic.is = ip.size > 0 ? nmin : Integer.MAX_VALUE;
    return true;
  }
  
  @Override
  public FTExpr indexEquivalent(final IndexContext ic) throws QueryException {
    for(int i = 0; i < expr.length; i++) expr[i] = expr[i].indexEquivalent(ic);

    if(pex.length == 0) {
      // !A FTAnd !B = !(a ftor b)
      for(int i = 0; i < expr.length; i++) expr[i] = expr[i].expr[0];
      return new FTNot(new FTOr(expr, nex, false));
    }
    return this;
  }
}
