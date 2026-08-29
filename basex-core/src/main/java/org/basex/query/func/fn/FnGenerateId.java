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
public final class FnGenerateId extends ContextFn {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final GNode node = toGNodeOrNull(context(qc), qc);
    return node != null ? Str.get(node.id()) : Str.EMPTY;
  }
}
