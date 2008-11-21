package org.basex.query.xpath.expr;

import org.basex.data.MetaData;
import org.basex.index.FTNode;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.locpath.Step;


/**
 * Fulltext position filter expression and FTTimes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTSelect extends FTArrayExpr {
  /** FTPositionFilter. */
  public FTPositionFilter ftpos;
  /**
   * Constructor.
   * @param e expressions
   * @param ftps FTPositionFilter
   */
  public FTSelect(final FTArrayExpr e, final FTPositionFilter ftps) {
    exprs = new FTArrayExpr[] { e };
    ftpos = ftps;   
    // ftopt???
  }
  
  @Override
  public FTArrayExpr comp(final XPContext ctx) throws QueryException {
    for(int i = 0; i != exprs.length; i++) {
      if(exprs[i].fto == null) exprs[i].fto = fto;
      else if (fto != null) exprs[i].fto.merge(fto);
      exprs[i] = exprs[i].comp(ctx);
    }
    return this;
   }

  @Override 
  public boolean pos() {
    return exprs[0].pos();
  }
  
  @Override
  public FTNode next(final XPContext ctx) {
    final FTPositionFilter tmp = ctx.ftpos;
    ctx.ftpos = ftpos;
    FTNode ftn = exprs[0].next(ctx);
    
    ftpos.pos.init(ctx.ftitem);
    if (ftn.size > 0) {
      init(ftn, ctx);
      while (!seqEval()) {
        if (more()) {
          ftn = exprs[0].next(ctx);
          if(ftn.size == 0) return new FTNode();
          init(ftn, ctx);
        } else {
          ctx.ftpos = tmp;
          return new FTNode();
        }
      } 
      ctx.ftpos = tmp;
      return ftn;
    }
      
    ctx.ftpos = tmp;    
    return new FTNode();
  }
  
  /**
   * Init FTPos for next seqEval with index use.
   * @param ftn current FTNode 
   * @param ctx current XPContext
   */
  private void init(final FTNode ftn, final XPContext ctx) {
    ftpos.pos.setPos(ftn.convertPos(), ftn.p.list[0]);
    if (ftn.getToken() != null) {
        ftpos.pos.ft.init(ctx.item.data.text(ftn.getPre()));
        ftpos.pos.term = ftpos.pos.ft.getTokenList();       
    }
  }
  
  /**
   * Getter for expr.
   * @return expr FTArrayExpr
   */
  public FTArrayExpr getExpr() {
    return exprs[0];
  }
  
  
  /**
   * Setter expr.
   * @param e FTArrayExpr
   */
  public void setExpr(final FTArrayExpr e) {
    exprs[0] = e;
  }

  @Override
  public boolean more() {
    return exprs[0].more();
  }
  
  @Override
  public Item eval(final XPContext ctx) throws QueryException {
    final FTPositionFilter tmp = ctx.ftpos;
    ctx.ftpos = ftpos;
    ctx.ftpos.st = ctx.ftpos.pos.content || ctx.ftpos.pos.different 
      || ctx.ftpos.pos.end || ctx.ftpos.pos.ordered || ctx.ftpos.pos.same 
      || ctx.ftpos.pos.start || ftpos.pos.dunit != null 
      || ftpos.pos.wunit != null;
    ftpos.pos.init(ctx.ftitem);
    Item i = exprs[0].eval(ctx);
    ftpos.pos.setPos(ctx.ftpos.pos.getPos(), ctx.ftpos.pos.getPos().length);
    if (!ctx.iu) i = Bln.get(i.bool() && seqEval());
    ctx.ftpos = tmp;
    if (ctx.ftpos != null) 
      ctx.ftpos.pos.setPos(ftpos.pos.getPos(), ftpos.pos.getPos().length);
    return i;
  }

  /**
   * Evaluates the position filters.
   * @return result of check
   */
  private boolean seqEval() {
    if(!ftpos.pos.valid()) return false;
    
    // ...distance?
    if(ftpos.pos.dunit != null) {
      if(!ftpos.pos.distance(ftpos.dist[0], ftpos.dist[1])) return false;
    }
    // ...window?
    if(ftpos.pos.wunit != null) {
      final long c = (long) ftpos.window.num();
      if(!ftpos.pos.window(c)) return false;
    }
  
    
    return true;
  }

  @Override
  public boolean indexOptions(final MetaData meta) {
    // if the following conditions yield true, the index is accessed:
    // - no ftcontent option was specified
    // return !ftpos.pos.start && !ftpos.pos.end && !ftpos.pos.content &&
    return super.indexOptions(meta);
  }

  @Override
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq)
      throws QueryException {

    exprs[0] = exprs[0].indexEquivalent(ctx, curr, seq);
    return this;
  }

  
  /**
   * Checks if FTContains Expr could be summed up.
   * 
   * @param ftpos1 FTPositionFilter second expr
   * @return int 
   */
  
  public boolean checkSumUp(final FTPositionFilter ftpos1) {
    return !ftpos.pos.ordered == !ftpos1.pos.ordered 
        && ftpos.window == null && ftpos1.window == null
        && ftpos.dist == null && ftpos.dist == null
        && ftpos.pos.sdunit == null && ftpos1.pos.sdunit == null
        && ftpos.pos.content == ftpos1.pos.content
        && ftpos.pos.start == ftpos1.pos.start
        && ftpos.pos.end == ftpos1.pos.end;
  }
  
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    
    final FTPositionFilter tmp = ctx.ftpos;
    ctx.ftpos = ftpos;   
    final int n = exprs[0].indexSizes(ctx, curr, min);
    ctx.ftpos = tmp;

    if (exprs[0] instanceof FTUnaryNot && n == 0) {
      // query looks like ftc ftnot "a" and "a" is not contained
      return Integer.MAX_VALUE;
    }
    return n;
  }
}
