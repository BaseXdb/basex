package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.iter.SeqIter;
import org.basex.util.Array;

/**
 * Or expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  public Expr comp(final QueryContext ctx) throws QueryException {
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
    
    if(expr.length == 2 && expr[0] instanceof Pos && expr[1] instanceof Pos) {
      return ((Pos) expr[0]).union((Pos) expr[1]);
    }
    return cmpG(ctx);
  }
  
  /**
   * If possible, converts the expressions to a comparison operator.
   * @param ctx query context
   * @return expression
   */
  private Expr cmpG(final QueryContext ctx) {
    if(!(expr[0] instanceof CmpG)) return this;

    final CmpG e1 = (CmpG) expr[0];
    if(!e1.standard(false)) return this;

    final SeqIter ir = new SeqIter();
    ir.add((Item) (e1.expr[1]));
    
    for(int e = 1; e != expr.length; e++) {
      if(!(expr[e] instanceof CmpG)) return this;
      final CmpG e2 = (CmpG) expr[e];
      if(!e2.standard(false) || e1.cmp != e2.cmp ||
         !e1.expr[0].sameAs(e2.expr[0])) return this;
      ir.add((Item) (e2.expr[1]));
    }
    
    ctx.compInfo(OPTRED);
    return new CmpG(e1.expr[0], ir.finish(), e1.cmp);
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    double d = 0;
    boolean f = false;
    for(final Expr e : expr) {
      final Item it = e.ebv(ctx);
      if(it.bool()) {
        final double s = it.score();
        if(s == 0) return Bln.TRUE;
        d = ctx.score.or(d, s);
        f = true;
      }
    }
    return d == 0 ? Bln.get(f) : Bln.get(d);
  }
  
  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    for(final Expr e : expr) if(!e.indexAccessible(ic)) return false;
    return true;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) throws QueryException {
    super.indexEquivalent(ic);
    return new Union(expr);
  }
  
  @Override
  public Return returned(final QueryContext ctx) {
    return Return.BLN;
  }

  @Override
  public String toString() {
    return toString(" " + OR + " ");
  }
}
