package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;

import org.basex.data.FTMatches;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
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
      if(!all.mildnot(expr[e].atomic(ctx).all)) break;
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
  public FTIter iter(final QueryContext ctx) {
    return new FTIter() {
      //final FTIter i1 = expr[0].iter(ctx);
      //final FTIter i2 = expr[1].iter(ctx);
      //FTItem n0, n1;
      
      @Override
      public FTItem next() { 
        return null;
        /*
        if(n0 == null) n0 = i1.next();
        if(n1 == null) n1 = i2.next();
        if(n0.empty() || n1.empty()) {
          final FTItem tmp = n0;
          n0 = null;
          return tmp;
        } 
        
        final IntList pos = new IntList();
        final TokenBuilder poi = new TokenBuilder();
        
        int d = n0.fte.pre - n1.fte.pre;
        if(d < 0) {
          final FTItem tmp = n0;
          n0 = null;
          return tmp;
        }
//        if(d > 0) {
          n1 = null;
          return next();
        }

        boolean mp0 = n0.fte.morePos();
        boolean mp1 = n1.fte.morePos();
        while(mp0 && mp1) {
          d = n0.fte.nextPos() - n1.fte.nextPos();
          if(d <= 0) {
            if(d < 0) {
              pos.add(n0.fte.nextPos());
              poi.add(n0.fte.nextPoi());
            }
            mp0 = n0.fte.morePos();
          }
          if(d >= 0) {
            mp1 = n1.fte.morePos();
          }
        }

        if(pos.size > 0) {
          final FTItem tmp = n0;
          tmp.fte.pos = pos;
          tmp.fte.poi = poi;
          n0 = null;
          n1 = null;
          return tmp;
        }
        n1 = null;
        n0 = null;
        return next();
        */
      }
    };
  }
  
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
    final FTMildNot[] mne = new FTMildNot[expr.length - 1];
    final int[] pex = new int[expr.length - 1];
    ie[0] = expr[0].indexEquivalent(ic);
    for(int i = 1; i < expr.length; i++) {
      ie[1] = expr[i].indexEquivalent(ic);
      mne[i - 1] = new FTMildNot(ie);
      pex[i - 1] = i - 1;
    }
    if(mne.length == 1) {
      return mne[0];
    }
    return new FTIntersection(pex, new int[] {}, mne);
  }
}
