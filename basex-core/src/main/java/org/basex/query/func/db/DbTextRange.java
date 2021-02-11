package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class DbTextRange extends DbAccess {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return rangeAccess(qc).iter(qc);
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return rangeAccess(qc).value(qc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    return compileData(cc);
  }

  /**
   * Returns the index type (overwritten by implementing functions).
   * @return index type
   */
  IndexType type() {
    return IndexType.TEXT;
  }

  /**
   * Returns a range index accessor.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  final StringRangeAccess rangeAccess(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final byte[] min = toToken(exprs[1], qc), max = toToken(exprs[2], qc);
    final StringRange sr = new StringRange(type(), min, true, max, true);
    return new StringRangeAccess(info, sr, new IndexStaticDb(data, info));
  }
}
