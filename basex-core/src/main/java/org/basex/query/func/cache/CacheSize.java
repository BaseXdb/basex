package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheSize extends CacheFn {
  @Override
  public Itr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toZeroString(arg(0), qc);

    return Itr.get(caches(qc).size(name));
  }
}
