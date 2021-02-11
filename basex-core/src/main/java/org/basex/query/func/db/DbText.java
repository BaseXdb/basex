package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class DbText extends DbAccess {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    return valueAccess(data, qc).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    return valueAccess(data, qc).value(qc);
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
   * Returns an index accessor.
   * @param data data reference
   * @param qc query context
   * @return index accessor
   * @throws QueryException query exception
   */
  final ValueAccess valueAccess(final Data data, final QueryContext qc) throws QueryException {
    final TokenSet set = new TokenSet();
    final Iter iter = exprs[1].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) set.put(toToken(item));
    return new ValueAccess(info, set, type(), null, new IndexStaticDb(data, info));
  }
}
