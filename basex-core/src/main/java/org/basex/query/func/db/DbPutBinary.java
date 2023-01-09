package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbPutBinary extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final Item input = toNodeOrAtomItem(1, qc);
    final String path = toDbPath(2, qc);
    if(data.inMemory()) throw DB_MAINMEM_X.get(info, data.meta.name);
    if(path.isEmpty()) throw RESINV_X.get(info, path);

    qc.updates().add(new DBPutBinary(data, input, path, info), qc);
    return Empty.VALUE;
  }
}
