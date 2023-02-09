package org.basex.query.func.job;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
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
    final String id = toString(exprs[0], qc);
    final ResultOptions options = toOptions(1, new ResultOptions(), true, qc);

    final Map<String, QueryJobResult> results = qc.context.jobs.results;
    final QueryJobResult result = results.get(id);
    if(result == null) return Empty.VALUE;
    if(result.value == null && result.exception == null) throw JOBS_RUNNING_X.get(info, id);

    try {
      if(result.exception != null) throw result.exception;
      return result.value;
    } finally {
      if(!options.get(ResultOptions.KEEP)) results.remove(id);
    }
  }
}
