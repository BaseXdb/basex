package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.CmpG.Comp;
import org.basex.query.xquery.func.Fun;
import org.basex.query.xquery.func.FunDef;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.util.Array;

/**
 * Predicate expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Pred extends Expr {
  /** Predicates. */
  public Expr[] pred;
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param r expression
   * @param p predicates
   */
  public Pred(final Expr r, final Expr[] p) {
    root = r;
    pred = p;
  }

  @Override
  public final Expr comp(final XQContext ctx) throws XQException {
    root = ctx.comp(root);
    pred = comp(pred, ctx);
    if(pred == null) return Seq.EMPTY;

    // No predicates.. return root
    if(pred.length == 0) return root;

    // Last flag
    final boolean last = pred[0] instanceof Fun &&
      ((Fun) pred[0]).func == FunDef.LAST;
    // Numeric value
    final boolean num = pred[0].i() && ((Item) pred[0]).n();
    // Multiple Predicates or POS
    if(pred.length > 1 || !last && !num && uses(Using.POS)) return this;
    // Use iterative evaluation
    return new PredIter(root, pred, last, num);
  }  

  /**
   * Compiles the specified predicates.
   * @param pred predicates
   * @param ctx context
   * @return compiled predicates or null if predicates are always false
   * @throws XQException query exception
   */
  public static Expr[] comp(final Expr[] pred, final XQContext ctx)
      throws XQException {
    
    Expr[] expr = {};
    for(final Expr p : pred) {
      Expr ex = ctx.comp(p);
      if(ex instanceof CmpG) {
        final CmpG cmp = (CmpG) ex;
        if(cmp.expr[0] instanceof Fun && ((Fun) cmp.expr[0]).func == FunDef.POS
            && cmp.expr[1].i()) {
          final Item i2 = (Item) cmp.expr[1];
          if(cmp.cmp == Comp.EQ && i2.n()) {
            ctx.compInfo(OPTSIMPLE, cmp, i2);
            ex = i2;
          }
        }
      }
      if(ex.i()) {
        final Item it = (Item) ex;
        if(!it.bool()) {
          ctx.compInfo(OPTFALSE, it);
          return null;
        }
        if(!it.n()) {
          ctx.compInfo(OPTTRUE, it);
          continue;
        }
      }
      expr = Array.add(expr, ex);
    }
    return expr;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Iter iter = ctx.iter(root);
    final Item ci = ctx.item;
    final int cs = ctx.size;
    final int cp = ctx.pos;
    
    // cache results to support last() function
    final SeqIter sb = new SeqIter();
    Item i;
    while((i = iter.next()) != null) sb.add(i);

    // evaluates predicates
    for(final Expr p : pred) {
      ctx.size = sb.size;
      ctx.pos = 1;
      int c = 0;
      for(int s = 0; s < sb.size; s++) {
        ctx.item = sb.item[s];
        i = ctx.iter(p).ebv();
        if(i.n() ? i.dbl() == ctx.pos : i.bool()) sb.item[c++] = sb.item[s];
        ctx.pos++;
      }
      sb.size = c;
    }

    ctx.item = ci;
    ctx.size = cs;
    ctx.pos = cp;
    return sb;
  }
  
  @Override
  public final boolean uses(final Using u) {
    for(final Expr p : pred) if(p.uses(u)) return true;
    
    if(u == Using.POS) {
      for(final Expr p : pred) {
        final Type t = p.returned();
        if(t == null || t.num) return true;
      }
    }
    return false;
  }

  @Override
  public final String color() {
    return "FFFF66";
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    root.plan(ser);
    for(final Expr e : pred) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder(root.toString());
    for(final Expr e : pred) sb.append("[" + e + "]");
    return sb.toString();
  }
}
