package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Rositsa Shadura
 */
public final class SqlPrepare extends SqlFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    final Connection conn = connection(qc);
    // Prepared statement
    final byte[] prepStmt = toToken(exprs[1], qc);
    try {
      // Keep prepared statement
      final PreparedStatement prep = conn.prepareStatement(string(prepStmt));
      return Int.get(jdbc(qc).add(prep));
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }
}
