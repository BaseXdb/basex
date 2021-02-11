package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple iterative path expression with no root expression and a single step without
 * positional access.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SingleIterPath extends AxisPath {
  /**
   * Constructor.
   * @param info input info
   * @param step axis step
   */
  SingleIterPath(final InputInfo info, final Expr step) {
    super(info, null, step);
  }

  @Override
  protected Iter iterator(final QueryContext qc) throws QueryException {
    return steps[0].iter(qc);
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    return steps[0].value(qc);
  }

  @Override
  public SingleIterPath copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new SingleIterPath(info, steps[0].copy(cc, vm)));
  }
}
