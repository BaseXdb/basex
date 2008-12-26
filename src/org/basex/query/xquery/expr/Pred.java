package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
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
  Expr root;

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
    for(int p = 0; p != pred.length; p++) {
      pred[p] = ctx.comp(pred[p]);
      if(pred[p].i()) {
        final Item it = (Item) pred[p];
        if(!it.bool()) return Seq.EMPTY;
        if(it.n()) continue;
        Array.move(pred, p + 1, -1, pred.length - p-- - 1);
        pred = Array.finish(pred, pred.length - 1);
      }
    }
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
