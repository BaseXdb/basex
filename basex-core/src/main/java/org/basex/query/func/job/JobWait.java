package org.basex.query.func.job;

import static org.basex.query.QueryError.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobWait extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String id = toString(arg(0), qc);
    if(qc.jc().id().equals(id)) throw JOBS_SELF_X.get(info, id);

    final JobPool pool = qc.context.jobs;
    while(pool.tasks.containsKey(id) || pool.active.containsKey(id)) {
      qc.checkStop();
      pool.awaitChange();
    }
    return Empty.VALUE;
  }
}
