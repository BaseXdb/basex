package org.basex.query.func.index;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class IndexTexts extends IndexFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return values(qc, IndexType.TEXT);
  }

  /**
   * Returns all entries of the specified value index.
   * @param qc query context
   * @param it index type
   * @return text entries
   * @throws QueryException query exception
   */
  final Iter values(final QueryContext qc, final IndexType it) throws QueryException {
    final Data data = checkData(qc);
    final byte[] entry = exprs.length < 2 ? EMPTY : toToken(exprs[1], qc);
    if(data.inMemory()) throw BXDB_MEM_X.get(info, data.meta.name);

    final IndexEntries et = exprs.length < 3 ? new IndexEntries(entry, it) :
      new IndexEntries(entry, toBoolean(exprs[2], qc), it);
    return entries(data, et, this);
  }
}
