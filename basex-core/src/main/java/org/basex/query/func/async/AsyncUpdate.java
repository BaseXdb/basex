package org.basex.query.func.async;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class AsyncUpdate extends AsyncEval {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return item(qc, true);
  }
}
