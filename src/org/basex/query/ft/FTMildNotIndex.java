package org.basex.query.ft;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNode;
import org.basex.query.iter.FTNodeIter;
import org.basex.util.IntList;

/**
 * FTMildnotIndex expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTMildNotIndex extends FTExpr {
  /**
   * Constructor.
   * @param l expression list
   */
  public FTMildNotIndex(final FTExpr... l) {
    super(l);
  }
  
  @Override
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    return new FTNodeIter() {
      final FTNodeIter i1 = expr[0].iter(ctx);
      final FTNodeIter i2 = expr[1].iter(ctx);
      FTNode n0, n1;
      
      @Override
      public FTNode next() throws QueryException { 
        if(n0 == null) n0 = i1.next();
        if(n1 == null) n1 = i2.next();
        if(n0.empty() || n1.empty()) {
          final FTNode tmp = n0;
          n0 = null;
          return tmp;
        } 
        
        final IntList pos = new IntList();
        pos.add(n0.fte.pre());
        final IntList poi = new IntList();
        poi.add(n0.fte.getNumTokens());
        
        int d = n0.fte.pre() - n1.fte.pre();
        if(d < 0) {
          final FTNode tmp = n0;
          n0 = null;
          return tmp;
        }
        if(d > 0) {
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

        if(pos.size > 1) {
          final FTNode tmp = n0;
          tmp.fte.pos = pos;
          tmp.fte.poi = poi;
          n0 = null;
          n1 = null;
          return tmp;
        }
        n1 = null;
        n0 = null;
        return next();
      }
    };
  }

  @Override
  public String toString() {
    return toString(" not inIndex ");
  }
}
