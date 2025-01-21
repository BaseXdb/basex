package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple iterative path expression with no root expression and a single step without
 * positional access.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SingleIterPath extends AxisPath {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param step {@link IterStep} instance
   */
  SingleIterPath(final InputInfo info, final Expr step) {
    super(info, null, step);
  }

  @Override
  protected Iter iterator(final QueryContext qc) throws QueryException {
    return qc.focus.value == Empty.VALUE ? Empty.ITER : steps[0].iter(qc);
  }

  @Override
  protected Value nodes(final QueryContext qc) throws QueryException {
    return qc.focus.value == Empty.VALUE ? Empty.VALUE : steps[0].value(qc);
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    return qc.focus.value != Empty.VALUE && steps[0].test(qc, ii, 0);
  }

  @Override
  public SingleIterPath copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new SingleIterPath(info, steps[0].copy(cc, vm)));
  }
}
