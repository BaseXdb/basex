package org.basex.query.func.job;

import static org.basex.query.QueryError.*;

import java.util.*;

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
public final class JobInfo extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String id = toString(arg(0), qc);

    final Map<String, QueryJobResult> results = qc.context.jobs.results;
    final QueryJobResult result = results.get(id);
    if(result == null) return Empty.VALUE;
    if(result.value == null && result.exception == null) throw JOBS_RUNNING_X.get(info, id);
    return result.info != null ? result.info : Empty.VALUE;
  }
}
