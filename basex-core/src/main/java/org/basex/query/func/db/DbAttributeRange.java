package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbAttributeRange extends DbTextRange {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    return attribute(arg(3), data, rangeAccess(data, qc), qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  IndexType type() {
    return IndexType.ATTRIBUTE;
  }
}
