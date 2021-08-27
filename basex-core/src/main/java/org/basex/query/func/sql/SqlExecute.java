package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.sql.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public class SqlExecute extends SqlFn {
  /** QName. */
  private static final QNm Q_ROW = new QNm(SQL_PREFIX, "row", SQL_URI);
  /** QName. */
  private static final QNm Q_COLUMN = new QNm(SQL_PREFIX, "column", SQL_URI);
  /** Name. */
  private static final String NAME = "name";

  /** Statement Options. */
  public static class StatementOptions extends Options {
    /** Query timeout. */
    public static final NumberOption TIMEOUT = new NumberOption("timeout", 0);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final Connection conn = connection(qc);
    final String query = string(toToken(exprs[1], qc));
    final StatementOptions options = toOptions(2, new StatementOptions(), qc);

    try {
      final Statement stmt = conn.createStatement();
      stmt.setQueryTimeout(options.get(StatementOptions.TIMEOUT));
      return iter(stmt, true, stmt.execute(query));
    } catch(final SQLTimeoutException ex) {
      throw SQL_TIMEOUT_X.get(info, ex);
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  /**
   * Returns a result iterator, or the number of updated rows.
   * @param stmt SQL statement
   * @param close close statement after last result
   * @param result result set flag ({@code false}: statement was updating)
   * @return iterator
   * @throws QueryException query exception
   */
  final Iter iter(final Statement stmt, final boolean close, final boolean result)
      throws QueryException {

    try {
      // updating statement: return number of updated rows
      if(!result) return Int.get(stmt.getUpdateCount()).iter();

      // create result set iterator
      final ResultSet rs = stmt.getResultSet();
      final ResultSetMetaData md = rs.getMetaData();
      final int cols = md.getColumnCount();
      return new Iter() {
        @Override
        public Item next() throws QueryException {
          try {
            if(!rs.next()) {
              rs.close();
              if(close) stmt.close();
              return null;
            }

            final FElem row = new FElem(Q_ROW).declareNS();
            for(int c = 1; c <= cols; c++) {
              // for each row add column values as children
              final String name = md.getColumnLabel(c);
              final Object value = rs.getObject(c);
              // null values are ignored
              if(value == null) continue;

              // element <sql:column name='...'>...</sql:column>
              final FElem col = new FElem(Q_COLUMN).add(NAME, name);
              row.add(col);

              if(value instanceof SQLXML) {
                // add XML value as child element
                final String xml = ((SQLXML) value).getString();
                try {
                  col.add(new DBNode(new IOContent(xml)).childIter().next());
                } catch(final IOException ex) {
                  // fallback: add string representation
                  Util.debug(ex);
                  col.add(xml);
                }
              } else if(value instanceof Clob) {
                // add huge string from clob
                final Clob clob = (Clob) value;
                col.add(clob.getSubString(1, (int) clob.length()));
              } else {
                // add string representation of other values
                col.add(value.toString());
              }
            }
            return row;
          } catch(final SQLException ex) {
            throw SQL_ERROR_X.get(info, ex);
          }
        }
      };

    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
  }
}
