package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.internal.OneOf;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Item;
import static org.basex.query.xpath.XPText.*;

/**
 * Logical OR expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Or extends ArrayExpr {
  /**
   * Constructor.
   * @param e expressions
   */
  public Or(final Expr[] e) {
    exprs = e;
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    for(final Expr e : exprs) if(e.eval(ctx).bool()) return Bool.TRUE;
    return Bool.FALSE;
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    for(int i = 0; i != exprs.length; i++) {
      // optimize operand
      exprs[i] = exprs[i].compile(ctx);

      // check if we can simplify a location path
      if(exprs[i] instanceof LocPath) {
        ((LocPath) exprs[i]).addPosPred(ctx);
      } else if(exprs[i] instanceof Item) {
        // simplify operand
        if(((Item) exprs[i]).bool()) {
          // this expression is always true
          ctx.compInfo(OPTOR1);
          return Bool.TRUE;
        }

        // remove element from or expression that is always false
        ctx.compInfo(OPTOR2);
        final Expr[] tmp = new Expr[exprs.length - 1];
        System.arraycopy(exprs, 0, tmp, 0, i);
        System.arraycopy(exprs, i + 1, tmp, i, exprs.length - i-- - 1);
        exprs = tmp;
      }
    }
    if(exprs.length == 0) return Bool.FALSE;
    if(exprs.length == 1) return exprs[0];
    return oneOf(ctx);
  }
  
  /**
   * Returns an oneOf()-Function of the class instance if no other
   * optimizations were applied.
   * @param ctx query context
   * @return expression
   */
  private Expr oneOf(final XPContext ctx) {
    if(!(exprs[0] instanceof Comparison)) return this;

    final Comparison e1 = (Comparison) exprs[0];
    if(!e1.simple()) return this;

    for(int e = 1; e != exprs.length; e++) {
      if(!(exprs[e] instanceof Comparison)) return this;
      final Comparison e2 = (Comparison) exprs[e];
      if(!e2.simple() || e1.type != e2.type ||
          !e1.expr1.sameAs(e2.expr1)) return this;
    }
    
    final Item[] ex = new Item[exprs.length];
    for(int e = 0; e != exprs.length; e++) {
      ex[e] = (Item) ((Comparison) exprs[e]).expr2;
    }

    ctx.compInfo(OPTOR5);
    return new OneOf((LocPath) e1.expr1, ex, e1.type);
  }
  
  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr)
      throws QueryException {

    final Expr[] indexExprs = new Expr[exprs.length];
    
    // find index equivalents
    for(int i = 0; i != exprs.length; i++) {
      indexExprs[i] = exprs[i].indexEquivalent(ctx, curr);
      if(indexExprs[i] == null) return null;
    }

    // perform path step only once if all path expressions are the same
    final Expr[] ex = XPOptimizer.getIndexExpr(indexExprs);
    if(ex != null) return new Path(new Union(ex),
        ((Path) indexExprs[0]).expr2);

    ctx.compInfo(OPTOR4);
    return new Union(indexExprs);
  }
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    int sum = 0;
    for(final Expr expr : exprs) {
      final int nrIDs = expr.indexSizes(ctx, curr, min);
      if(nrIDs == Integer.MAX_VALUE) return nrIDs;
      sum += nrIDs;
      if(sum > min) return min;
    }
    return sum > min ? min : sum;
  }
}
