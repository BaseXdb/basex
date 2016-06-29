package org.basex.query.func.jobs;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobsFinished extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);

    final String id = Token.string(toToken(exprs[0], qc));
    final JobPool pool = qc.context.jobs;
    final JobResult result = pool.results.get(id);
    // returns true if job is unknown and if no result exists, or it has been finished
    return Bln.get(!pool.jobs.containsKey(id) && (result == null || result.finished()));
  }
}
