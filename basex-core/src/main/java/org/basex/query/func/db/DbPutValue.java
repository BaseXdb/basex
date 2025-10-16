package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbPutValue extends DbPutBinary {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final Value input = arg(1).value(qc);
    final String path = toDbPath(arg(2), qc);
    return put(data, path, new DBPut(data, input.shrink(qc), path, info), qc);
  }
}
