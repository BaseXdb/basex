package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DbGetValue extends DbAccess {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final String path = toDbPath(1, qc);
    if(data.inMemory()) throw DB_MAINMEM_X.get(info, data.meta.name);

    final IOFile bin = data.meta.file(path, ResourceType.VALUE);
    if(!bin.exists() || bin.isDir()) throw WHICHRES_X.get(info, path);

    try(DataInput in = new DataInput(bin)) {
      return Store.read(in, qc);
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }
}
