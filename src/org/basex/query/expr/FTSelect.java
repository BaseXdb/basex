package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.FTPos;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNodeItem;
import org.basex.query.item.Item;
import org.basex.query.iter.FTNodeIter;
import org.basex.query.util.Err;
import org.basex.util.IntList;

/**
 * FTSelect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTSelect extends FTExpr {
  /** Position filter. */
  public FTPos pos;
  /** Window. */
  public Expr window;
  /** Distance occurrences. */
  public Expr[] dist;
  /** Weight. */
  public Expr weight;

  /**
   * Constructor.
   * @param e expression
   * @param u fulltext selections
   */
  public FTSelect(final FTExpr e, final FTPos u) {
    super(e);
    pos = u;
  }
  
  /**
   * Check if FTSelect is initial.
   * There are no further options set, beside the default ones.
   * Used for FTNot expression.
   * 
   * @return boolean initial
   */
  public boolean initial() {
    return window == null && dist == null && pos.valid();
  }

  @Override
  public FTNodeIter iter(final QueryContext ctx) throws QueryException {
    FTPos tmp = ctx.ftpos;
    ctx.ftpos = pos;
    pos.init(ctx.ftitem);

    final FTNodeItem it = expr[0].iter(ctx).next();

    if(tmp != null) {
      final int os = tmp.term.size;
      for (int i = 0; i < pos.term.size; i++) {
        tmp.term.add(pos.term.list[i]);
      }
      final IntList[] il = new IntList[pos.term.size + os];
      System.arraycopy(pos.getPos(), 0, il, 0, pos.term.size);
      System.arraycopy(tmp.getPos(), 0, il, pos.term.size, os);
      tmp.setPos(il, il.length);
      ctx.ftd = il;
    } else ctx.ftd = pos.getPos();
     
    
    ctx.ftpos = tmp;
    final double s = it.score();
    if(s == 0 || !posFilter(ctx)) return score(0);

    // calculate weight
    final double d = checkDbl(ctx.iter(weight));
    if(d < 0 || d > 1000) Err.or(FTWEIGHT, d);
    //return score(d != 1 ? Dbl.get(s * d) : it);
    return score(s * d);
  }

  /**
   * Evaluates the position filters.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean posFilter(final QueryContext ctx) throws QueryException {
    if(!pos.valid()) return false;

    // ...distance?
    if(pos.dunit != null) {
      final long mn = checkItr(ctx.iter(dist[0]));
      final long mx = checkItr(ctx.iter(dist[1]));
      if(!pos.distance(mn, mx)) return false;
    }
    // ...window?
    if(pos.wunit != null) {
      final long c = checkItr(ctx.iter(window));
      if(!pos.window(c)) return false;
    }
    return true;
  }
  
  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    weight = weight.comp(ctx);
    if(weight.i()) {
      final Item wg = (Item) weight;
      if(!wg.n()) Err.or(XPTYPENUM, WEIGHT, weight);
      if(wg.dbl() < 0 || wg.dbl() > 1000) Err.or(FTWEIGHT, wg);
    }
    return super.comp(ctx);
  }

  @Override
  public void indexAccessible(final QueryContext ctx, final IndexContext ic) 
      throws QueryException {
    
    expr[0].indexAccessible(ctx, ic);
    
    // index could only be used, if there is no ftselection specified 
    // before an ftnot
    if (ic.ftnot) ic.iu &= initial();
  }
  
  @Override
  public FTExpr indexEquivalent(final QueryContext ctx, final IndexContext ic)
    throws QueryException {

    return new FTSelectIndex(expr[0].indexEquivalent(ctx, ic),
        pos, window, weight, dist);
  }
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    pos.plan(ser);
    expr[0].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(expr[0]);
    sb.append(pos);
    if(pos.dunit != null) {
      sb.append(" distance(");
      sb.append(dist[0]);
      sb.append(",");
      sb.append(dist[1]);
      sb.append(")");
    }
    return sb.toString();
  }
}
