package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.CmpG.OpG;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.func.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract predicate expression, implemented by {@link Filter} and
 * {@link Step}.
 *
 * @author BaseX Team 2005-13, BSD License
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
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    for(int p = 0; p < preds.length; ++p)
      preds[p] = preds[p].compile(ctx, scp).compEbv(ctx);
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    for(int p = 0; p < preds.length; ++p) {
      Expr pr = Pos.get(OpV.EQ, preds[p], preds[p], info);

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
          ctx.compInfo(OPTREMOVE, this, pr);
          return Empty.SEQ;
        }
        ctx.compInfo(OPTREMOVE, this, pr);
        preds = Array.delete(preds, p--);
      } else if(pr instanceof And && !pr.has(Flag.FCS)) {
        // replace AND expression with predicates (don't swap position tests)
        ctx.compInfo(OPTPRED, pr);
        final Expr[] and = ((Arr) pr).expr;
        final int m = and.length - 1;
        final ExprList tmp = new ExprList(preds.length + m);
        for(final Expr e : Arrays.asList(preds).subList(0, p)) tmp.add(e);
        for(final Expr a : and) {
          // wrap test with boolean() if the result is numeric
          tmp.add(Function.BOOLEAN.get(null, info, a).compEbv(ctx));
        }
        for(final Expr e : Arrays.asList(preds).subList(p + 1, preds.length)) tmp.add(e);
        preds = tmp.finish();
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
      final boolean np = !preds[p].type().mayBeNumber() && !preds[p].has(Flag.FCS);
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
  protected boolean preds(final Item it, final QueryContext ctx) throws QueryException {
    if(preds.length == 0) return true;

    // set context item and position
    final Value cv = ctx.value;
    try {
      Item i = null;
      for(final Expr p : preds) {
        ctx.value = it;
        i = p.test(ctx, info);
        if(i == null) return false;
      }
      // item accepted.. adopt last scoring value
      it.score(i.score());
      return true;
    } finally {
      ctx.value = cv;
    }
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Expr p : preds) {
      if(flag == Flag.FCS && p.type().mayBeNumber() || p.has(flag)) return true;
    }
    return false;
  }

  @Override
  public boolean removable(final Var v) {
    for(final Expr p : preds) if(p.uses(v)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.sum(v, preds);
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    return inlineAll(ctx, scp, preds, v, e) ? optimize(ctx, scp) : null;
  }

  @Override
  public void plan(final FElem plan) {
    for(final Expr p : preds) p.plan(plan);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Expr e : preds) sb.append('[').append(e).append(']');
    return sb.toString();
  }

  /**
   * Copies fields to the given object.
   * @param <T> object type
   * @param p copy
   * @return the copy
   */
  protected <T extends Preds> T copy(final T p) {
    p.last = last;
    p.pos = pos;
    return copyType(p);
  }
}
