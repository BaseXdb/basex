package org.basex.query.func.jobs;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class JobsEval extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return eval(toQuery(0, qc), qc);
  }

  /**
   * Evaluates a query as job.
   * @param query query
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  final Str eval(final IOContent query, final QueryContext qc) throws QueryException {
    checkAdmin(qc);
    final HashMap<String, Value> bindings = toBindings(1, qc);
    final JobsOptions opts = toOptions(2, new JobsOptions(), qc);
    opts.set(JobsOptions.BASE_URI, toBaseUri(query.url(), opts));

    final boolean service = Boolean.TRUE.equals(opts.get(JobsOptions.SERVICE));
    if(service) {
      if(!bindings.isEmpty()) throw JOBS_SERVICE.get(info);
      // invalidate option (not relevant for next steps, i.e., if services are written to disk)
      opts.put(JobsOptions.SERVICE, null);
    }

    // copy variable values
    for(final Entry<String, Value> it : bindings.entrySet()) {
      bindings.put(it.getKey(), it.getValue().materialize(qc, BASEX_FUNCTION_X, info));
    }

    final QueryJobSpec spec = new QueryJobSpec(opts, bindings, query);
    final QueryJob job = new QueryJob(spec, qc.context, info, null);

    // add service
    if(service) {
      if(!bindings.isEmpty()) throw JOBS_SERVICE.get(info);
      try {
        final Jobs jobs = new Jobs(qc.context);
        jobs.add(spec);
        jobs.write();
      } catch(final IOException ex) {
        throw JOBS_SERVICE_X_X.get(info, ex);
      }
    }
    return Str.get(job.jc().id());
  }
}
