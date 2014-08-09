package org.basex.query.ft;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class defines is an abstract class for full-text expressions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class FTExpr extends ParseExpr {
  /** Expressions. */
  public final FTExpr[] exprs;

  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  FTExpr(final InputInfo info, final FTExpr... exprs) {
    super(info);
    this.exprs = exprs;
    seqType = SeqType.BLN;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(exprs);
  }

  @Override
  public FTExpr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    final int es = exprs.length;
    for(int e = 0; e < es; e++) exprs[e] = exprs[e].compile(qc, scp);
    return this;
  }

  @Override
  public FTExpr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    return this;
  }

  /**
   * This method is called by the sequential full-text evaluation.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException;

  /**
   * This method is called by the index-based full-text evaluation.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTIter iter(final QueryContext qc) throws QueryException;

  @Override
  public boolean has(final Flag flag) {
    for(final FTExpr e : exprs) if(e.has(flag)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var var) {
    for(final Expr e : exprs) if(!e.removable(var)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, exprs);
  }

  @Override
  public FTExpr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    return inlineAll(qc, scp, exprs, var, ex) ? optimize(qc, scp) : null;
  }

  @Override
  public abstract FTExpr copy(QueryContext qc, VarScope scp, IntObjMap<Var> vs);

  /**
   * Checks if sub expressions of a mild not operator violate the grammar.
   * @return result of check
   */
  boolean usesExclude() {
    for(final FTExpr e : exprs) if(e.usesExclude()) return true;
    return false;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), exprs);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, exprs);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : exprs) sz += e.exprSize();
    return sz;
  }

  /**
   * Prints the array with the specified separator.
   * @param sep separator
   * @return string representation
   */
  final String toString(final Object sep) {
    final StringBuilder sb = new StringBuilder();
    final int es = exprs.length;
    for(int e = 0; e < es; e++) sb.append(e == 0 ? "" : sep.toString()).append(exprs[e]);
    return sb.toString();
  }
}
