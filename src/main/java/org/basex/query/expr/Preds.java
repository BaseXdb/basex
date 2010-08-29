package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.path.Step;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * Abstract predicate expression, implemented by {@link Filter} and
 * {@link Step}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Preds extends ParseExpr {
  /** Predicates. */
  public Expr[] pred;

  /**
   * Constructor.
   * @param ii input info
   * @param p predicates
   */
  public Preds(final InputInfo ii, final Expr[] p) {
    super(ii);
    pred = p;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final Expr p : pred) checkUp(p, ctx);

    Expr e = this;
    for(int p = 0; p < pred.length; ++p) {
      Expr pr = pred[p].comp(ctx).compEbv(ctx);
      pr = Pos.get(CmpV.Op.EQ, pr, pr, input);

      if(pr.value()) {
        if(!pr.ebv(ctx, input).bool(input)) {
          ctx.compInfo(OPTREMOVE, desc(), pr);
          e = Empty.SEQ;
          break;
        }
        ctx.compInfo(OPTREMOVE, desc(), pr);
        pred = Array.delete(pred, p--);
      } else {
        pred[p] = pr;
      }
    }
    return e;
  }

  @Override
  public boolean uses(final Use u) {
    for(final Expr p : pred) {
      if(u == Use.POS && p.type().mayBeNum() || p.uses(u)) return true;
    }
    return false;
  }

  @Override
  public boolean uses(final Var v) {
    for(final Expr p : pred) if(p.uses(v)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr p : pred) if(p.uses(v)) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    for(int p = 0; p < pred.length; ++p) pred[p] = pred[p].remove(v);
    return this;
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
