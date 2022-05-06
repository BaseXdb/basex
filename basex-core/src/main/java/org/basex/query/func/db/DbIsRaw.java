package org.basex.query.func.db;

import org.basex.data.*;
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
public final class DbIsRaw extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = toData(qc);
    final String path = toDbPath(1, qc);

    final IOFile bin = data.meta.binary(path);
    return Bln.get(bin != null && bin.exists() && !bin.isDir());
  }
}
