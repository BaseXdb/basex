package org.basex.query.func.index;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class IndexTexts extends IndexFn {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final byte[] prefix = toZeroToken(arg(1), qc);
    final Boolean ascending = defined(2) ? toBoolean(arg(2), qc) : null;

    final IndexEntries entries = ascending != null ? new IndexEntries(prefix, ascending, type()) :
      new IndexEntries(prefix, type());
    return entries(data, entries, this);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  /**
   * Returns the index type (overwritten by implementing functions).
   * @return index type
   */
  IndexType type() {
    return IndexType.TEXT;
  }
}
