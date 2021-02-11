package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbReplace extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    final Item item = toNodeOrAtomItem(2, qc);
    final Options opts = toOptions(3, new Options(), qc);

    final Updates updates = qc.updates();
    final IntList docs = data.resources.docs(path);
    int d = 0;

    // delete binary resources
    final IOFile bin = data.meta.binary(path);
    final boolean disk = !data.inMemory();
    if(disk && (bin == null || bin.isDir())) throw DB_TARGET_X.get(info, path);

    if(disk && item instanceof Bin) {
      updates.add(new DBStore(data, path, item, info), qc);
    } else {
      if(disk && bin.exists()) updates.add(new DBDelete(data, path, info), qc);
      final NewInput input = checkInput(item, token(path));
      final Update update = docs.isEmpty() ?
        new DBAdd(data, input, opts, true, qc, info) :
        new ReplaceDoc(docs.get(d++), data, input, opts, qc, info);
      updates.add(update, qc);
    }

    // delete old documents
    final int ds = docs.size();
    for(; d < ds; d++) updates.add(new DeleteNode(docs.get(d), data, info), qc);
    return Empty.VALUE;
  }
}
