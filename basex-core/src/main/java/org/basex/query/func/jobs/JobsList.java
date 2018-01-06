package org.basex.query.func.jobs;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class JobsList extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    checkAdmin(qc);
    return StrSeq.get(org.basex.core.cmd.JobsList.ids(qc.context));
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
