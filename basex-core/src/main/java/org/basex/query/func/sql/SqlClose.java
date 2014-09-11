package org.basex.query.func.sql;

import static org.basex.query.util.Err.*;

import java.sql.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class SqlClose extends SqlFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    try {
      final int id = (int) toLong(exprs[0], qc);
      final JDBCConnections jdbc = jdbc(qc);
      final Object obj = jdbc.get(id);
      if(obj instanceof Connection) {
        ((Connection) obj).close();
      } else {
        ((PreparedStatement) obj).close();
      }
      jdbc.remove(id);
      return null;
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }
}
