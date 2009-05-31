package org.basex.query.ft;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.iter.FTNodeIter;
import org.basex.util.IntList;

/**
 * FTUnion expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
final class FTUnion extends FTExpr {
  /** Cache for one of the nodes. */
  final IntList cp;
  /** Flag is set, if ith expression has any result. */
  final FTNodeItem[] mp;
  /** Pointer on the positive expression with the lowest pre-values.*/
  int minp = -1;
  /** Saving index of positive expressions. */
  final int[] pex;
  /** Flag if one result was a ftnot. */
  final boolean not;
  
  /**
   * Constructor.
   * @param posex pointer on expression
   * @param ftnot flag for ftnot expression
   * @param e expression list
   * 
   */
  FTUnion(final int[] posex, final boolean ftnot, final FTExpr... e) {
    super(e);
    pex = posex;
    mp = new FTNodeItem[pex.length];
    cp = new IntList(pex);
    not = ftnot;
  }

  @Override
  public FTNodeIter iter(final QueryContext ctx) {
    return new FTNodeIter() {
      @Override
      public FTNodeItem next() throws QueryException { 
        boolean b = false;
        for(int i = 0; i < cp.size; i++) {
          final int p = pex[cp.list[i]];
          mp[p] = expr[p].iter(ctx).next();
          if(!b) b = !mp[i].ftn.empty();
        }
        cp.reset();
        if(!b) for(final FTNodeItem c : mp) if(!c.ftn.empty()) break;

        if(minp == -1) {
          minp = 0;
          while(minp < mp.length && mp[minp].ftn.empty()) minp++;
          if(minp < mp.length) cp.set(minp, 0);
          for(int ip = minp + 1; ip < pex.length; ip++) {
            if(!mp[ip].ftn.empty()) {
              final FTNodeItem n1 = mp[pex[ip]];
              final FTNodeItem n2 = mp[pex[minp]];
              if(n1.ftn.pre() < n2.ftn.pre()) {
                minp = ip;
                cp.set(ip, 0);
              } else if(n1.ftn.pre() == n2.ftn.pre()) {
                cp.add(ip);
              }
            }
          }
        }

        minp = -1;
        final FTNodeItem m = mp[pex[cp.list[0]]];
        for(int i = 1; i < cp.size; i++) {
          m.union(ctx, mp[pex[cp.list[i]]], 0);
          // in case of ftor !"a" ftor "b" "a b" is result
          m.ftn.not = false;
        }

        // ftnot causes to set this flag (seq. index mode)
        if(m.ftn.empty()) m.ftn.not = not;
        return m;
      }
    };
  }

  @Override
  public String toString() {
    return toString(" ftunion ");
  }
}
