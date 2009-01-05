package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;

import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.path.AxisPath;
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
    for(int e = 0; e < expr.length; e++) {
      Expr ex = ctx.comp(expr[e]);
      if(ex instanceof AxisPath) {
        final AxisPath ap = ((AxisPath) ex).addPos();
        if(ap != null) {
          ex = ap;
          ctx.compInfo(OPTPOS);
        }
      }
      expr[e] = ex;
      if(!ex.i()) continue;
      
      if(!((Item) ex).bool()) {
        // atomic items can be pre-evaluated
        ctx.compInfo(OPTFALSE, ex);
        return Bln.FALSE;
      }
      ctx.compInfo(OPTTRUE, ex);
      expr = Array.delete(expr, e--);
    }
    return expr.length == 0 ? Bln.TRUE : this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).ebv();
      if(!it.bool()) return Bln.FALSE.iter();
      d = Scoring.and(d, it.score());
    }
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
