package org.basex.query.func.repo;

import org.basex.query.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RepoDelete extends RepoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    new RepoManager(qc.context, info).delete(toString(arg(0), qc));
    return Empty.VALUE;
  }
}
