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
public final class JobsStop extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);

    final String id = Token.string(toToken(exprs[0], qc));
    final JobPool pool = qc.context.jobs;
    // send stop signal
    final Job job = pool.jobs.get(id);
    if(job != null) job.stop();
    // remove potentially cached result
    pool.results.remove(id);
    return null;
  }
}
