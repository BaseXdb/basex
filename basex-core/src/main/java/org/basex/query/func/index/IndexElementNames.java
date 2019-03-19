package org.basex.query.func.index;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public class IndexElementNames extends IndexFn {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return values(qc);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return values(qc).value(qc);
  }

  /**
   * Returns the index type (overwritten by implementing functions).
   * @return index type
   */
  IndexType type() {
    return IndexType.ELEMNAME;
  }

  /**
   * Returns all entries of the specified name index.
   * @param qc query context
   * @return text entries
   * @throws QueryException query exception
   */
  final Iter values(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final IndexType type = type();
    return entries(type == IndexType.ELEMNAME ? data.elemNames : data.attrNames,
      new IndexEntries(EMPTY, type));
  }
}
