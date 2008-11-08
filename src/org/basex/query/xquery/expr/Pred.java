package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.func.Fun;
import org.basex.query.xquery.func.FunDef;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.util.Token;

/**
 * Predicate expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Pred extends Arr {
  /** Expression. */
  Expr root;

  /**
   * Constructor.
   * @param r expression
   * @param e predicates
   */
  public Pred(final Expr r, final Expr[] e) {
    super(e);
    root = r;
  }

  @Override
  public final Expr comp(final XQContext ctx) throws XQException {
    root = ctx.comp(root);
    super.comp(ctx);
    // LAST
    final boolean last = expr[0] instanceof Fun &&
      ((Fun) expr[0]).func == FunDef.LAST;
    // Numeric value
    final boolean num = expr[0].i() && ((Item) expr[0]).n();
    // Multiple Predicates or POS
    return expr.length > 1 || (!last && !num && uses(Using.POS)) ? this :
      new PredIter(root, expr, last, num);
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
    for(final Expr p : expr) {
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
    switch(u) {
      case POS:
        for(final Expr e : expr) {
          final Type t = e.returned();
          if(t == null || t.num || e.uses(u)) return true;
        }
        return super.uses(u);
      default:
        return super.uses(u);
    }
  }

  @Override
  public final String toString() {
    return Token.string(name()) + "(" + root + ", " + toString(", ") + ")";
  }

  @Override
  public final String info() {
    return "Predicate";
  }

  @Override
  public final void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NS, timer());
    root.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }
}
