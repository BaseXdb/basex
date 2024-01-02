package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.index.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Pragma extension.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class Extension extends Single {
  /** Pragma. */
  private final Pragma pragma;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param pragma pragma
   * @param expr enclosed expression
   */
  public Extension(final InputInfo info, final Pragma pragma, final Expr expr) {
    super(info, expr, SeqType.ITEM_ZM);
    this.pragma = pragma;
  }

  @Override
  public void checkUp() throws QueryException {
    expr.checkUp();
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final Object state = pragma.init(cc.qc, info);
    try {
      expr = expr.compile(cc);
    } finally {
      pragma.finish(cc.qc, state);
    }
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    return pragma.simplify() && expr instanceof Value &&
        !expr.seqType().mayBeFunction() ? expr : adoptType(expr);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Object state = pragma.init(qc, info);
    try {
      return expr.value(qc);
    } finally {
      pragma.finish(qc, state);
    }
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Extension(info, pragma.copy(), expr.copy(cc, vm)));
  }

  @Override
  public boolean has(final Flag... flags) {
    return pragma.has(flags) || super.has(flags);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    pragma.accept(visitor);
    return super.accept(visitor);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    final QueryContext qc = ii.cc.qc;
    final Object state = pragma.init(qc, info);
    try {
      return expr.indexAccessible(ii);
    } finally {
      pragma.finish(qc, state);
    }
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Extension &&
        pragma.equals(((Extension) obj).pragma) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), pragma, expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(pragma).brace(expr);
  }
}
