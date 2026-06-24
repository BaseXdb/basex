package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Step expression, caching all results.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CachedStep extends Step {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  public CachedStep(final InputInfo info, final Axis axis, final Test test, final Expr... preds) {
    super(info, axis, test, preds);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // evaluate step
    final GNodeList list = new GNodeList();
    for(final GNode node : iterator(qc)) list.add(node);
    // evaluate predicates
    return preds(list, qc);
  }

  @Override
  public Step copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CachedStep(info, axis, test.copy(), copyAll(cc, vm, exprs)));
  }
}
