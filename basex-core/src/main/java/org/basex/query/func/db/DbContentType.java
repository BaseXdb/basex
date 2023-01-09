package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbContentType extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final String path = toDbPath(1, qc);

    String pt = null;
    ResourceType type = null;
    final int pre = data.resources.doc(path);
    if(pre != -1) {
      pt = string(data.text(pre, true));
      type = ResourceType.XML;
    } else {
      for(final ResourceType tp : Resources.BINARIES) {
        final IOFile bin = data.meta.file(path, tp);
        if(bin != null && bin.exists() && !bin.isDir()) {
          type = tp;
          pt = path;
          break;
        }
      }
    }
    if(pt == null) throw WHICHRES_X.get(info, path);
    return Str.get(type.contentType(pt).toString());
  }
}
