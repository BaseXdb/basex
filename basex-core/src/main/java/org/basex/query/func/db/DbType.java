package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DbType extends DbAccess {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final String path = toDbPath(1, qc);

    ResourceType type = null;
    if(data.resources.doc(path) != -1) {
      type = ResourceType.XML;
    } else {
      for(final ResourceType tp : Resources.BINARIES) {
        final IOFile bin = data.meta.file(path, tp);
        if(bin != null && bin.exists() && !bin.isDir()) {
          type = tp;
          break;
        }
      }
    }
    if(type == null) throw WHICHRES_X.get(info, path);
    return Str.get(type.toString());
  }
}
