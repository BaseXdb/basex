package org.basex.query.func.cache;

import org.basex.query.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheClear extends CacheFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) {
    cache(qc).clear();
    return Empty.VALUE;
  }
}
