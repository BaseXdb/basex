package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DbReplace extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final String path = toDbPath(1, qc);
    final Item item = toNodeOrAtomItem(2, qc);
    final Options opts = toOptions(3, new Options(), qc);

    final Updates updates = qc.updates();
    final IntList docs = data.resources.docs(path);
    int d = 0;

    if(item instanceof Bin) {
      // store binary resource
      if(data.inMemory()) throw DB_MAINMEM_X.get(info, data.meta.name);
      updates.add(new DBStore(data, path, item, info), qc);
    } else {
      // store XML document: replace existing document or add new one
      final NewInput input = toNewInput(item, path);
      final Update update = docs.isEmpty() ?
        new DBAdd(data, input, opts, true, qc, info) :
        new ReplaceDoc(docs.get(d++), data, input, opts, qc, info);
      updates.add(update, qc);

      // delete file resources
      for(final ResourceType type : Resources.BINARIES) {
        final IOFile bin = data.meta.file(path, type);
        if(bin != null && bin.exists()) updates.add(new DBDelete(data, bin, info), qc);
      }
    }

    // delete spare documents
    final int ds = docs.size();
    for(; d < ds; d++) updates.add(new DeleteNode(docs.get(d), data, info), qc);
    return Empty.VALUE;
  }
}
