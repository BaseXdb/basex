package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnHasChildren extends ContextFn {
  @Override
  public Bln value(final QueryContext qc) throws QueryException {
    return Bln.get(ebv(qc));
  }

  @Override
  protected boolean test(final QueryContext qc, final long pos) throws QueryException {
    final GNode node = toGNodeOrNull(context(qc), qc);
    return node != null && node.hasChildren();
  }
}
