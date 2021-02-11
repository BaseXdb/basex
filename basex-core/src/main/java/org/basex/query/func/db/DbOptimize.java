package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbOptimize extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final boolean all = exprs.length > 1 && toBoolean(exprs[1], qc);
    final Options opts = toOptions(2, new Options(), qc);
    qc.updates().add(new DBOptimize(data, all, opts, qc, info), qc);
    return Empty.VALUE;
  }
}
