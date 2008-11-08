package org.basex.query.xpath.expr;

import org.basex.index.FTNode;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Bln;
import org.basex.util.IntList;

/**
 * FTMildNotExprs. This expresses the mild combination of ftand and ftnot.
 * The selection A not in B matches a token sequence that matches a, but
 * not when it is part of b.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTMildNotExprs extends FTArrayExpr {
  /**
   * Constructor.
   * @param e operands joined with the mild not operator
   */
  public FTMildNotExprs(final FTArrayExpr[] e) {
    exprs = e;
  }

  @Override
  public boolean more() {
    return exprs[0].more();
  }

  /** Result node from expression 1. */
  FTNode n1 = null;
  
  @Override
  public FTNode next(final XPContext ctx) {
    final FTNode n0 = exprs[0].next(ctx);
    if (n1 == null) {
      if (exprs[1].more()) n1 = exprs[1].next(ctx);
      else return n0;
    } 
    
    final IntList pos = new IntList();
    pos.add(n0.getPre());
    final IntList poi = new IntList();
    poi.add(n0.getNumTokens());
    
    if (n0.getPre() < n1.getPre()) {
      return n0;
    } else if (n0.getPre() > n1.getPre()) {
      n1 = null;
      if (more()) return next(ctx);
      else return n0;
    } else {
      boolean mp0 = n0.morePos();
      boolean mp1 = n1.morePos();
      while(mp0 && mp1) {
        if (n0.nextPos() < n1.nextPos()) {
          pos.add(n0.nextPos());
          poi.add(n0.nextPoi());
          mp0 = n0.morePos();
        } else if (n0.nextPos() > n1.nextPos()) {
          mp1 = n1.morePos();
        } else {
          mp0 = n0.morePos();
          mp1 = n1.morePos();
        }
      }
    }
    if (pos.size > 1) {
      return new FTNode(pos.finish(), poi.finish());
    } else {
      n1 = null;
      if (more()) return next(ctx);
      else return new FTNode();
    }
  }
  
  @Override
  public Bln eval(final XPContext ctx) throws QueryException {
    ctx.ftpos.st = true;
    final Bln b0 = (Bln) exprs[0].eval(ctx);
    exprs[1].eval(ctx);
    return b0;
  }  
}
