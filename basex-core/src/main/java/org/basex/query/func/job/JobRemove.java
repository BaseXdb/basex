package org.basex.query.func.job;

import static org.basex.query.QueryError.*;

import java.io.*;

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
public final class JobRemove extends StandardFunc {
  /** Remove options. */
  public static final class RemoveOptions extends Options {
    /** Remove service. */
    public static final BooleanOption SERVICE = new BooleanOption("service", false);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String id = toString(arg(0), qc);
    final RemoveOptions options = toOptions(arg(1), new RemoveOptions(), qc);

    // remove job
    qc.context.jobs.remove(id);
    // remove service
    if(options.get(RemoveOptions.SERVICE)) {
      try {
        qc.context.services.unregister(id);
      } catch(final IOException ex) {
        throw JOBS_SERVICE_X_X.get(info, ex);
      }
    }
    return Empty.VALUE;
  }
}
