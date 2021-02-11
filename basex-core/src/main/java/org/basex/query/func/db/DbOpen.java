package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbOpen extends DbAccess {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final String path = exprs.length < 2 ? "" : path(1, qc);
    return DBNodeSeq.get(data.resources.docs(path), data, true, path.isEmpty());
  }

  @Override
  public boolean ddo() {
    return true;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return compileData(cc);
  }
}
