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
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-15, BSD License
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
    final String query = string(toToken(exprs[1], qc));

    final Object obj = jdbc(qc).get(id);
    if(!(obj instanceof Connection)) throw BXSQ_CONN_X.get(info, id);
    try {
      final Statement stmt = ((Connection) obj).createStatement();
      return stmt.execute(query) ? iter(stmt, true) : Empty.ITER;
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }

  /**
   * Returns a result iterator.
   * @param stmt SQL statement
   * @param close close statement after last result
   * @return iterator
   * @throws QueryException query exception
   */
  final Iter iter(final Statement stmt, final boolean close) throws QueryException {
    try {
      final ResultSet rs = stmt.getResultSet();
      final ResultSetMetaData md = rs.getMetaData();
      final int cc = md.getColumnCount();
      return new Iter() {
        @Override
        public Item next() throws QueryException {
          try {
            if(!rs.next()) {
              rs.close();
              if(close) stmt.close();
              return null;
            }

            final FElem row = new FElem(Q_ROW);
            for(int k = 1; k <= cc; k++) {
              // for each row add column values as children
              final String name = md.getColumnLabel(k);
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
            return row;
          } catch(final SQLException ex) {
            throw BXSQ_ERROR_X.get(info, ex);
          }
        }
      };

    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }
}
