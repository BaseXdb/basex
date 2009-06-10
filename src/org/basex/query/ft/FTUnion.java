package org.basex.query.ft;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.util.IntList;

/**
 * FTUnion expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
final class FTUnion extends FTExpr {
  /** Saving index of positive expressions. */
  final int[] pex;
  /** Flag if one result was a ftnot. */
  final boolean not;
  
  /**
   * Constructor.
   * @param posex pointer on expression
   * @param ftnot flag for ftnot expression
   * @param e expression list
   */
  FTUnion(final int[] posex, final boolean ftnot, final FTExpr... e) {
    super(e);
    pex = posex;
    not = ftnot;
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    // initialize iterators
    final FTIter[] ir = new FTIter[expr.length];
    for(int i = 0; i < expr.length; i++) ir[i] = expr[i].iter(ctx);
    
    return new FTIter() {
      /** Item array. */
      final FTItem[] it = new FTItem[pex.length];
      /** Cache for one of the nodes. */
      final IntList cp = new IntList(pex);
      /** Pointer on the positive expression with the lowest pre-values.*/
      int minp = -1;

      @Override
      public FTItem next() throws QueryException { 
        // [SG] is b needed?
        //boolean b = false;
        for(int i = 0; i < cp.size; i++) {
          final int p = pex[cp.list[i]];
          it[p] = ir[p].next();
          //if(!b) b = !mp[i].ftn.empty();
        }
        cp.reset();
        //if(!b) for(final FTNodeItem c : mp) if(!c.ftn.empty()) break;

        if(minp == -1) {
          minp = 0;
          while(minp < it.length && it[minp].empty()) minp++;
          if(minp < it.length) cp.set(minp, 0);
          for(int ip = minp + 1; ip < pex.length; ip++) {
            if(!it[ip].empty()) {
              final FTItem n1 = it[pex[ip]];
              final FTItem n2 = it[pex[minp]];
              final int d = n1.fte.pre - n2.fte.pre;
              if(d < 0) {
                minp = ip;
                cp.set(ip, 0);
              } else if(d == 0) {
                cp.add(ip);
              }
            }
          }
        }

        minp = -1;
        final FTItem m = it[pex[cp.list[0]]];
        for(int i = 1; i < cp.size; i++) {
          m.union(ctx, it[pex[cp.list[i]]], 0);
          // in case of ftor !"a" ftor "b" "a b" is result
          m.fte.not = false;
        }

        // ftnot causes to set this flag (seq. index mode)
        if(m.empty()) m.fte.not = not;
        return m;
      }
    };
  }

  @Override
  public String toString() {
    return toString(" ftunion ");
  }
}
