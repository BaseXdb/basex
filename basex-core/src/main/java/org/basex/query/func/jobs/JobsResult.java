package org.basex.query.func.jobs;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobsResult extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String id = Token.string(toToken(exprs[0], qc));
    final JobPool jobs = qc.context.jobs;

    final Map<String, JobResult> results = jobs.results;
    final JobResult result = results.get(id);
    if(result == null) throw JOBS_UNKNOWN_X.get(info, id);
    if(result.value == null && result.exception == null) throw JOBS_RUNNING_X.get(info, id);

    try {
      if(result.value != null) return result.value;
      throw result.exception;
    } finally {
      results.remove(id);
    }
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
