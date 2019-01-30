package org.basex.query.func.jobs;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public class JobsEval extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = toItem(exprs[0], qc);
    return item instanceof Uri ? invoke(item.string(info), qc) : eval(qc, toToken(item), null);
  }

  /**
   * Evaluates the specified URI.
   * @param uri URI
   * @param qc query context
   * @return query string
   * @throws QueryException query exception
   */
  final Str invoke(final byte[] uri, final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final IO io = checkPath(uri);
    try {
      return eval(qc, io.read(), io.url());
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }

  /**
   * Evaluates a query as job.
   * @param qc query context
   * @param query query
   * @param path path (can be {@code null})
   * @return resulting value
   * @throws QueryException query exception
   */
  final Str eval(final QueryContext qc, final byte[] query, final String path)
      throws QueryException {

    checkAdmin(qc);
    final HashMap<String, Value> bindings = toBindings(1, qc);
    final JobsOptions opts = toOptions(2, new JobsOptions(), qc);

    // copy variable values
    final Context ctx = qc.context;
    for(final Entry<String, Value> it : bindings.entrySet()) {
      bindings.put(it.getKey(), QueryJob.materialize(it.getValue().iter(), qc));
    }

    // check if number of maximum queries has been reached
    if(ctx.jobs.active.size() >= JobPool.MAXQUERIES) throw JOBS_OVERFLOW.get(info);

    final String base = opts.get(JobsOptions.BASE_URI);
    final String uri = base != null ? base : path != null ? path : string(sc.baseURI().string());
    opts.set(JobsOptions.BASE_URI, uri);

    final boolean service = opts.contains(JobsOptions.SERVICE) && opts.get(JobsOptions.SERVICE);
    if(service) {
      if(!bindings.isEmpty()) throw JOBS_SERVICE.get(info);
      // invalidate option (not relevant for next steps, i.e., if services are written to disk)
      opts.put(JobsOptions.SERVICE, null);
    }

    final QueryJobSpec spec = new QueryJobSpec(opts, bindings, string(query));
    final QueryJob job = new QueryJob(spec, info, qc.context);

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
