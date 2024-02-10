package org.basex.query.func.job;

import static org.basex.query.QueryError.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class JobWait extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String id = toString(arg(0), qc);
    if(qc.jc().id().equals(id)) throw JOBS_SELF_X.get(info, id);

    final JobPool pool = qc.context.jobs;
    while(pool.tasks.containsKey(id) || pool.active.containsKey(id)) {
      Performance.sleep(1);
      qc.checkStop();
    }
    return Empty.VALUE;
  }
}
