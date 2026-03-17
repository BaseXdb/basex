package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnJposition extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final JNode jnode = toJNodeOrNull(context(qc), qc);
    return jnode != null && jnode.position != 0 ? Itr.get(jnode.position) : Empty.VALUE;
  }
}
