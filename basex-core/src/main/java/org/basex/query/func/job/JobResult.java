package org.basex.query.func.job;

import static org.basex.query.QueryError.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobResult extends StandardFunc {
  /** Result options. */
  public static final class ResultOptions extends Options {
    /** Keep result. */
    public static final BooleanOption KEEP = new BooleanOption("keep", false);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String id = toString(arg(0), qc);
    final ResultOptions options = toOptions(qc);

    final JobPool jobs = qc.context.jobs;
    final QueryJobResult result = jobs.results.get(id);
    if(result == null) return Empty.VALUE;
    if(result.value == null && result.exception == null) throw JOBS_RUNNING_X.get(info, id);
    if(!options.get(ResultOptions.KEEP)) jobs.removeResult(id);
    return result.get();
  }
}
