package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CachePut extends CacheFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] key = toToken(arg(0), qc);
    final Value value = arg(1).value(qc);
    final Item expires = arg(2).atomItem(qc, info);

    cache(key, value, expires, qc);
    return Empty.VALUE;
  }
}
