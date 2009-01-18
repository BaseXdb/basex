package org.basex.query.xquery.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.ResetIter;
import org.basex.query.xquery.util.Scoring;
import org.basex.query.xquery.util.Var;
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
  public Expr comp(final XQContext ctx) throws XQException {
    expr = expr.comp(ctx);
    // empty sequence - empty loop
    if(expr.e()) return Seq.EMPTY;

    // [CG] extend to arbitrary types
    if(pos == null && score == null && (expr.returned(ctx) == Type.NOD ||
        expr.i())) var.bind(expr, ctx);

    ctx.vars.add(var);
    if(pos != null) ctx.vars.add(pos);
    if(score != null) ctx.vars.add(score);
    return this;
  }

  @Override
  public ResetIter iter(final XQContext ctx) {
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
      public Bln next() throws XQException {
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
  public boolean usesVar(final Var v) {
    return super.usesVar(v) || (score == null || !v.eq(score)) &&
      (pos == null || !v.eq(pos));
  }

  @Override
  public Expr removeVar(final Var v) {
    return !var.eq(v) && (score == null || !v.eq(score)) &&
        (pos == null && !v.eq(pos)) ? super.removeVar(v) : this;
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
