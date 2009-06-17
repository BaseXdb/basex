package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;

import org.basex.data.FTMatches;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.util.Err;
import org.basex.util.IntList;

/**
 * FTMildnot expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTMildNot extends FTExpr {
  /**
   * Constructor.
   * @param l expression list
   * @throws QueryException query exception
   */
  public FTMildNot(final FTExpr[] l) throws QueryException {
    super(l);
    if(usesExclude()) Err.or(FTMILD);
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    final FTItem item = expr[0].atomic(ctx);
    final FTMatches all = item.all;
    for(int e = 1; e < expr.length; e++) {
      final FTMatches al = expr[e].atomic(ctx).all;
      for(int a = 0; a < all.size; a++) {
        for(int b = 0; b < al.size; b++) {
          if(!all.match[a].notin(al.match[b])) {
            all.delete(a--);
            break;
          }
        }
      }
    }
    if(all.size == 0) item.score(0);
    return item;
  }
  
  @Override
  public String toString() {
    return toString(" " + NOT + " " + IN + " ");
  }


  
  // [CG] FT: to be revised...
  
  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    // [CG] FT: skip index access
    if(1 == 1) return false;

    final int mmin = ic.is;
    IntList il = new IntList(expr.length - 1);
    for (int i = 1; i < expr.length; i++) {
      if(!expr[i].indexAccessible(ic)) return false;
      if (ic.is > 0) il.add(i);
    }
    
    if(il.size < expr.length - 1) {
      FTExpr[] e = new FTExpr[il.size + 1];
      e[0] = expr[0];
      int c = 1;
      for (int i = 0; i < il.size; i++) e[c++] = expr[il.list[i]];
      expr = e;
    }
    final boolean ia = expr[0].indexAccessible(ic);
    ic.is = mmin < ic.is ? mmin : ic.is;
    return ia;
  }
  
  @Override
  public FTExpr indexEquivalent(final IndexContext ic) throws QueryException {
    if(expr.length == 1) return expr[0].indexEquivalent(ic);
    
    // assumption 1: ftcontains "a" not in "a b" not in "a c"
    // and ftcontains "a" not in "a b" ftand "a" not in "a c" are equivalent

    final FTExpr[] ie = new FTExpr[2];
    final FTMildNotIndex[] mne = new FTMildNotIndex[expr.length - 1];
    final int[] pex = new int[expr.length - 1];
    ie[0] = expr[0].indexEquivalent(ic);
    for(int i = 1; i < expr.length; i++) {
      ie[1] = expr[i].indexEquivalent(ic);
      mne[i - 1] = new FTMildNotIndex(ie);
      pex[i - 1] = i - 1;
    }
    if(mne.length == 1) {
      return mne[0];
    }
    return new FTIntersection(pex, new int[] {}, mne);
  }
}
