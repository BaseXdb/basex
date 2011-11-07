package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.Function;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.path.AxisStep;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.list.ObjList;

/**
 * Abstract predicate expression, implemented by {@link Filter} and
 * {@link AxisStep}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Preds extends ParseExpr {
  /** Predicates. */
  public Expr[] preds;
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
    preds = p;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final Expr p : preds) checkUp(p, ctx);

    Expr e = this;
    for(int p = 0; p < preds.length; ++p) {
      Expr pr = preds[p].comp(ctx).compEbv(ctx);
      pr = Pos.get(CmpV.Op.EQ, pr, pr, input);

      if(pr.value()) {
        if(!pr.ebv(ctx, input).bool(input)) {
          ctx.compInfo(OPTREMOVE, desc(), pr);
          e = Empty.SEQ;
          break;
        }
        ctx.compInfo(OPTREMOVE, desc(), pr);
        preds = Array.delete(preds, p--);
      } else {
        preds[p] = pr;

        // replace AND expression with predicates (don't swap position tests)
        if(preds[p] instanceof And && !preds[p].uses(Use.POS)) {
          ctx.compInfo(OPTPRED, preds[p].desc());
          final Expr[] and = ((And) preds[p]).expr;
          final int m = and.length - 1;
          final ObjList<Expr> tmp = new ObjList<Expr>(preds.length + m);
          for(int i = 0; i < p; i++) tmp.add(preds[i]);
          for(final Expr a : and) {
            // wrap test with boolean() if the result is numeric
            tmp.add(Function.BOOLEAN.get(input, a).compEbv(ctx));
          }
          for(int i = p + 1; i < preds.length; i++) tmp.add(preds[i]);
          preds = tmp.toArray(new Expr[tmp.size()]);
        }
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
  protected boolean useIterator() {
    // numeric predicate
    pos = preds[0] instanceof Pos ? (Pos) preds[0] : null;
    last = preds[0].isFun(Function.LAST);

    boolean np1 = true;
    boolean np2 = true;
    for(int p = 0; p < preds.length; p++) {
      final boolean np = !preds[p].type().mayBeNum() && !preds[p].uses(Use.POS);
      np1 &= np;
      if(p > 0) np2 &= np;
    }
    return np1 || pos != null && np2 || last && preds.length == 1;
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

    if(preds.length == 0) return true;

    // set context item and position
    Item i = null;
    for(final Expr p : preds) {
      ctx.value = it;
      i = p.test(ctx, input);
      if(i == null) return false;
    }
    // item accepted.. adopt last scoring value
    it.score(i.score());
    return true;
  }

  @Override
  public boolean uses(final Use u) {
    for(final Expr p : preds) {
      if(u == Use.POS && p.type().mayBeNum() || p.uses(u)) return true;
    }
    return false;
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final Expr p : preds) c += p.count(v);
    return c;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr p : preds) if(p.count(v) != 0) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    for(int p = 0; p < preds.length; ++p) preds[p] = preds[p].remove(v);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    for(final Expr p : preds) p.plan(ser);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Expr e : preds) sb.append("[" + e + "]");
    return sb.toString();
  }
}
