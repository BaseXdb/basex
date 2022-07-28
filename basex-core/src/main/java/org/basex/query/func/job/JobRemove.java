package org.basex.query.func.job;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class JobRemove extends StandardFunc {
  /** Stop options. */
  public static final class RemoveOptions extends Options {
    /** Stop service. */
    public static final BooleanOption SERVICE = new BooleanOption("service");
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String id = toString(exprs[0], qc);
    final RemoveOptions opts = toOptions(1, new RemoveOptions(), qc);

    // stop job
    remove(qc.context, id);

    // remove service
    if(opts.contains(RemoveOptions.SERVICE) && opts.get(RemoveOptions.SERVICE)) {
      try {
        final Jobs jobs = new Jobs(qc.context);
        jobs.remove(id);
        jobs.write();
      } catch(final IOException ex) {
        throw JOBS_SERVICE_X_X.get(info, ex);
      }
    }
    return Empty.VALUE;
  }

  /**
   * Stops the specified process.
   * @param ctx context
   * @param id id
   * @return return success flag
   */
  public static boolean remove(final Context ctx, final String id) {
    // stop scheduled task
    final JobPool jobs = ctx.jobs;
    final TimerTask task = jobs.tasks.remove(id);
    if(task != null) task.cancel();
    // send stop signal to job
    final Job job = jobs.active.get(id);
    if(job != null) job.stop();
    // remove potentially cached result
    jobs.results.remove(id);

    return job != null || task != null;
  }
}
