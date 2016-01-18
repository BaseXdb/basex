package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public class DbTextRange extends DbAccess {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return rangeAccess(true, qc).iter(qc);
  }

  /**
   * Returns a range index accessor.
   * @param text text/attribute flag
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  final StringRangeAccess rangeAccess(final boolean text, final QueryContext qc)
      throws QueryException {

    final Data data = checkData(qc);
    final byte[] min = toToken(exprs[1], qc);
    final byte[] max = toToken(exprs[2], qc);

    final MetaData meta = data.meta;
    if(!(text ? meta.textindex : meta.attrindex)) throw BXDB_INDEX_X.get(info, meta.name,
        (text ? IndexType.TEXT : IndexType.ATTRIBUTE).toString().toLowerCase(Locale.ENGLISH));

    final StringRange sr = new StringRange(text, min, true, max, true);
    return new StringRangeAccess(info, sr, new IndexContext(data, false));
  }
}
