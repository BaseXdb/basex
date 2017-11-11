package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Pragma extension.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class Extension extends Single {
  /** Pragma. */
  private final Pragma pragma;

  /**
   * Constructor.
   * @param info input info
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
  public Expr optimize(final CompileContext cc) throws QueryException {
    return adoptType(expr);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return qc.iter(value(qc));
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Object state = pragma.init(qc, info);
    try {
      return qc.value(expr);
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
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Extension &&
        pragma.equals(((Extension) obj).pragma) && super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), pragma, expr);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    final Object state = pragma.init(ii.qc, info);
    try {
      return expr.indexAccessible(ii);
    } finally {
      pragma.finish(ii.qc, state);
    }
  }

  @Override
  public String toString() {
    return new StringBuilder().append(pragma).append(' ').append(CURLY1 + ' ').
        append(expr).append(' ').append(CURLY2).toString();
  }
}
