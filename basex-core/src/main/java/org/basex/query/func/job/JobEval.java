package org.basex.query.func.job;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class JobEval extends StandardFunc {
  /** Eval options. */
  public static final class EvalOptions extends JobOptions {
    /** Register as service. */
    public static final BooleanOption SERVICE = new BooleanOption("service");
  }

  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    return eval(toOptions(arg(2), new EvalOptions(), qc), qc);
  }

  /**
   * Evaluates a query or function as job.
   * @param options job options
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  final Str eval(final EvalOptions options, final QueryContext qc) throws QueryException {
    final boolean service = options.get(EvalOptions.SERVICE) == Boolean.TRUE;
    final QueryJobSpec spec = toJobSpec(arg(0), arg(1), options, service, qc);
    // invalidate option (not relevant for next steps, i.e., if services are written to disk)
    if(service) options.put(EvalOptions.SERVICE, null);

    // synchronous jobs share the caller's context
    final boolean sync = synchronous();
    final Locks held = sync ? qc.context.locking.held() : null;
    final QueryJob job = new QueryJob(spec, sync ? qc.context : qc.context.detach(), info, null,
        held);

    // add service
    if(service) {
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

  /**
   * Indicates if the calling query waits for the job result and is blocked until the job finishes.
   * Overridden by {@link JobExecute}.
   * @return result of check
   */
  boolean synchronous() {
    return false;
  }
}
