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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbPutBinary extends DbAccessFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final Item input = toNodeOrAtomItem(arg(1), false, qc);
    final String path = toDbPath(arg(2), qc);
    if(data.inMemory()) throw DB_MAINMEM_X.get(info, data.meta.name);
    if(path.isEmpty()) throw DB_PATH_X.get(info, path);

    qc.updates().add(new DBPutBinary(data, input, path, info), qc);
    return Empty.VALUE;
  }
}
