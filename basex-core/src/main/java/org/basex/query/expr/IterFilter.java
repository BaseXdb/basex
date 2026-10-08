package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative filter expression without numeric predicates.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IterFilter extends Filter {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param preds predicate expressions
   */
  IterFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, root, preds);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return filterIter(root.iter(qc), qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return filterValue(root.iter(qc), qc);
  }

  @Override
  public IterFilter copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new IterFilter(info, root.copy(cc, vm), copyAll(cc, vm, exprs)));
  }
}
