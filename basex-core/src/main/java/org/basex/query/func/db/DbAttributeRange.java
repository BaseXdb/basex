package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DbAttributeRange extends DbTextRange {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return attribute(rangeAccess(false, qc), qc, 3);
  }
}
