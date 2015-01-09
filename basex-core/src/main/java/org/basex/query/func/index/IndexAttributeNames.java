package org.basex.query.func.index;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class IndexAttributeNames extends IndexElementNames {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return names(qc, IndexType.ATTNAME);
  }
}
