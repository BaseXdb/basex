package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.CmpG.Comp;
import org.basex.query.xquery.func.Fun;
import org.basex.query.xquery.func.FunDef;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.path.Step;
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
  public Expr comp(final XQContext ctx) throws XQException {
    for(int p = 0; p < pred.length; p++) {
      Expr ex = addPos(ctx, pred[p].comp(ctx));
      if(ex instanceof CmpG) {
        final CmpG cmp = (CmpG) ex;
        if(cmp.expr[0] instanceof Fun && ((Fun) cmp.expr[0]).func == FunDef.POS
            && cmp.expr[1].i()) {
          final Item i2 = (Item) cmp.expr[1];
          if(cmp.cmp == Comp.EQ && i2.n()) {
            ctx.compInfo(OPTSIMPLE, cmp, i2);
            ex = i2;
          }
        }
      }
      pred[p] = ex;

      if(ex.i()) {
        final Item it = (Item) ex;
        if(!it.bool()) {
          ctx.compInfo(OPTFALSE, it);
          return null;
        }
        if(!it.n()) {
          ctx.compInfo(OPTTRUE, it);
          pred = Array.delete(pred, p--);
          continue;
        }
      }
    }
    return this;
  }
  
  @Override
  public final boolean uses(final Using u) {
    for(final Expr p : pred) if(p.uses(u)) return true;
    
    if(u == Using.POS) {
      for(final Expr p : pred) {
        final Type t = p.returned();
        if(t == null || t.num) return true;
      }
    }
    return false;
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
