package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnJposition extends ContextFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final JNode jnode = toJNodeOrNull(context(qc), qc);
    return jnode != null && jnode.position != 0 ? Itr.get(jnode.position) : Empty.VALUE;
  }
}
