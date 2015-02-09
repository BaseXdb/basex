package org.basex.query.func.index;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class IndexElementNames extends IndexFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return names(qc, IndexType.TAG);
  }

  /**
   * Returns all entries of the specified name index.
   * @param qc query context
   * @param it index type
   * @return text entries
   * @throws QueryException query exception
   */
  Iter names(final QueryContext qc, final IndexType it) throws QueryException {
    final Data data = checkData(qc);
    return entries(it == IndexType.TAG ? data.elemNames : data.attrNames,
      new IndexEntries(EMPTY, it));
  }
}
