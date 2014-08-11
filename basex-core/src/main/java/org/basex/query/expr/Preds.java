package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.path.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Abstract predicate expression, implemented by {@link Filter} and {@link Step}.
 *
 * @author BaseX Team 2005-14, BSD License
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
   * @param info input info
   * @param preds predicates
   */
  protected Preds(final InputInfo info, final Expr[] preds) {
    super(info);
    this.preds = preds;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(preds);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    final int pl = preds.length;
    for(int p = 0; p < pl; ++p) preds[p] = preds[p].compile(qc, scp).compEbv(qc);
    return optimize(qc, scp);
  }

  /**
   * Prepares this expression for iterative evaluation. The expression can be iteratively
   * evaluated if no predicate or only the first is positional.
   * @return result of check
   */
  protected final boolean posIterator() {
    // check if first predicate is numeric
    if(preds.length == 1) {
      if(preds[0] instanceof Int) {
        final long p = ((Int) preds[0]).itr();
        preds[0] = Pos.get(p, p, info);
      }
      pos = preds[0] instanceof Pos ? (Pos) preds[0] : null;
      last = preds[0].isFunction(Function.LAST);
    }
    return pos != null || last;
  }

  /**
   * Checks if the predicates are successful for the specified item.
   * @param it item to be checked
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  protected final boolean preds(final Item it, final QueryContext qc) throws QueryException {
    if(preds.length == 0) return true;

    // set context value and position
    final Value cv = qc.value;
    try {
      for(final Expr p : preds) {
        qc.value = it;
        final Item i = p.test(qc, info);
        if(i == null) return false;
        it.score(i.score());
      }
      return true;
    } finally {
      qc.value = cv;
    }
  }

  @Override
  public boolean has(final Flag flag) {
    for(final Expr p : preds) {
      if(flag == Flag.FCS && p.seqType().mayBeNumber() || p.has(flag)) return true;
    }
    return false;
  }

  @Override
  public boolean removable(final Var var) {
    for(final Expr p : preds) if(p.uses(var)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, preds);
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    return inlineAll(qc, scp, preds, var, ex) ? optimize(qc, scp) : null;
  }

  /**
   * Copies fields to the given object.
   * @param <T> object type
   * @param p copy
   * @return the copy
   */
  protected final <T extends Preds> T copy(final T p) {
    p.last = last;
    p.pos = pos;
    return copyType(p);
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
}
