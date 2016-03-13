package org.basex.query.func.db;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class DbAttributeRange extends DbTextRange {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return attribute(rangeAccess(IndexType.ATTRIBUTE, qc), qc, 3);
  }
}
