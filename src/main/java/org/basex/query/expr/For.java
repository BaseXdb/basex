package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
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
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class For extends ForLet {
  /** Positional variable. */
  protected final Var pos;
  /** Full-text score. */
  protected final Var score;

  /**
   * Constructor.
   * @param ii input info
   * @param e variable input
   * @param v variable
   */
  public For(final InputInfo ii, final Expr e, final Var v) {
    this(ii, e, v, null, null);
  }

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
  public For comp(final QueryContext ctx) throws QueryException {
    expr = checkUp(expr, ctx).comp(ctx);
    type = expr.type();

    // bind variable or set return type
    //if(pos != null || score != null || !type.one() || !bind(ctx)) {
      var.ret = ctx.grouping ? SeqType.get(type.type, SeqType.Occ.ZM) :
        type.type.seq();
    //}
    ctx.vars.add(var);
    if(pos   != null) ctx.vars.add(pos);
    if(score != null) ctx.vars.add(score);

    size = expr.size();
    return this;
  }

  @Override
  protected boolean bind(final QueryContext ctx) throws QueryException {
    return simple(true) && super.bind(ctx);
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
      public long size() {
        return expr.size();
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
  boolean simple(final boolean one) {
    return pos == null && score == null && (!one || type.one());
  }

  @Override
  public boolean shadows(final Var v) {
    return var.eq(v) || pos != null && pos.eq(v) ||
      score != null && score.eq(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, VAR, token(var.toString()));
    if(pos != null) ser.attribute(POS, token(pos.toString()));
    if(score != null) ser.attribute(Token.token(SCORE),
        token(score.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FOR + " " + var + " ");
    if(pos != null) sb.append(AT + " " + pos + " ");
    if(score != null) sb.append(SCORE + " " + score + " ");
    return sb.append(IN + " " + expr).toString();
  }
}
