package org.basex.query.func.jobs;

import static org.basex.query.QueryError.*;

import java.io.*;

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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class JobsStop extends StandardFunc {
  /** Stop options. */
  public static final class StopOptions extends Options {
    /** Stop service. */
    public static final BooleanOption SERVICE = new BooleanOption("service");
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);

    final String id = Token.string(toToken(exprs[0], qc));
    final StopOptions opts = toOptions(1, new StopOptions(), qc);

    // stop job
    org.basex.core.cmd.JobsStop.stop(qc.context, id);

    // remove service
    if(opts.contains(StopOptions.SERVICE) && opts.get(StopOptions.SERVICE)) {
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
}
