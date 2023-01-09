package org.basex.query.func.job;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class JobFinished extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String id = toString(exprs[0], qc);
    final JobPool pool = qc.context.jobs;
    final QueryJobResult result = pool.results.get(id);
    // returns true if job is not active, and if no result exists or if it has been cached
    return Bln.get(!pool.active.containsKey(id) && (result == null || result.cached()));
  }
}
