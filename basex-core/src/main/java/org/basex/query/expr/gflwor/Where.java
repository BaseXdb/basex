package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * GFLWOR {@code where} clause, filtering tuples not satisfying the predicate.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class Where extends Clause {
  /** Predicate expression. */
  Expr expr;

  /**
   * Constructor.
   * @param expr predicate expression
   * @param info input info
   */
  public Where(final Expr expr, final InputInfo info) {
    super(info, SeqType.BOOLEAN_O);
    this.expr = expr;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        while(sub.next(qc)) {
          if(expr.ebv(qc, info).bool(info)) return true;
        }
        return false;
      }
    };
  }

  @Override
  public boolean has(final Flag... flags) {
    return expr.has(flags);
  }

  @Override
  public Where compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    return optimize(cc);
  }

  @Override
  public Where optimize(final CompileContext cc) throws QueryException {
    // where exists(nodes)  ->  where nodes
    expr = expr.simplifyFor(Simplify.EBV, cc);
    if(expr instanceof Value && !(expr instanceof Bln)) {
      expr = cc.replaceWith(expr, Bln.get(expr.ebv(cc.qc, info).bool(info)));
    }
    return this;
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return expr.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return expr.count(var);
  }

  @Override
  public Clause inline(final InlineContext ic) throws QueryException {
    final Expr inlined = expr.inline(ic);
    if(inlined == null) return null;
    expr = inlined;
    return optimize(ic.cc);
  }

  @Override
  public Where copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Where(expr.copy(cc, vm), info));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor);
  }

  @Override
  boolean skippable(final Clause cl) {
    // do not slide LET clauses over WHERE (WHERE may filter out many items)
    return !(cl instanceof Let);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public void calcSize(final long[] minMax) {
    minMax[0] = 0;
    if(expr == Bln.FALSE) minMax[1] = 0;
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Where && expr.equals(((Where) obj).expr);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), expr);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(WHERE).token(expr);
  }
}
