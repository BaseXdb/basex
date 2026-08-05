package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheKeys extends CacheFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String name = toZeroString(arg(0), qc);

    return StrSeq.get(caches(qc).keys(name));
  }
}
