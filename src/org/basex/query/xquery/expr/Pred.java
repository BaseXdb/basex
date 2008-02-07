package org.basex.query.xquery.expr;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Scoring;
import org.basex.util.Token;

/**
 * Predicate expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Pred extends Arr {
  /** Expression. */
  protected Expr root;

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
  public Expr comp(final XQContext ctx) throws XQException {
    root = root.comp(ctx);
    return super.comp(ctx);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    Iter iter = ctx.iter(root);

    final Item c = ctx.item;
    final int cs = ctx.size;
    final int cp = ctx.pos;

    for(final Expr e : expr) {
      ctx.size = (int) iter.size();
      ctx.pos = 1;
      final SeqIter seq = new SeqIter();
      Item it;
      while((it = iter.next()) != null) {
        ctx.item = it;
        final Item i = ctx.iter(e).ebv();
        if(i.n() ? i.dbl() == ctx.pos : i.bool()) {
          it.score(Scoring.add(i.score(), it.score()));
          seq.add(it);
        }
        ctx.pos++;
      }
      iter = seq;
    }

    ctx.item = c;
    ctx.size = cs;
    ctx.pos = cp;
    return iter;
  }

  @Override
  public boolean uses(final Using u) {
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
  public String toString() {
    return Token.string(name()) + "(" + root + "[" + toString("][") + "])";
  }

  @Override
  public String info() {
    return "Predicate";
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    root.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "FF6666";
  }
}
