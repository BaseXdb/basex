package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class DbGet extends DbAccess {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final String path = defined(1) ? toDbPath(arg(1), qc) : "";
    return DBNodeSeq.get(data.resources.docs(path), data, true, path.isEmpty());
  }

  @Override
  public boolean ddo() {
    return true;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return cc.dynamic && allAreValues(true) ? value(cc.qc) : compileData(cc);
  }
}
