package org.basex.query.func.db;

import static org.basex.query.util.Err.*;
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

    // delete binary resources
    final IOFile bin = data.meta.binary(path);
    if(bin == null || bin.isDir()) throw BXDB_REPLACE_X.get(info, path);

    if(item instanceof Bin) {
      updates.add(new DBStore(data, path, item, info), qc);
    } else {
      if(bin.exists()) updates.add(new DBDelete(data, path, info), qc);
      updates.add(new DBAdd(data, checkInput(item, token(path)), opts, qc, info), qc);
    }

    // remove old documents
    final int ds = docs.size();
    for(int d = 0; d < ds; d++) updates.add(new DeleteNode(docs.get(d), data, info), qc);
    return null;
  }
}
