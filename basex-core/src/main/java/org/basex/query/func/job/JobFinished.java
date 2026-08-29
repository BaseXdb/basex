package org.basex.query.func.job;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobFinished extends StandardFunc {
  @Override
  public Bln value(final QueryContext qc) throws QueryException {
    return Bln.get(ebv(qc));
  }

  @Override
  protected boolean test(final QueryContext qc, final long pos) throws QueryException {
    final String id = toString(arg(0), qc);
    final JobPool pool = qc.context.jobs;
    final QueryJobResult result = pool.results.get(id);
    // returns true if job is not active, and if no result exists or if it has been cached
    return !pool.active.containsKey(id) && (result == null || result.cached());
  }
}
