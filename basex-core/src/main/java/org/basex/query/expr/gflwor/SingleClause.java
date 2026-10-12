package org.basex.query.expr.gflwor;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * FLWOR clause with a single expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract sealed class SingleClause extends Clause permits ForLet, FilterClause, Trace {
  /** Expression. */
  public Expr expr;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param seqType sequence type
   * @param expr expression
   * @param vars declared variables
   */
  SingleClause(final InputInfo info, final SeqType seqType, final Expr expr, final Var... vars) {
    super(info, seqType, vars);
    this.expr = expr;
  }

  @Override
  public final Clause compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    return optimize(cc);
  }

  @Override
  public boolean has(final Flag... flags) {
    return expr.has(flags);
  }

  @Override
  public final boolean inlineable(final InlineContext ic) {
    return expr.inlineable(ic);
  }

  @Override
  public final VarUsage count(final Var var) {
    return expr.count(var);
  }

  @Override
  public final Clause inline(final InlineContext ic) throws QueryException {
    final Expr inlined = expr.inline(ic);
    if(inlined == null) return null;
    expr = inlined;
    return optimize(ic.cc);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public final int exprSize() {
    return expr.exprSize();
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), expr);
  }
}
