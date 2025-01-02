package org.basex.query.func.job;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobList extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) {
    return StrSeq.get(qc.context.jobs.ids());
  }
}
