package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Rositsa Shadura
 */
public final class SqlPrepare extends SqlFn {
  @Override
  public Uri item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Connection conn = connection(qc);
    final String prepStmt = toString(arg(1), qc);
    try {
      // keep prepared statement
      final PreparedStatement prep = conn.prepareStatement(prepStmt);
      return jdbc(qc).add(prep);
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
  }
}
