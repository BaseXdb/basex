package org.basex.query.func.job;

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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class JobRemove extends StandardFunc {
  /** Remove options. */
  public static final class RemoveOptions extends Options {
    /** Remove service. */
    public static final BooleanOption SERVICE = new BooleanOption("service", false);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String id = toString(exprs[0], qc);
    final RemoveOptions opts = toOptions(1, new RemoveOptions(), qc);

    // remove job
    qc.context.jobs.remove(id);
    // remove service
    if(opts.get(RemoveOptions.SERVICE)) {
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
