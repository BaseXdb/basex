package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoreClear extends StoreFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) {
    stores(qc).clear();
    return Empty.VALUE;
  }
}
