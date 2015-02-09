package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DbAttribute extends DbText {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return attribute(valueAccess(false, qc), qc, 2);
  }
}
