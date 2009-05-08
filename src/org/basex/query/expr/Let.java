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
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.Token;

/**
 * Let Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Let extends ForLet {
  /** Scoring flag. */
  boolean score;

  /**
   * Constructor.
   * @param e variable input
   * @param v variable
   * @param s score flag
   */
  public Let(final Expr e, final Var v, final boolean s) {
    expr = e;
    var = v;
    score = s;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    expr = expr.comp(ctx);

    // bind variable if expression uses no var, pos, ctx or fragment
    if(!score && !(expr.uses(Use.VAR, ctx) || expr.uses(Use.POS, ctx) ||
        expr.uses(Use.CTX, ctx) || expr.uses(Use.FRG, ctx))) {
      ctx.compInfo(OPTBIND, var);
      //var.bind(expr, ctx);
      var.ret = expr.returned(ctx);
    } else {
      var.ret = expr.returned(ctx);
    }

    ctx.vars.add(var);
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    final Var v = var.clone();

    return new Iter() {
      /** Variable stack size. */
      private int vs;
      /** Iterator flag. */
      private boolean more;

      @Override
      public Bln next() throws QueryException {
        if(!more) {
          vs = ctx.vars.size();
          final Iter ir = ctx.iter(expr);
          Item it;
          if(score) {
            // assign average score value
            double s = 0;
            int c = 0;
            while((it = ir.next()) != null) {
              s += it.score();
              c++;
            }
            it = Dbl.get(ctx.score.finish(s / c));
          } else {
            it = ir.finish();
          }
          ctx.vars.add(v.bind(it, ctx));
          more = true;
        } else {
          reset();
        }
        return Bln.get(more);
      }

      @Override
      public boolean reset() {
        ctx.vars.reset(vs);
        more = false;
        return true;
      }
    };
  }

  @Override
  boolean standard() {
    return !score;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, score ? Token.token(SCORE) : VAR, var.name.str());
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return LET + " " + var + " " + ASSIGN + " " + expr;
  }
}
