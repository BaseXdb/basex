package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheKeys extends CacheFn {
  @Override
  public Value value(final QueryContext qc) {
    return cache(qc).keys();
  }
}
