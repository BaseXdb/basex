package org.basex.query.func.db;

import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DbStore extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    final Item item = toItem(exprs[2], qc);
    if(data.inMemory()) throw BXDB_MEM_X.get(info, data.meta.name);

    final IOFile file = data.meta.binary(path);
    if(file == null || file.isDir()) throw RESINV_X.get(info, path);
    qc.resources.updates().add(new DBStore(data, path, item, info), qc);
    return null;
  }
}
