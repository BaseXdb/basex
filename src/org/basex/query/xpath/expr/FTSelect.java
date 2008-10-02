package org.basex.query.xpath.expr;

import org.basex.data.MetaData;
import org.basex.index.FTNode;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Item;


/**
 * Fulltext position filter expression and FTTimes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTSelect extends FTArrayExpr {
  /**
   * Constructor.
   * @param e expressions
   * @param ftps FTPositionFilter
   */
  public FTSelect(final FTArrayExpr e, final FTPositionFilter ftps) {
    exprs = new FTArrayExpr[] { e };
    ftpos = ftps;   
  }

  @Override 
  public boolean pos() {
    return exprs[0].pos();
  }
  
  @Override
  public FTNode next(final XPContext ctx) {
    FTNode ftn = exprs[0].next(ctx);
    final FTPositionFilter tmp = ctx.ftpos;
    ctx.ftpos = ftpos;
    
    ftpos.pos.init(ctx.ftitem);
    if (ftn.size > 0) {
      init(ftn, ctx);
      while (!seqEval()) {
        if (more()) {
          ftn = exprs[0].next(ctx);
          init(ftn, ctx);
        } else {
          ctx.ftpos = tmp;
          return new FTNode();
        }
      } 
      ctx.ftpos = tmp;
      return ftn;
      //ftn = indexEval(ctx, ftn);
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
    ftpos.pos.setPos(ftn.convertPos(), ftn.p.get(0));
    if (ftn.getToken() != null) {
      // Diskaccess could be optimized??? 
      ftpos.pos.ft.init(ctx.local.data.text(ftn.getPre()));
      ftpos.pos.term = ftpos.pos.ft.getTokenList();
    }
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
    final Item i = exprs[0].eval(ctx);
    ctx.ftpos = tmp;
    if (ctx.iu) return i; 
    return Bool.get(i.bool() && seqEval());
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
    
    /*
    // to be checked... will probably disappear as soon as pre values are 
    // processed one by one
    if(e instanceof FTIntersection) {
      ((FTIntersection) e).pres = true;
    } else if(e instanceof FTUnion) {
      ((FTUnion) e).po = true;
    } else if(e instanceof FTUnaryNotExprs || e instanceof FTMildNotExprs) {
      final FTArrayExpr not = e;
      final FTArrayExpr ex = not.exprs[0];
      final FTIntersection fti1 = new FTIntersection(not.exprs, true);
      final FTPositionFilter ftposfil = ftpos.clone();

      final FTSelect ftps = new FTSelect(fti1, ftposfil);
      not.exprs = new FTArrayExpr[] { ex, ftps };
      return not;
    } else if(e instanceof FTIndex
        && (ftpos.pos.start || ftpos.pos.end || ftpos.pos.content)) {
      ((FTIndex) e).setFTPosFilter(ftpos);
    } */
    return this;
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
