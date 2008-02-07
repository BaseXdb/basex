package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.internal.AllOf;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Comp;
import org.basex.query.xpath.values.Item;
import static org.basex.query.xpath.XPText.*;

/**
 * Logical AND Expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class And extends ArrayExpr {
  /**
   * Constructor.
   * @param e expressions
   */
  public And(final Expr[] e) {
    exprs = e;
  }

  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    for(final Expr e : exprs) if(!ctx.eval(e).bool()) return Bool.FALSE;
    return Bool.TRUE;
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    for(int e = 0; e != exprs.length; e++) {
      exprs[e] = exprs[e].compile(ctx);
      
      // check if we can add a position predicate to match only the first title
      if(exprs[e] instanceof LocPath) {
        ((LocPath) exprs[e]).addPosPred(ctx);
      } else if(exprs[e] instanceof Item) {
        // simplify operand
        if(!((Item) exprs[e]).bool()) {
          // this expression will always be false; remove expression
          ctx.compInfo(OPTAND1);
          return Bool.FALSE;
        }

        // remove element from and expression that is always true
        ctx.compInfo(OPTAND2);
        final Expr[] tmp = new Expr[exprs.length - 1];
        System.arraycopy(exprs, 0, tmp, 0, e);
        System.arraycopy(exprs, e + 1, tmp, e, exprs.length - e-- - 1);
        exprs = tmp;
      }
    }
    if(exprs.length == 0) return Bool.TRUE;
    if(exprs.length == 1) return exprs[0];
    
    // optimization to speedup range queries (not that elegant yet)
    if(exprs.length == 2 && exprs[0] instanceof Comparison &&
        exprs[1] instanceof Comparison) {
      final Comparison r1 = (Comparison) exprs[0];
      final Comparison r2 = (Comparison) exprs[1];
      if(!r1.simple() || !r2.simple()) return this;
      final LocPath p1 = (LocPath) r1.expr1;
      final LocPath p2 = (LocPath) r2.expr1;
      
     /* if(r1.type == Comp.GE && r2.type == Comp.LE && p1.sameAs(p2)) {
        ctx.compInfo(OPTRANGE);
        return new Range(p1, (Item) r1.expr2, (Item) r2.expr2);
      } else */
      if ((r1.type == Comp.GT || r1.type == Comp.GE) 
          && (r2.type == Comp.LT || r2.type == Comp.LE) && p1.sameAs(p2)) {
        ctx.compInfo(OPTRANGE);
        return new Range(exprs, (Item) r1.expr2, r1.type == Comp.GE, 
            (Item) r2.expr2, r2.type == Comp.LE);
      }
    }
    return allOf(ctx);
  }
  
  /**
   * Returns an allOf()-Function of the class instance if no optimizations
   * are possible.. To be generalized soon.
   * @param ctx query context
   * @return expression
   */
  private Expr allOf(final XPContext ctx) {
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
    
    ctx.compInfo(OPTAND5);
    return new AllOf((LocPath) e1.expr1, ex, e1.type);
  }
  
  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr)
      throws QueryException {
    
    final int el = exprs.length;
    if(el == 0) return null;
    final Expr[] indexExprs = new Expr[el];
    
    // find index equivalents
    for(int i = 0; i != el; i++) {
      indexExprs[i] = exprs[i].indexEquivalent(ctx, curr);
      if(indexExprs[i] == null) return null;
    }

    // perform path step only once if all path expressions are the same
    final Expr[] ex = XPOptimizer.getIndexExpr(indexExprs);
    if(ex != null) return new Path(new InterSect(ex),
        ((Path) indexExprs[0]).expr2);

    ctx.compInfo(OPTAND4);
    return new InterSect(indexExprs).compile(ctx);
  }
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    int max = Integer.MIN_VALUE;
    for(final Expr expr : exprs) {
      final int nrIDs = expr.indexSizes(ctx, curr, min);
      if(nrIDs == 0 || nrIDs > min) return nrIDs;
      if(max < nrIDs) max = nrIDs;
    }
    return max;
  }
}
