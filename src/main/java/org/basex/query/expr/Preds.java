package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.path.AxisStep;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * Abstract predicate expression, implemented by {@link Filter} and
 * {@link AxisStep}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Preds extends ParseExpr {
  /** Predicates. */
  public Expr[] pred;
  /** Compilation: first predicate uses last function. */
  public boolean last;
  /** Compilation: first predicate uses position. */
  public Pos pos;

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

  /**
   * Checks if this expression can be evaluated in an iterative manner.
   * This is possible if no predicate, or only the first, is positional, or
   * if a single {@code last()} predicate is specified.
   * @return result of check
   */
  protected boolean iterable() {
    // position predicate
    pos = pred[0] instanceof Pos ? (Pos) pred[0] : null;
    last = pred[0] instanceof Fun && ((Fun) pred[0]).def == FunDef.LAST;

    boolean pos1 = false;
    boolean pos2 = false;
    for(int p = 0; p < pred.length; p++) {
      final boolean ps = pred[p].type().mayBeNum() || pred[p].uses(Use.POS);
      pos1 |= ps;
      if(p > 0) pos2 |= ps;
    }
    return !pos1 || pos != null && !pos2 || last && pred.length == 1;
  }

  /**
   * Checks if the predicates are successful for the specified item.
   * @param it item to be checked
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean preds(final Item it, final QueryContext ctx)
      throws QueryException {

    // set context item and position
    ctx.value = it;
    for(final Expr p : pred) {
      final Item i = p.test(ctx, input);
      if(i == null) return false;
      // item accepted.. adopt last scoring value
      it.score(i.score());
    }
    return true;
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
