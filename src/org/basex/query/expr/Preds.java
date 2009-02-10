package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.query.path.Step;
import org.basex.query.util.Var;
import org.basex.util.Array;

/**
 * Abstract predicate expression, implemented by {@link Pred} and
 * {@link Step}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Preds extends Expr {
  /** Predicates. */
  public Expr[] pred;

  /**
   * Constructor.
   * @param p predicates
   */
  public Preds(final Expr[] p) {
    pred = p;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final Item ci = ctx.item;
    final Type ct = ci != null ? ci.type : null;
    if(ct == Type.DOC) ctx.item.type = Type.ELM;

    for(int p = 0; p < pred.length; p++) {
      Expr ex = pred[p].comp(ctx);
      ex = Pos.get(ex, CmpV.Comp.EQ, ex);
      
      if(ex.i()) {
        if(((Item) ex).bool()) {
          ctx.compInfo(OPTTRUE, ex);
          pred = Array.delete(pred, p--);
        } else {
          ctx.compInfo(OPTFALSE, ex);
          ctx.item = ci;
          return null;
        }
      } else {
        pred[p] = ex;
      }
    }

    ctx.item = ci;
    if(ci != null) ctx.item.type = ct;
    return this;
  }
  
  @Override
  public boolean uses(final Use use, final QueryContext ctx) {
    for(final Expr p : pred) {
      if(use == Use.POS && p.returned(ctx).num || p.uses(use, ctx)) return true;
    }
    return false;
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final Expr p : pred) c += p.count(v);
    return c;
  }

  @Override
  public Expr remove(final Var v) {
    for(int p = 0; p < pred.length; p++) pred[p] = pred[p].remove(v);
    return this;
  }

  @Override
  public final String color() {
    return "FFFF66";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    for(final Expr p : pred) p.plan(ser);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Expr e : pred) sb.append("[" + e + "]");
    return sb.toString();
  }
}
