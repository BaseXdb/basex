package org.basex.query.func.index;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class IndexTexts extends IndexFn {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final byte[] prefix = toZeroToken(arg(1), qc);
    final Boolean ascending = toBooleanOrNull(arg(2), qc);

    final IndexEntries entries = ascending == null ? new IndexEntries(prefix, type()) :
      new IndexEntries(prefix, ascending, type());
    return entries(data, entries, this);
  }

  /**
   * Returns the index type (overwritten by implementing functions).
   * @return index type
   */
  IndexType type() {
    return IndexType.TEXT;
  }
}
