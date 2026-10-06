package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative expression for paths that return nodes in distinct document order.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IterPath extends AxisPath {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param steps axis steps
   */
  IterPath(final InputInfo info, final Expr root, final Expr... steps) {
    super(info, root, steps);
  }

  @Override
  protected Iter iterator(final QueryContext qc) {
    return lazyIter(qc);
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    return iterator(qc).value(qc, this);
  }

  @Override
  public IterPath copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Expr rt = root == null ? null : root.copy(cc, vm);
    return copyType(new IterPath(info, rt, Arr.copyAll(cc, vm, steps)));
  }
}
