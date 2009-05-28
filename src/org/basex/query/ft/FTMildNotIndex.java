package org.basex.query.ft;

import org.basex.index.FTNode;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
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
      FTNodeItem n0, n1;
      
      @Override
      public FTNodeItem next() throws QueryException { 
        if(n0 == null) n0 = i1.next();
        if(n1 == null) n1 = i2.next();
        if(n1.ftn.empty() || n0.ftn.empty()) {
          final FTNodeItem tmp = n0;
          n0 = null;
          return tmp;
        } 
        
        final IntList pos = new IntList();
        pos.add(n0.ftn.pre());
        final IntList poi = new IntList();
        poi.add(n0.ftn.getNumTokens());
        
        if(n0.ftn.pre() < n1.ftn.pre()) {
          final FTNodeItem tmp = n0;
          n0 = null;
          return tmp;
        }
        if(n0.ftn.pre() > n1.ftn.pre()) {
          n1 = null;
          return next();
        }

        boolean mp0 = n0.ftn.morePos();
        boolean mp1 = n1.ftn.morePos();
        while(mp0 && mp1) {
          if(n0.ftn.nextPos() < n1.ftn.nextPos()) {
            pos.add(n0.ftn.nextPos());
            poi.add(n0.ftn.nextPoi());
            mp0 = n0.ftn.morePos();
          } else if(n0.ftn.nextPos() > n1.ftn.nextPos()) {
            mp1 = n1.ftn.morePos();
          } else {
            mp0 = n0.ftn.morePos();
            mp1 = n1.ftn.morePos();
          }
        }
        if(pos.size > 1) {
          final FTNode n = new FTNode(pos.finish(), poi.finish());
          n.setToken(n0.ftn.getToken());
          final FTNodeItem tmp = new FTNodeItem(n, n0.data);
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
