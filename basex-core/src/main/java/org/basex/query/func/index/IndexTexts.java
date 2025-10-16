package org.basex.query.func.index;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

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
    final Item ascending = arg(2).atomItem(qc, info);

    final IndexEntries entries = ascending.isEmpty() ? new IndexEntries(prefix, type()) :
      new IndexEntries(prefix, toBoolean(ascending), type());
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
