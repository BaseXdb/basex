package org.basex.query.expr.gflwor;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * FLWOR clause that filters tuples with a predicate.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract sealed class FilterClause extends SingleClause permits Where, While {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr predicate expression
   */
  FilterClause(final InputInfo info, final Expr expr) {
    super(info, Types.BOOLEAN_O, expr);
  }

  @Override
  public final FilterClause optimize(final CompileContext cc) throws QueryException {
    // where exists($nodes) → where $nodes
    expr = expr.simplifyFor(Simplify.EBV, cc);
    if(expr instanceof Value && !(expr instanceof Bln)) {
      expr = cc.replaceWith(expr, Bln.get(expr.ebv(cc.qc, info)));
    }
    return this;
  }

  @Override
  final boolean skippable(final Clause cl) {
    // do not slide LET clauses over filters (they may filter out many items)
    return !(cl instanceof Let);
  }

  @Override
  public final void calcSize(final long[] minMax) {
    minMax[0] = 0;
    if(expr == Bln.FALSE) minMax[1] = 0;
  }
}
