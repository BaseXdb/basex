package org.basex.query.func.db;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class DbToken extends DbText {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return attribute(valueAccess(qc), qc, 2);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return refinedValue(qc);
  }

  @Override
  IndexType type() {
    return IndexType.TOKEN;
  }
}
