package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class SqlCommit extends SqlFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    try {
      connection(qc).commit();
      return Empty.VALUE;
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
  }
}
