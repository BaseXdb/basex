package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class StoreList extends StoreFn {
  @Override
  public Value value(final QueryContext qc) {
    return store(qc).list();
  }
}
