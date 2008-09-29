package org.basex.query.xquery.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.xquery.XQTokens.*;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.iter.Iter;
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
   * @throws XQException xquery exception
   */
  public For(final Expr e, final Var v, final Var p, final Var s)
      throws XQException {
    expr = e;
    var = v;
    pos = p;
    score = s;
    if(score != null) score.item(Dbl.ZERO, null);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    expr = expr.comp(ctx);
    // empty sequence - empty loop
    return expr.e() ? Seq.EMPTY : this;
  }

  @Override
  public Iter iter(final XQContext ctx) {
    final Var v = var.clone();
    final Var p = pos != null ? pos.clone() : null;
    final Var sc = score != null ? score.clone() : null;
    
    return new Iter() {
      /** Iterator flag. */
      private boolean more;
      /** Variable stack size. */
      private int vs;
      /** Iterator flag. */
      private Iter iter;
      /** Counter. */
      private int c;
      
      @Override
      public Bln next() throws XQException {
        if(!more) {
          vs = ctx.vars.size();
          iter = ctx.iter(expr);
          ctx.vars.add(v);
          if(p != null) ctx.vars.add(p);
          if(sc != null) ctx.vars.add(sc);
          c = 0;
        }

        final Item it = iter.next();
        more = it != null;
        if(more) {
          v.item(it, ctx);
          if(p != null) p.item(Itr.get(++c), ctx);
          // assign score value
          if(sc != null) sc.item(Dbl.get(Scoring.finish(it.score())), ctx);
        } else {
          ctx.vars.reset(vs);
        }
        return Bln.get(more);
      }

      @Override
      public void reset() {
        more = false;
      }
    };
  }

  @Override
  public String toString() {
    return FOR + " " + var + " " + IN + " " + expr;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.startElement(this);
    ser.attribute(VAR, var.name.str());
    if(pos != null) ser.attribute(POS, pos.name.str());
    if(score != null) ser.attribute(Token.token(SCORE), score.name.str());
    ser.finishElement();
    expr.plan(ser);
    ser.closeElement(this);
  }
}
