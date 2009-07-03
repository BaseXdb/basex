package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Var;

/**
 * Predicate expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class Pred extends Preds {
  /** Expression. */
  Expr root;
  /** Counter flag. */
  private boolean counter;

  /**
   * Constructor.
   * @param r expression
   * @param p predicates
   */
  public Pred(final Expr r, final Expr... p) {
    super(p);
    root = r;
  }

  @Override
  public final Expr comp(final QueryContext ctx) throws QueryException {
    if(super.comp(ctx) != this) return Seq.EMPTY;
    root = root.comp(ctx);

    if(root.e()) {
      ctx.compInfo(OPTPRE, this);
      return Seq.EMPTY;
    }

    // No predicates.. return root
    if(pred.length == 0) return root;
    final Expr p = pred[0];

    // Position predicate
    final Pos pos = p instanceof Pos ? (Pos) p : null;
    // Last flag
    final boolean last = p instanceof Fun && ((Fun) p).func == FunDef.LAST;
    // Use iterative evaluation
    if(pred.length == 1 && (last || pos != null || !uses(Use.POS, ctx)))
        return new IterPred(root, pred, pos, last);

    // faster runtime evaluation of variable counters (array[$pos] ...)
    counter = pred.length == 1 && p.returned(ctx) == Return.NUM &&
      !p.uses(Use.CTX, ctx);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    if(counter) {
      final Item it = pred[0].ebv(ctx);
      final long l = it.itr();
      final Expr e = Pos.get(l, l);
      return l != it.dbl() || e == Bln.FALSE ? Iter.EMPTY :
        new IterPred(root, pred, (Pos) e, false).iter(ctx);
    }

    final Iter iter = ctx.iter(root);
    final Item ci = ctx.item;
    final long cs = ctx.size;
    final long cp = ctx.pos;

    // cache results to support last() function
    final SeqIter si = new SeqIter();
    Item i;
    while((i = iter.next()) != null) si.add(i);

    // evaluates predicates
    for(final Expr p : pred) {
      ctx.size = si.size();
      ctx.pos = 1;
      int c = 0;
      for(int s = 0, sl = si.size(); s < sl; s++) {
        ctx.item = si.item[s];
        if(p.test(ctx) != null) si.item[c++] = si.item[s];
        ctx.pos++;
      }
      si.size(c);
    }

    ctx.item = ci;
    ctx.size = cs;
    ctx.pos = cp;
    return si;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return root.uses(u, ctx) || super.uses(u, ctx);
  }

  @Override
  public boolean removable(final Var v, final QueryContext ctx) {
    return root.removable(v, ctx) && super.removable(v, ctx);
  }

  @Override
  public Expr remove(final Var v) {
    root = root.remove(v);
    return super.remove(v);
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
