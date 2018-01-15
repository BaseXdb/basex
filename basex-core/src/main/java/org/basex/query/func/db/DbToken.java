package org.basex.query.func.db;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class DbToken extends DbText {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return attribute(valueAccess(IndexType.TOKEN, qc), qc, 2);
  }
}
