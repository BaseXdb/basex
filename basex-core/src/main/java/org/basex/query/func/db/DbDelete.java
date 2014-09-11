package org.basex.query.func.db;

import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DbDelete extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);

    // delete XML resources
    final IntList docs = data.resources.docsIn(path);
    final int is = docs.size();
    final Updates updates = qc.resources.updates();
    for(int i = 0; i < is; i++) {
      updates.add(new DeleteNode(docs.get(i), data, info), qc);
    }
    // delete raw resources
    if(!data.inMemory()) {
      final IOFile bin = data.meta.binary(path);
      if(bin == null) throw UPDBDELETE_X.get(info, path);
      updates.add(new DBDelete(data, path, info), qc);
    }
    return null;
  }
}
