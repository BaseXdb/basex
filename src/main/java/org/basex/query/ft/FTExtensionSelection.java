package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * FTExtensionSelection expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FTExtensionSelection extends FTExpr {
  /** Pragmas. */
  private final Pragma[] pragmas;

  /**
   * Constructor.
   * @param ii input info
   * @param prag pragmas
   * @param e enclosed FTSelection
   */
  public FTExtensionSelection(final InputInfo ii, final Pragma[] prag, final FTExpr e) {
    super(ii, e);
    pragmas = prag;
  }

  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return expr[0].item(ctx, info);
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    return expr[0].iter(ctx);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), pragmas, expr);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Pragma p : pragmas) sb.append(p).append(' ');
    return sb.append(BRACE1 + ' ' + expr[0] + ' ' + BRACE2).toString();
  }
}
