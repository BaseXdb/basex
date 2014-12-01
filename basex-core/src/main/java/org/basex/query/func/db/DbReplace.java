package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class DbReplace extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final String path = path(1, qc);
    final Item item = toItem(exprs[2], qc);
    final Options opts = toOptions(3, Q_OPTIONS, new Options(), qc);

    final Updates updates = qc.resources.updates();
    final IntList docs = data.resources.docs(path);
    int d = 0;

    // delete binary resources
    final IOFile bin = data.meta.binary(path);
    if(bin == null || bin.isDir()) throw BXDB_REPLACE_X.get(info, path);

    if(item instanceof Bin) {
      updates.add(new DBStore(data, path, item, info), qc);
    } else {
      if(bin.exists()) updates.add(new DBDelete(data, path, info), qc);
      final NewInput input = checkInput(item, token(path));
      if(docs.isEmpty() || docs.get(0) == 0) {
        // no replacement of first document (because of TableDiskAccess#insert, used > 0, pre = 0)
        updates.add(new DBAdd(data, input, opts, qc, info), qc);
      } else {
        updates.add(new ReplaceDoc(docs.get(0), data, input, opts, qc, info), qc);
        d = 1;
      }
    }

    // delete old documents
    final int ds = docs.size();
    for(; d < ds; d++) updates.add(new DeleteNode(docs.get(d), data, info), qc);
    return null;
  }
}
