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
  /** Result node from expression 0. */
  FTNodeItem n0;
  /** Result node from expression 1. */
  FTNodeItem n1;

  /**
   * Constructor.
   * @param l expression list
   */
  public FTMildNotIndex(final FTExpr... l) {
    super(l);
  }
  
  @Override
  public FTNodeIter iter(final QueryContext ctx) {
    return new FTNodeIter(){
      @Override
      public FTNodeItem next() throws QueryException { 
        n0 = (n0 == null) ? expr[0].iter(ctx).next() : n0;
        n1 = (n1 == null) ? expr[1].iter(ctx).next() : n1;
        if (n1.ftn.size == 0 || n0.ftn.size == 0) {
          final FTNodeItem tmp = n0;
          n0 = null;
          return tmp;
        } 
        
        final IntList pos = new IntList();
        pos.add(n0.ftn.getPre());
        final IntList poi = new IntList();
        poi.add(n0.ftn.getNumTokens());
        
        if (n0.ftn.getPre() < n1.ftn.getPre()) {
          final FTNodeItem tmp = n0;
          n0 = null;
          return tmp;
        } else if (n0.ftn.getPre() > n1.ftn.getPre()) {
          n1 = null;
          //if (more(ctx)) 
            return next();
          //else return n0;
        } else {
          boolean mp0 = n0.ftn.morePos();
          boolean mp1 = n1.ftn.morePos();
          while(mp0 && mp1) {
            if (n0.ftn.nextPos() < n1.ftn.nextPos()) {
              pos.add(n0.ftn.nextPos());
              poi.add(n0.ftn.nextPoi());
              mp0 = n0.ftn.morePos();
            } else if (n0.ftn.nextPos() > n1.ftn.nextPos()) {
              mp1 = n1.ftn.morePos();
            } else {
              mp0 = n0.ftn.morePos();
              mp1 = n1.ftn.morePos();
            }
          }
        }
        if (pos.size > 1) {
          final FTNode n = new FTNode(pos.finish(), poi.finish());
          n.setToken(n0.ftn.getToken());
          final FTNodeItem tmp = new FTNodeItem(n, n0.data);
          n0 = null;
          n1 = null;
          return tmp;
        } else {
          n1 = null;
          n0 = null;
          //if (more(ctx)) 
          return next();
          //else return new FTNodeItem();
        }
      }
    };
  }

  @Override
  public String toString() {
    return toString(" not inIndex ");
  }
}
