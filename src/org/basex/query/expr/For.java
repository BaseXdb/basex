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
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ResetIter;
import org.basex.query.util.Scoring;
import org.basex.query.util.Var;
import org.basex.util.Token;

/**
 * For Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class For extends ForLet {
  /** Positional variable. */
  Var pos;
  /** Fulltext score. */
  Var score;

  /**
   * Constructor.
   * @param e variable input
   * @param v variable
   * @param p positional variable
   * @param s score variable
   */
  public For(final Expr e, final Var v, final Var p, final Var s) {
    expr = e;
    var = v;
    pos = p;
    score = s;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    expr = expr.comp(ctx);
    // empty sequence - empty loop
    if(expr.e()) return Seq.EMPTY;

    // bind variable if single value is returned and if no variables are used
    if(pos == null && score == null && expr.returned(ctx).single &&
        expr.countVar(null) == 0) {
      ctx.compInfo(OPTBIND, var);
      var.bind(expr, ctx);
    }

    ctx.vars.add(var);
    if(pos != null) ctx.vars.add(pos);
    if(score != null) ctx.vars.add(score);
    return this;
  }

  @Override
  public ResetIter iter(final QueryContext ctx) {
    final Var v = var.clone();
    final Var p = pos != null ? pos.clone() : null;
    final Var s = score != null ? score.clone() : null;
    
    return new ResetIter() {
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
          if(s != null) s.bind(Dbl.get(Scoring.finish(it.score())), ctx);
        } else {
          reset();
        }
        return Bln.get(ir != null);
      }

      @Override
      public void reset() {
        ctx.vars.reset(vs);
        ir = null;
        c = 0;
      }
    };
  }
  
  @Override
  public boolean shadows(final Var v) {
    return super.shadows(v) || !v.visible(pos) || !v.visible(score);
  }
  
  @Override
  public String toString() {
    return FOR + " " + var + " " + IN + " " + expr;
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
