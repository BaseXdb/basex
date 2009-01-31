package org.basex.query.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Var;

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
  public final Expr comp(final QueryContext ctx) throws QueryException {
    if(super.comp(ctx) != this) return Seq.EMPTY;
    root = root.comp(ctx);
    
    // No predicates.. return root
    if(pred.length == 0) return root;
    final Expr p = pred[0];

    // Position predicate
    final Pos pos = p instanceof Pos ? (Pos) p : null;
    // Last flag
    final boolean last = p instanceof Fun && ((Fun) p).func == FunDef.LAST;
    // Multiple Predicates or POS
    if(pred.length > 1 || !last && pos == null && usesPos(ctx)) return this;
    // Use iterative evaluation
    return new IterPred(root, pred, pos, last);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // quick evaluation of variable counters (array[$pos] ...)
    if(pred.length == 1 && pred[0] instanceof VarCall) {
      final Item it = pred[0].iter(ctx).ebv();
      if(it.n()) {
        final long l = it.itr();
        return new IterPred(root, pred, (Pos) Pos.get(l, l), false).iter(ctx);
      }
    }
    
    final Iter iter = ctx.iter(root);
    final Item ci = ctx.item;
    final long cs = ctx.size;
    final long cp = ctx.pos;
    
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
        if(ctx.iter(p).test(ctx) != null) sb.item[c++] = sb.item[s];
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
  public boolean usesPos(final QueryContext ctx) {
    return root.usesPos(ctx) || super.usesPos(ctx);
  }

  @Override
  public int countVar(final Var v) {
    return root.countVar(v) + super.countVar(v);
  }

  @Override
  public Expr removeVar(final Var v) {
    root = root.removeVar(v);
    for(int p = 0; p < pred.length; p++) pred[p] = pred[p].removeVar(v);
    return this;
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
