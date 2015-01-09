package org.basex.query.func.db;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DbAdd extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final byte[] path = exprs.length < 3 ? Token.EMPTY : token(path(2, qc));
    final NewInput input = checkInput(toItem(exprs[1], qc), path);
    final Options opts = toOptions(3, Q_OPTIONS, new Options(), qc);
    qc.resources.updates().add(new DBAdd(data, input, opts, qc, info), qc);
    return null;
  }
}
