package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.Token;

/**
 * For clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class For extends ForLet {
  /** Positional variable. */
  private final Var pos;
  /** Full-text score. */
  private final Var score;

  /**
   * Constructor.
   * @param e variable input
   * @param v variable
   * @param p positional variable
   * @param s score variable
   */
  public For(final Expr e, final Var v, final Var p, final Var s) {
    super(e, v);
    pos = p;
    score = s;
  }

  @Override
  public ForLet comp(final QueryContext ctx) throws QueryException {
    // empty sequence - empty loop
    expr = checkUp(expr, ctx).comp(ctx);

    // bind variable if single value is returned and if no variables are used
    final SeqType ret = expr.returned(ctx);
    if(pos == null && score == null && ret.single() &&
        !expr.uses(Use.VAR, ctx)) {
      ctx.compInfo(OPTBIND, var);
      var.bind(expr, ctx);
    } else {
      var.ret = new SeqType(ret.type, SeqType.OCC_1);
    }
    ctx.vars.add(var);

    if(pos != null) {
      ctx.vars.add(pos);
      pos.ret = SeqType.ITR;
    }
    if(score != null) {
      ctx.vars.add(score);
      score.ret = SeqType.ITR;
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    final Var v = var.copy();
    final Var p = pos != null ? pos.copy() : null;
    final Var s = score != null ? score.copy() : null;

    return new Iter() {
      /** Variable stack size. */
      private int vs;
      /** Iterator flag. */
      private Iter ir;
      /** Counter. */
      private int c;

      @Override
      public Bln next() throws QueryException {
        if(ir == null) {
          vs = ctx.vars.size();
          ir = ctx.iter(expr);
          ctx.vars.add(v);
          if(p != null) ctx.vars.add(p);
          if(s != null) ctx.vars.add(s);
        }

        final Item it = ir.next();
        if(it != null) {
          v.bind(it, ctx);
          if(p != null) p.bind(Itr.get(++c), ctx);
          if(s != null) s.bind(Dbl.get(it.score()), ctx);
        } else {
          reset();
        }
        return Bln.get(ir != null);
      }

      @Override
      public boolean reset() {
        ctx.vars.reset(vs);
        if(ir != null) ir.reset();
        ir = null;
        c = 0;
        return true;
      }
    };
  }

  @Override
  boolean standard() {
    return pos == null && score == null;
  }

  @Override
  public boolean shadows(final Var v) {
    return super.shadows(v) || !v.visible(pos) || !v.visible(score);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FOR + " " + var + " ");
    if(pos != null) sb.append(AT + " " + pos + " ");
    if(score != null) sb.append(SCORE + " " + score + " ");
    return sb.append(IN + " " + expr).toString();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, VAR, var.name.str());
    if(pos != null) ser.attribute(POS, pos.name.str());
    if(score != null) ser.attribute(Token.token(SCORE), score.name.str());
    expr.plan(ser);
    ser.closeElement();
  }
}
