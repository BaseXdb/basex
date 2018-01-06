package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.index.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public class DbTextRange extends DbAccess {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return rangeAccess(IndexType.TEXT, qc).iter(qc);
  }

  /**
   * Returns a range index accessor.
   * @param type index type ({@link IndexType#TEXT} or {@link IndexType#ATTRIBUTE})
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  final StringRangeAccess rangeAccess(final IndexType type, final QueryContext qc)
      throws QueryException {

    final Data data = checkData(qc);
    final byte[] min = toToken(exprs[1], qc), max = toToken(exprs[2], qc);
    final StringRange sr = new StringRange(type, min, true, max, true);
    return new StringRangeAccess(info, sr, new IndexStaticDb(info, data));
  }
}
