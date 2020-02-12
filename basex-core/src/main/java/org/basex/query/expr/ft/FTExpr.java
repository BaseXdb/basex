package org.basex.query.expr.ft;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class defines is an abstract class for full-text expressions.
 *
 * @author BaseX Team 2005-20, BSD License
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
    super(info, SeqType.BLN_O);
    this.exprs = exprs;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(exprs);
  }

  @Override
  public FTExpr compile(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el; e++) exprs[e] = exprs[e].compile(cc);
    return this;
  }

  @Override
  public FTExpr optimize(final CompileContext cc) throws QueryException {
    return this;
  }

  /**
   * This method is called by the index-based full-text evaluation.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTIter iter(QueryContext qc) throws QueryException;

  @Override
  public Value value(final QueryContext qc) {
    // will never be called
    throw Util.notExpected();
  }

  /**
   * This method is called by the sequential full-text evaluation. It always returns an item.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTNode item(QueryContext qc, InputInfo ii) throws QueryException;

  @Override
  public boolean has(final Flag... flags) {
    for(final FTExpr expr : exprs) {
      if(expr.has(flags)) return true;
    }
    return false;
  }

  @Override
  public boolean inlineable(final Var var) {
    for(final Expr expr : exprs) {
      if(!expr.inlineable(var)) return false;
    }
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, exprs);
  }

  @Override
  public FTExpr inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {
    return inlineAll(var, ex, exprs, cc) ? optimize(cc) : null;
  }

  @Override
  public abstract FTExpr copy(CompileContext cc, IntObjMap<Var> vm);

  /**
   * Checks if sub expressions of a mild not operator violate the grammar.
   * @return result of check
   */
  public boolean usesExclude() {
    for(final FTExpr expr : exprs) {
      if(expr.usesExclude()) return true;
    }
    return false;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, exprs);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof FTExpr && Array.equals(exprs, ((FTExpr) obj).exprs);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), exprs);
  }

  /**
   * Prints the array with the specified separator.
   * @param sep separator
   * @return string representation
   */
  final String toString(final Object sep) {
    final StringBuilder sb = new StringBuilder();
    final int el = exprs.length;
    for(int e = 0; e < el; e++) sb.append(e == 0 ? "" : sep.toString()).append(exprs[e]);
    return sb.toString();
  }
}
