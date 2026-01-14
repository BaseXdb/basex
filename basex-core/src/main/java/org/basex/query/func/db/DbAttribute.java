package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbAttribute extends DbText {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    return attribute(arg(2), data, valueAccess(data, qc), qc);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return iterValue(qc);
  }

  @Override
  IndexType type() {
    return IndexType.ATTRIBUTE;
  }
}
