package org.basex.query.func.db;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class DbAttribute extends DbText {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return attribute(valueAccess(IndexType.ATTRIBUTE, qc), qc, 2);
  }
}
