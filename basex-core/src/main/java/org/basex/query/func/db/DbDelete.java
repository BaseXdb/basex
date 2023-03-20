package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbDelete extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final String path = toDbPath(arg(1), qc);

    // delete XML resources
    final IntList docs = data.resources.docs(path);
    final int ds = docs.size();
    final Updates updates = qc.updates();
    for(int d = 0; d < ds; d++) {
      updates.add(new DeleteNode(docs.get(d), data, info), qc);
    }
    // delete file resources
    for(final ResourceType type : Resources.BINARIES) {
      final IOFile bin = data.meta.file(path, type);
      if(bin != null) updates.add(new DBDelete(data, bin, info), qc);
    }
    return Empty.VALUE;
  }
}
