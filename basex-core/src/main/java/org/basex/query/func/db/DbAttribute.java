package org.basex.query.func.db;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public class DbAttribute extends DbText {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return attribute(valueAccess(qc), qc, 2);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  IndexType type() {
    return IndexType.ATTRIBUTE;
  }
}
