package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;

import org.basex.query.xquery.IndexContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.Array;

/**
 * Or expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Or extends Arr {
  /**
   * Constructor.
   * @param e expression list
   */
  public Or(final Expr[] e) {
    super(e);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    for(int e = 0; e < expr.length; e++) {
      if(!expr[e].i()) continue;

      if(((Item) expr[e]).bool()) {
        // atomic items can be pre-evaluated
        ctx.compInfo(OPTTRUE, expr[e]);
        return Bln.TRUE;
      }
      ctx.compInfo(OPTFALSE, expr[e]);
      expr = Array.delete(expr, e--);
      if(expr.length == 0) return Bln.FALSE;
    }
    return cmpG(ctx);
  }
  
  /**
   * If possible, converts the expressions to a comparison operator.
   * @param ctx query context
   * @return expression
   */
  private Expr cmpG(final XQContext ctx) {
    if(!(expr[0] instanceof CmpG)) return this;

    final CmpG e1 = (CmpG) expr[0];
    if(!e1.standard()) return this;

    final SeqIter ir = new SeqIter();
    ir.add((Item) (e1.expr[1]));
    
    for(int e = 1; e != expr.length; e++) {
      if(!(expr[e] instanceof CmpG)) return this;
      final CmpG e2 = (CmpG) expr[e];
      if(!e2.standard() || e1.cmp != e2.cmp ||
         !e1.expr[0].sameAs(e2.expr[0])) return this;
      ir.add((Item) (e2.expr[1]));
    }
    
    ctx.compInfo(OPTRED);
    return new CmpG(e1.expr[0], ir.finish(), e1.cmp);
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    double d = 0;
    boolean found = false;
    for(final Expr e : expr) {
      final Item it = ctx.iter(e).ebv();
      if(it.bool()) {
        final double s = it.score();
        if(s == 0) return Bln.TRUE.iter();
        d = Scoring.or(d, s);
        found = true;
      }
    }
    return (d == 0 ? Bln.get(found) : Bln.get(d)).iter();
  }

  @Override
  public void indexAccessible(final XQContext ctx, final IndexContext ic)
      throws XQException {
    
    for(final Expr e : expr) {
      e.indexAccessible(ctx, ic);
      if(!ic.iu) return;
    }
  }

  @Override
  public Expr indexEquivalent(final XQContext ctx, final IndexContext ic)
      throws XQException {

    for(int e = 0; e < expr.length; e++) {
      expr[e] = expr[e].indexEquivalent(ctx, ic);
    }
    return new Union(expr);
  }
  
  @Override
  public Type returned() {
    return Type.BLN;
  }

  @Override
  public String toString() {
    return toString(" or ");
  }
}
