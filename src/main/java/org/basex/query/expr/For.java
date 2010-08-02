package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
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
   * @param ii input info
   * @param e variable input
   * @param v variable
   * @param p positional variable
   * @param s score variable
   */
  public For(final InputInfo ii, final Expr e, final Var v, final Var p,
      final Var s) {
    super(ii, e, v);
    pos = p;
    score = s;
  }

  @Override
  public ForLet comp(final QueryContext ctx) throws QueryException {
    // empty sequence - empty loop
    expr = checkUp(expr, ctx).comp(ctx);

    /* bind variable if zero or one values are returned,
       and if no variables and no context reference is used. */
    final SeqType ret = expr.returned(ctx);
    if(pos == null && score == null && ret.zeroOrOne()
        && !expr.uses(Use.VAR, ctx) && !expr.uses(Use.CTX, ctx)
        && !ctx.grouping) {
      ctx.compInfo(OPTBIND, var);
      var.bind(expr, ctx);
    } else {
      var.ret = new SeqType(ret.type, SeqType.Occ.O);
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
      public Item next() throws QueryException {
        init();
        final Item it = ir.next();
        if(it != null) return bind(it, ++c);
        reset();
        return null;
      }

      @Override
      public long size() throws QueryException {
        return expr.size(ctx);
      }

      @Override
      public Item get(final long i) throws QueryException {
        init();
        return bind(ir.get(i), i + 1);
      }

      @Override
      public boolean reset() {
        if(ir != null) {
          ctx.vars.reset(vs);
          ir.reset();
          ir = null;
          c = 0;
        }
        return true;
      }

      /**
       * Initializes the iterator.
       */
      private void init() throws QueryException {
        if(ir == null) {
          vs = ctx.vars.size();
          ir = ctx.iter(expr);
          ctx.vars.add(v);
          if(p != null) ctx.vars.add(p);
          if(s != null) ctx.vars.add(s);
        }
      }

      /**
       * Binds an item to the loop variables.
       * @param it item
       * @param i position counter
       * @return specified item
       */
      private Item bind(final Item it, final long i) throws QueryException {
        v.bind(it, ctx);
        if(p != null) p.bind(Itr.get(i), ctx);
        if(s != null) s.bind(Dbl.get(it.score()), ctx);
        return it;
      }
    };
  }

  @Override
  public long size(final QueryContext ctx) throws QueryException {
    return expr.size(ctx);
  }

  @Override
  boolean simple() {
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
    ser.openElement(this, VAR, var.name.atom());
    if(pos != null) ser.attribute(POS, pos.name.atom());
    if(score != null) ser.attribute(Token.token(SCORE), score.name.atom());
    expr.plan(ser);
    ser.closeElement();
  }
}
