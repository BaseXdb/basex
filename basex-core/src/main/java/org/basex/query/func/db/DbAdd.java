package org.basex.query.func.db;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
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
public final class DbAdd extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    byte[] path = null;
    if(exprs.length > 2) {
      path = toTokenOrNull(exprs[2], qc);
      if(path != null) path = token(path(path));
    }
    final NewInput input = checkInput(toNodeOrAtomItem(1, qc), path == null ? EMPTY : path);
    final Options opts = toOptions(3, new Options(), qc);

    qc.updates().add(new DBAdd(data, input, opts, false, qc, info), qc);
    return Empty.VALUE;
  }
}
