package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DbRetrieve extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    if(data.inMemory()) throw BXDB_MEM_X.get(info, data.meta.name);

    final IOFile file = data.meta.binary(path);
    if(file == null || !file.exists() || file.isDir()) throw WHICHRES_X.get(info, path);
    return new B64Stream(file, IOERR_X);
  }
}
