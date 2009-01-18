package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.ResetIter;
import org.basex.query.xquery.util.Scoring;
import org.basex.query.xquery.util.Var;
import org.basex.util.Token;

/**
 * Let Clause.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
   * @throws XQException xquery exception
   */
  public Let(final Expr e, final Var v, final boolean s) throws XQException {
    expr = e;
    var = v;
    score = s;
    if(s) var.bind(Dbl.ZERO, null);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    expr = expr.comp(ctx);

    if(!score) {
      // [CG] extend to arbitrary types
      if(expr.returned(ctx) == Type.NOD || expr.i()) var.bind(expr, ctx);
    }
    
    ctx.vars.add(var);
    return this;
  }

  @Override
  public ResetIter iter(final XQContext ctx) {
    final Var v = var.clone();

    return new ResetIter() {
      /** Variable stack size. */
      private int vs;
      /** Iterator flag. */
      private boolean more;

      @Override
      public Bln next() throws XQException {
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
            it = Dbl.get(Scoring.finish(s / c));
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
      public void reset() {
        ctx.vars.reset(vs);
        more = false;
      }
    };
  }
  
  @Override
  public String toString() {
    return LET + " " + var + " " + ASSIGN + " " + expr;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, score ? Token.token(SCORE) : VAR, var.name.str());
    expr.plan(ser);
    ser.closeElement();
  }
}
