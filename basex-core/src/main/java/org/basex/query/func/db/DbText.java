package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class DbText extends DbAccess {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return valueAccess(true, qc).iter(qc);
  }

  /**
   * Returns an index accessor.
   * @param text text/attribute flag
   * @param qc query context
   * @return index accessor
   * @throws QueryException query exception
   */
  final ValueAccess valueAccess(final boolean text, final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final MetaData meta = data.meta;
    if(!(text ? meta.textindex : meta.attrindex)) throw BXDB_INDEX_X.get(info, meta.name,
        (text ? IndexType.TEXT : IndexType.ATTRIBUTE).toString().toLowerCase(Locale.ENGLISH));

    return new ValueAccess(info, exprs[1], text, false, null, new IndexContext(data, false));
  }
}
