package org.basex.query.func.job;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class JobServices extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      return new Jobs(qc.context).toXML().childIter().value(qc, this);
    } catch(final IOException ex) {
      throw JOBS_SERVICE_X_X.get(info, ex);
    }
  }
}
