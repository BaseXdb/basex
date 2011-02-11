package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.ft.Scoring;

/**
 * Let clause.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class Let extends ForLet {
  /** Scoring flag. */
  protected final boolean score;

  /**
   * Constructor.
   * @param ii input info
   * @param e variable input
   * @param v variable
   */
  public Let(final InputInfo ii, final Expr e, final Var v) {
    this(ii, e, v, false);
  }

  /**
   * Constructor.
   * @param ii input info
   * @param e variable input
   * @param v variable
   * @param s score flag
   */
  public Let(final InputInfo ii, final Expr e, final Var v, final boolean s) {
    super(ii, e, v);
    score = s;
  }

  @Override
  public Let comp(final QueryContext ctx) throws QueryException {
    // always returns a self reference
    expr = checkUp(expr, ctx).comp(ctx);

    // bind variable or set return type
    if(score || !bind(ctx)) {
      // set return type if variable cannot be statically bound
      var.ret = score ? SeqType.DBL : expr.type();
    }
    ctx.vars.add(var);

    type = SeqType.ITEM;
    size = 1;
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    final Var vr = var.copy();

    return new Iter() {
      /** Variable stack size. */
      private int vs;
      /** Iterator flag. */
      private boolean more;

      @Override
      public Item next() throws QueryException {
        if(!more) {
          vs = ctx.vars.size();
          Value v;
          if(score) {
            // assign average score value
            double s = 0;
            int c = 0;
            Item it;
            final Iter ir = ctx.iter(expr);
            while((it = ir.next()) != null) {
              s += it.score();
              ++c;
            }
            v = Dbl.get(Scoring.let(s, c));
          } else {
            v = expr.value(ctx);
          }
          ctx.vars.add(vr.bind(v, ctx));
          more = true;
          return Bln.TRUE;
        }
        reset();
        return null;
      }

      @Override
      public long size() {
        return 1;
      }

      @Override
      public Item get(final long i) throws QueryException {
        reset();
        return next();
      }

      @Override
      public boolean reset() {
        if(more) {
          ctx.vars.reset(vs);
          more = false;
        }
        return true;
      }
    };
  }

  @Override
  boolean simple() {
    return !score;
  }

  @Override
  public boolean shadows(final Var v) {
    return var.eq(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, score ? Token.token(SCORE) : VAR,
        token(var.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(LET).append(' ');
    if(score) sb.append(SCORE).append(' ');
    sb.append(var).append(' ').append(ASSIGN).append(' ').append(expr);
    return sb.toString();
  }
}
