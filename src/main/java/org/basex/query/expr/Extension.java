package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Pragma extension.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Extension extends Single {
  /** Pragmas of the ExtensionExpression. */
  private final Pragma[] pragmas;

  /**
   * Constructor.
   * @param ii input info
   * @param prag pragmas
   * @param e enclosed expression
   */
  public Extension(final InputInfo ii, final Pragma[] prag, final Expr e) {
    super(ii, e);
    pragmas = prag;
  }

  @Override
  public void checkUp() throws QueryException {
    expr.checkUp();
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    // ignore pragma
    return optPre(expr.compile(ctx), ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // currently, will never be called, as compilation step returns argument
    return ctx.iter(expr);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), pragmas, expr);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Pragma p : pragmas) sb.append(p).append(' ');
    return sb.append(BRACE1 + ' ' + expr + ' ' + BRACE2).toString();
  }

  @Override
  public Expr markTailCalls() {
    expr = expr.markTailCalls();
    return this;
  }
}
