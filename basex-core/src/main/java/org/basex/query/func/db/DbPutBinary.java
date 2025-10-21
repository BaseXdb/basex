package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class DbPutBinary extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final Item input = toNodeOrAtomItem(arg(1), false, qc);
    final String path = toDbPath(arg(2), qc);
    return put(data, path, new DBPutBinary(data, input, path, info), qc);
  }

  /**
   * Performs a put operation.
   * @param data data reference
   * @param path target path
   * @param up update to perform
   * @param qc query context
   * @return empty value
   * @throws QueryException query exception
   */
  final Empty put(final Data data, final String path, final Update up, final QueryContext qc)
      throws QueryException {

    final HashMap<String, String> options = toOptions(arg(3), qc);
    if(data.inMemory()) throw DB_MAINMEM_X.get(info, data.meta.name);
    if(path.isEmpty()) throw DB_PATH_X.get(info, path);

    final IntList docs = data.resources.docs(path);
    if(put(docs, data, path, options)) {
      // delete XML resources
      final Updates updates = qc.updates();
      for(final int pre : docs.toArray()) updates.add(new DeleteNode(pre, data, info), qc);
      // add binary resource
      updates.add(up, qc);
    }
    return Empty.VALUE;
  }
}
