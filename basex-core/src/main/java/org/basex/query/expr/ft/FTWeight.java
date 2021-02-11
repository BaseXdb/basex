package org.basex.query.expr.ft;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.index.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * FTOptions expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTWeight extends FTExpr {
  /** Weight. */
  private Expr weight;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param weight weight
   */
  public FTWeight(final InputInfo info, final FTExpr expr, final Expr weight) {
    super(info, expr);
    this.weight = weight;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(weight);
    super.checkUp();
  }

  @Override
  public FTExpr compile(final CompileContext cc) throws QueryException {
    weight = weight.compile(cc);
    return super.compile(cc).optimize(cc);
  }

  @Override
  public FTExpr optimize(final CompileContext cc) throws QueryException {
    weight = weight.simplifyFor(Simplify.NUMBER, cc);
    return this;
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return weight(exprs[0].item(qc, info), qc);
  }

  @Override
  public FTIter iter(final QueryContext qc) {
    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        final FTNode node = exprs[0].iter(qc).next();
        return node == null ? null : weight(node, qc);
      }
    };
  }

  /**
   * Returns the item with an enriched score value.
   * @param item input item
   * @param qc query context
   * @return item
   * @throws QueryException query exception
   */
  private FTNode weight(final FTNode item, final QueryContext qc) throws QueryException {
    // evaluate weight
    final double d = toDouble(weight, qc);
    if(Math.abs(d) > 1000) throw FTWEIGHT_X.get(info, d);
    if(d == 0) item.matches().reset();
    item.score(item.score() * d);
    return item;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) {
    // weight makes no sense as long as no index-based scoring exists
    return false;
  }

  @Override
  public boolean has(final Flag... flags) {
    return weight.has(flags) || super.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return weight.inlineable(ic) && super.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return weight.count(var).plus(super.count(var));
  }

  @Override
  public FTExpr inline(final InlineContext ic) throws QueryException {
    boolean changed = ic.inline(exprs);
    final Expr inlined = weight.inline(ic);
    if(inlined != null) {
      weight = inlined;
      changed = true;
    }
    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new FTWeight(info, exprs[0].copy(cc, vm), weight.copy(cc, vm)));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && weight.accept(visitor);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final FTExpr expr : exprs) size += expr.exprSize();
    return size + weight.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTWeight && weight.equals(((FTWeight) obj).weight) &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), weight, exprs[0]);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(exprs[0]).token(WEIGHT).brace(weight);
  }
}
