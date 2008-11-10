package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.internal.OneOf;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.locpath.ExprInfoList;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.Step;

import static org.basex.query.xpath.XPText.*;

/**
 * Or expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Or extends Arr {
  /**
   * Constructor.
   * @param e expressions
   */
  public Or(final Expr[] e) {
    super(e);
  }

  @Override
  public Bln eval(final XPContext ctx) throws QueryException {
    ctx.iu = false;
    
    for(final Expr e : expr) 
      if(e.eval(ctx).bool()) 
        return Bln.TRUE;
    return Bln.FALSE;
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);

    for(int i = 0; i != expr.length; i++) {
      // check if we can simplify a location path
      if(expr[i] instanceof LocPath) {
        ((LocPath) expr[i]).addPosPred(ctx);
      } else if(expr[i] instanceof Item) {
        // simplify operand
        if(((Item) expr[i]).bool()) {
          // this expression is always true
          ctx.compInfo(OPTOR1);
          return Bln.TRUE;
        }

        // remove element from or expression that is always false
        ctx.compInfo(OPTOR2);
        final Expr[] tmp = new Expr[expr.length - 1];
        System.arraycopy(expr, 0, tmp, 0, i);
        System.arraycopy(expr, i + 1, tmp, i, expr.length - i-- - 1);
        expr = tmp;
      } else if (expr[i] instanceof And) {
        // sum up and predicates
        final ExprInfoList eil = new ExprInfoList();
        final And o = (And) expr[i];
        for (int j = 0; j < o.expr.length; j++)
          eil.add(o.expr[j], true);

        if (eil.size > 0 && eil.size < o.expr.length) {
          Expr[] e = eil.finishE();
          if (e.length == 1) expr[i] = e[0];
          o.expr = eil.finishE();
         ctx.compInfo(OPTSUMPREDS);
       }
     }
    }
    if(expr.length == 0) return Bln.FALSE;
    if(expr.length == 1) return expr[0];
    
    return oneOf(ctx);
  }
  
  /**
   * Returns an oneOf()-Function of the class instance if no other
   * optimizations were applied.
   * @param ctx query context
   * @return expression
   */
  private Expr oneOf(final XPContext ctx) {
    if(!(expr[0] instanceof Cmp)) return this;

    final Cmp e1 = (Cmp) expr[0];
    if(!e1.standard()) return this;

    for(int e = 1; e != expr.length; e++) {
      if(!(expr[e] instanceof Cmp)) return this;
      final Cmp e2 = (Cmp) expr[e];
      if(!e2.standard() || e1.type != e2.type ||
          !e1.expr[0].sameAs(e2.expr[0])) return this;
    }
    
    final Item[] ex = new Item[expr.length];
    for(int e = 0; e != expr.length; e++) {
      ex[e] = (Item) ((Cmp) expr[e]).expr[1];
    }

    ctx.compInfo(OPTOR5);
    return new OneOf((LocPath) e1.expr[0], ex, e1.type);
  }
  
  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq)
      throws QueryException {

    final Expr[] indexExprs = new Expr[expr.length];
    
    // find index equivalents
    for(int i = 0; i != expr.length; i++) {
      indexExprs[i] = expr[i].indexEquivalent(ctx, curr, seq);
      if(indexExprs[i] == null) return null;
    }

    // perform path step only once if all path expressions are the same
    final Expr[] ex = XPOptimizer.getIndexExpr(indexExprs);
    if(ex != null) return new Path(new Union(ex),
        ((Path) indexExprs[0]).expr[1]);

    ctx.compInfo(OPTOR4);
    return new Union(indexExprs);
  }
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    int sum = 0;
    for(final Expr e : expr) {
      final int nrIDs = e.indexSizes(ctx, curr, min);
      if(nrIDs == Integer.MAX_VALUE) return nrIDs;
      sum += nrIDs;
      if(sum > min) return min;
    }
    return sum > min ? min : sum;
  }

  @Override
  public String toString() {
    return toString(" or ");
  }
}
