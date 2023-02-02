package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DbExists extends DbAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    try {
      final Data data = toData(qc);
      if(exprs.length < 2) return Bln.TRUE;
      final String path = toDbPath(1, qc);

      final Checks<ResourceType> exists = type -> {
        final IOFile bin = data.meta.file(path, type);
        return bin != null && bin.exists() && !bin.isDir();
      };
      return Bln.get(data.resources.doc(path) != -1 || exists.any(Resources.BINARIES));
    } catch(final QueryException ex) {
      if(ex.error() == DB_OPEN2_X) return Bln.FALSE;
      throw ex;
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    try {
      return compileData(cc);
    } catch(final QueryException ex) {
      if(ex.error() == DB_OPEN2_X) return Bln.FALSE;
      throw ex;
    }
  }
}
