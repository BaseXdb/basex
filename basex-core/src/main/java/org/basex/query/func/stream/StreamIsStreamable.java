package org.basex.query.func.stream;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class StreamIsStreamable extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item it = toAtomItem(exprs[0], qc);
    return Bln.get(it instanceof StrStream || it instanceof B64Stream);
  }
}
