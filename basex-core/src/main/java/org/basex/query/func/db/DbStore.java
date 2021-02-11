package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbStore extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    final Item item = toNodeOrAtomItem(2, qc);
    if(data.inMemory()) throw DB_MAINMEM_X.get(info, data.meta.name);

    final IOFile file = data.meta.binary(path);
    if(file == null || path.isEmpty()) throw RESINV_X.get(info, path);
    qc.updates().add(new DBStore(data, path, item, info), qc);
    return Empty.VALUE;
  }
}
