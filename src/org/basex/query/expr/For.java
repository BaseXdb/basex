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
import org.basex.query.util.Var;
import org.basex.util.Token;

/**
 * For Clause.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class For extends ForLet {
  /** Positional variable. */
  Var pos;
  /** Full-text score. */
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
        !expr.uses(Use.VAR, ctx)) {
      ctx.compInfo(OPTBIND, var);
      var.bind(expr, ctx);
    } else {
      final Return ret = expr.returned(ctx);
      if     (ret == Return.NUMSEQ)   var.ret = Return.NUM;
      else if(ret == Return.NONUMSEQ) var.ret = Return.NONUM;
      else if(ret == Return.NODSEQ)   var.ret = Return.NOD;
    }
    ctx.vars.add(var);

    if(pos != null) {
      ctx.vars.add(pos);
      pos.ret = Return.NUM;
    }
    if(score != null) {
      ctx.vars.add(score);
      score.ret = Return.NUM;
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    final Var v = var.clone();
    final Var p = pos != null ? pos.clone() : null;
    final Var s = score != null ? score.clone() : null;
    
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
          if(s != null) s.bind(Dbl.get(ctx.score.finish(it.score())), ctx);
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
