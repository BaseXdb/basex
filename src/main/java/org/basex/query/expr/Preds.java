package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.CmpG.OpG;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.func.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Abstract predicate expression, implemented by {@link Filter} and
 * {@link AxisStep}.
 *
 * @author BaseX Team 2005-12, BSD License
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
  protected Preds(final InputInfo ii, final Expr[] p) {
    super(ii);
    preds = p;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(preds);
  }

  @Override
  public Expr analyze(final AnalyzeContext ctx) throws QueryException {
    for(int p = 0; p < preds.length; p++) preds[p] = preds[p].analyze(ctx);
    return this;
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    for(int p = 0; p < preds.length; ++p) {
      Expr pr = preds[p].compile(ctx).compEbv(ctx);
      pr = Pos.get(OpV.EQ, pr, pr, info);

      // position() = last() -> last()
      if(pr instanceof CmpG || pr instanceof CmpV) {
        final Cmp cmp = (Cmp) pr;
        if(cmp.expr[0].isFunction(Function.POSITION) &&
           cmp.expr[1].isFunction(Function.LAST)) {
          if(cmp instanceof CmpG && ((CmpG) cmp).op == OpG.EQ ||
             cmp instanceof CmpV && ((CmpV) cmp).op == OpV.EQ) {
            ctx.compInfo(OPTWRITE, pr);
            pr = cmp.expr[1];
          }
        }
      }

      if(pr.isValue()) {
        if(!pr.ebv(ctx, info).bool(info)) {
          ctx.compInfo(OPTREMOVE, description(), pr);
          return Empty.SEQ;
        }
        ctx.compInfo(OPTREMOVE, description(), pr);
        preds = Array.delete(preds, p--);
      } else if(pr instanceof And && !pr.uses(Use.POS)) {
        // replace AND expression with predicates (don't swap position tests)
        ctx.compInfo(OPTPRED, pr.description());
        final Expr[] and = ((And) pr).expr;
        final int m = and.length - 1;
        final ArrayList<Expr> tmp = new ArrayList<Expr>(preds.length + m);
        tmp.addAll(Arrays.asList(preds).subList(0, p));
        for(final Expr a : and) {
          // wrap test with boolean() if the result is numeric
          tmp.add(Function.BOOLEAN.get(info, a).compEbv(ctx));
        }
        tmp.addAll(Arrays.asList(preds).subList(p + 1, preds.length));
        preds = tmp.toArray(new Expr[tmp.size()]);
      } else {
        preds[p] = pr;
      }
    }
    return this;
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
    last = preds[0].isFunction(Function.LAST);

    boolean np1 = true;
    boolean np2 = true;
    for(int p = 0; p < preds.length; p++) {
      final boolean np = !preds[p].type().mayBeNumber() &&
          !preds[p].uses(Use.POS);
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
  public boolean preds(final Item it, final QueryContext ctx) throws QueryException {
    if(preds.length == 0) return true;

    // set context item and position
    Item i = null;
    for(final Expr p : preds) {
      ctx.value = it;
      i = p.test(ctx, info);
      if(i == null) return false;
    }
    // item accepted.. adopt last scoring value
    it.score(i.score());
    return true;
  }

  @Override
  public boolean uses(final Use u) {
    for(final Expr p : preds) {
      if(u == Use.POS && p.type().mayBeNumber() || p.uses(u)) return true;
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
  public void plan(final FElem plan) {
    for(final Expr p : preds) p.plan(plan);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Expr e : preds) sb.append("[" + e + ']');
    return sb.toString();
  }
}
