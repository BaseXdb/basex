package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.sql.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public class SqlExecute extends SqlFn {
  /** QName. */
  private static final QNm Q_ROW = QNm.get(SQL_PREFIX, "row", SQL_URI);
  /** QName. */
  private static final QNm Q_COLUMN = QNm.get(SQL_PREFIX, "column", SQL_URI);
  /** Name. */
  private static final String NAME = "name";

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final int id = (int) toLong(exprs[0], qc);
    final Object obj = jdbc(qc).get(id);
    if(!(obj instanceof Connection)) throw BXSQ_CONN_X.get(info, id);

    final String query = string(toToken(exprs[1], qc));
    try(final Statement stmt = ((Connection) obj).createStatement()) {
      return stmt.execute(query) ? buildResult(stmt.getResultSet()) : new NodeSeqBuilder();
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }

  /**
   * Builds a sequence of elements from a query's result set.
   * @param rs result set
   * @return sequence of elements <tuple/> each of which represents a row from the result set
   * @throws QueryException query exception
   */
  final NodeSeqBuilder buildResult(final ResultSet rs) throws QueryException {
    try {
      final ResultSetMetaData metadata = rs.getMetaData();
      final int cc = metadata.getColumnCount();
      final NodeSeqBuilder rows = new NodeSeqBuilder();
      while(rs.next()) {
        final FElem row = new FElem(Q_ROW);
        rows.add(row);
        for(int k = 1; k <= cc; k++) {
          // for each row add column values as children
          final String name = metadata.getColumnLabel(k);
          final Object value = rs.getObject(k);
          // null values are ignored
          if(value == null) continue;

          // element <sql:column name='...'>...</sql:column>
          final FElem col = new FElem(Q_COLUMN).add(NAME, name);
          row.add(col);

          if(value instanceof SQLXML) {
            // add XML value as child element
            final String xml = ((SQLXML) value).getString();
            try {
              col.add(new DBNode(new IOContent(xml)).children().next());
            } catch(final IOException ex) {
              // fallback: add string representation
              Util.debug(ex);
              col.add(xml);
            }
          } else {
            // add string representation of other values
            col.add(value.toString());
          }
        }
      }
      return rows;
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }
}
