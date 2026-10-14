package org.basex.query.expr.ft;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This is an abstract class for full-text expressions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class FTExpr extends ParseExpr {
  /** Expressions. */
  public final FTExpr[] exprs;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  FTExpr(final InputInfo info, final FTExpr... exprs) {
    super(info, Types.BOOLEAN_O);
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
   * Adds the stop words of the queried database to requested default stop words.
   * @param data data reference
   */
  void stopWords(final Data data) {
    for(final FTExpr expr : exprs) expr.stopWords(data);
  }

  /**
   * This method is called by the index-based full-text evaluation.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  @Override
  public abstract FTIter iter(QueryContext qc) throws QueryException;

  /**
   * This method is called by the sequential full-text evaluation.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  @Override
  public abstract FTNode value(QueryContext qc) throws QueryException;

  @Override
  public boolean has(final Flag... flags) {
    for(final FTExpr expr : exprs) {
      if(expr.has(flags)) return true;
    }
    return false;
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    for(final Expr expr : exprs) {
      if(!expr.inlineable(ic)) return false;
    }
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, exprs);
  }

  @Override
  public FTExpr inline(final InlineContext ic) throws QueryException {
    return ic.inline(exprs) ? optimize(ic.cc) : null;
  }

  @Override
  public abstract FTExpr copy(CompileContext cc, IntObjectMap<Var> vm);

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
    return this == obj || obj != null && getClass() == obj.getClass() &&
        Arrays.equals(exprs, ((FTExpr) obj).exprs);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), exprs);
  }

  /**
   * Adds a full-text range to the query string.
   * @param qs query string
   * @param min minimum
   * @param max maximum
   * @param dflt default minimum
   */
  static void range(final QueryString qs, final Expr min, final Expr max, final Itr dflt) {
    if(min.equals(max)) qs.token(QueryText.EXACTLY).token(min);
    else if(max.equals(Itr.MAX)) qs.token(QueryText.AT).token(QueryText.LEAST).token(min);
    else if(min.equals(dflt)) qs.token(QueryText.AT).token(QueryText.MOST).token(max);
    else qs.token(QueryText.FROM).token(min).token(QueryText.TO).token(max);
  }
}
