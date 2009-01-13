package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.Array;

/**
 * And expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class And extends Arr {
  /**
   * Constructor.
   * @param e expression list
   */
  public And(final Expr[] e) {
    super(e);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    for(int e = 0; e < expr.length; e++) {
      if(!expr[e].i()) continue;
      
      if(!((Item) expr[e]).bool()) {
        // atomic items can be pre-evaluated
        ctx.compInfo(OPTFALSE, expr[e]);
        return Bln.FALSE;
      }
      ctx.compInfo(OPTTRUE, expr[e]);
      expr = Array.delete(expr, e--);
      if(expr.length == 0) return Bln.TRUE;
    }
    
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).ebv();
      if(!it.bool()) {
        if(ctx.ftdata != null) ctx.ftdata.remove(((DBNode) ctx.item).pre + 1);
        return Bln.FALSE.iter();
      }
      d = Scoring.and(d, it.score());
    }
    
    if (!Bln.get(d).bool() && ctx.item instanceof DBNode && ctx.ftdata != null) 
      ctx.ftdata.remove(((DBNode) ctx.item).pre + 1);
    
    return (d == 0 ? Bln.TRUE : Bln.get(d)).iter();
  }
  
  @Override
  public Type returned() {
    return Type.BLN;
  }

  @Override
  public void indexAccessible(final XQContext ctx, final IndexContext ic)
      throws XQException {
    
    for(final Expr e : expr) {
      e.indexAccessible(ctx, ic);
      if(!ic.iu || ic.is == 0) return;
    }
  }

  @Override
  public Expr indexEquivalent(final XQContext ctx, final IndexContext ic)
      throws XQException {

    for(int e = 0; e < expr.length; e++) {
      expr[e] = expr[e].indexEquivalent(ctx, ic);
    }
    return new InterSect(expr);
  }

  @Override
  public String toString() {
    return toString(" and ");
  }
}
