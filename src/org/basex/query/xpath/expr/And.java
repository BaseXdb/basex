package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
import org.basex.query.QueryException;
import org.basex.query.xpath.ExprList;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.internal.AllOf;
import org.basex.query.xpath.internal.Range;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Comp;
import org.basex.query.xpath.item.Item;
import org.basex.query.xpath.path.LocPath;
import org.basex.query.xpath.path.Step;

/**
 * And expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class And extends Arr {
  /**
   * Constructor.
   * @param e expressions
   */
  public And(final Expr[] e) {
    super(e);
  }

  @Override
  public Bln eval(final XPContext ctx) throws QueryException {
    for(final Expr e : expr) if(!ctx.eval(e).bool()) return Bln.FALSE;
    return Bln.TRUE;
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    super.comp(ctx);
    final ExprList eil0 = new ExprList();
    for(int e = 0; e != expr.length; e++) {
      // check if we can add a position predicate to match only the first title
      if(expr[e] instanceof LocPath) {
        ((LocPath) expr[e]).addPosPred(ctx);
      } else if(expr[e] instanceof Item) {
        // simplify operand
        if(!((Item) expr[e]).bool()) {
          // this expression will always be false; remove expression
          ctx.compInfo(OPTAND1);
          return Bln.FALSE;
        }
        
        // remove element from and expression that is always true
        ctx.compInfo(OPTAND2);
        final Expr[] tmp = new Expr[expr.length - 1];
        System.arraycopy(expr, 0, tmp, 0, e);
        System.arraycopy(expr, e + 1, tmp, e, expr.length - e-- - 1);
        expr = tmp;
      } else if (expr[e] instanceof Or) {
        // sum up and predicates
        final ExprList eil = new ExprList();
        final Or o = (Or) expr[e];
        for (int j = 0; j < o.expr.length; j++)
          eil.add(o.expr[j], ctx, false);

        if (eil.size > 0 && eil.size < o.expr.length) {
          Expr[] ex = eil.finishE();
          if (ex.length == 1) expr[e] = ex[0];
          else o.expr = eil.finishE();
          ctx.compInfo(OPTPREDS);
       } 
     } 
      eil0.add(expr[e], ctx, true);
    }
    
    if (eil0.size < expr.length)
      expr = eil0.finishE();
    
    if(expr.length == 0) return Bln.TRUE;
    if(expr.length == 1) return expr[0];

    // merge several position tests to a single one
    Pos pos = new Pos(Integer.MIN_VALUE, Integer.MAX_VALUE);
    for(final Expr e : expr) {
      if(!(e instanceof Pos)) {
        pos = null;
        break;
      }
      pos.min = Math.max(pos.min, ((Pos) e).min);
      pos.max = Math.min(pos.max, ((Pos) e).max);
    }
    if(pos != null) return pos;
    
    // optimization to speedup range queries (not that elegant yet)
    if(expr.length == 2 && expr[0] instanceof Cmp &&
        expr[1] instanceof Cmp) {
      final Cmp r1 = (Cmp) expr[0];
      final Cmp r2 = (Cmp) expr[1];
      if(!r1.standard() || !r2.standard()) return this;
      final LocPath p1 = (LocPath) r1.expr[0];
      final LocPath p2 = (LocPath) r2.expr[0];
      
      // [CG] XPath/support GT & LT
      if(r1.type == Comp.GE && r2.type == Comp.LE && p1.sameAs(p2)) {
        ctx.compInfo(OPTRANGE);
        return new Range(p1, (Item) r1.expr[1], (Item) r2.expr[1]);
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
    
    ctx.compInfo(OPTAND5);
    return new AllOf((LocPath) e1.expr[0], ex, e1.type);
  }
  
  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq) throws QueryException {
    
    final int el = expr.length;
    if(el == 0) return null;
    final Expr[] indexExprs = new Expr[el];
    boolean ie = true;
    
    // find index equivalents
    for(int i = 0; i != el; i++) {
      indexExprs[i] = expr[i].indexEquivalent(ctx, curr, seq);
      if(indexExprs[i] == null) ie = false; 
    }

    if (!ie) return null;
    
    // perform path step only once if all path expressions are the same
    final Expr[] ex = XPOptimizer.getIndexExpr(indexExprs);
    if(ex != null) return new Path(new InterSect(ex),
        ((Path) indexExprs[0]).path);

    ctx.compInfo(OPTAND4);
    return new InterSect(indexExprs).comp(ctx);
  }
  
  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    int max = Integer.MIN_VALUE;
    for(final Expr e : expr) {
      final int nrIDs = e.indexSizes(ctx, curr, min);
      if(nrIDs == 0 || nrIDs > min) return nrIDs;
      if(max < nrIDs) max = nrIDs;
    }
    return max;
  }

  @Override
  public String toString() {
    return toString(" and ");
  }
}
