package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.func.Fun;
import org.basex.query.xquery.func.FunDef;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;

/**
 * Predicate expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Pred extends Preds {
  /** Expression. */
  public Expr root;

  /**
   * Constructor.
   * @param r expression
   * @param p predicates
   */
  public Pred(final Expr r, final Expr[] p) {
    super(p);
    root = r;
  }

  @Override
  public final Expr comp(final XQContext ctx) throws XQException {
    if(super.comp(ctx) != this) return Seq.EMPTY;
    root = ctx.comp(root);

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
    return new IterPred(root, pred, last, num);
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
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    root.plan(ser);
    super.plan(ser);
    ser.closeElement();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder(root.toString());
    sb.append(super.toString());
    return sb.toString();
  }
}
