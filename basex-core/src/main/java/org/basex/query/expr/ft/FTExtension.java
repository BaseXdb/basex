package org.basex.query.expr.ft;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * FTExtensionSelection expression.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class FTExtension extends FTExpr {
  /** Pragma. */
  private final Pragma pragma;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param pragma pragma
   * @param expr enclosed FTSelection
   */
  public FTExtension(final InputInfo info, final Pragma pragma, final FTExpr expr) {
    super(info, expr);
    this.pragma = pragma;
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return exprs[0].item(qc, info);
  }

  @Override
  public FTIter iter(final QueryContext qc) throws QueryException {
    return exprs[0].iter(qc);
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new FTExtension(info, pragma.copy(), exprs[0].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final FTExtension fte && pragma.equals(fte.pragma) &&
        super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), pragma, exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(pragma).brace(exprs[0]);
  }
}
