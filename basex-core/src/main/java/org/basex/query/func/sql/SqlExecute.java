package org.basex.query.func.sql;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

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
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public class SqlExecute extends SqlFn {
  /** QName. */
  static final QNm Q_ROW = new QNm(SQL_PREFIX, "row", SQL_URI);
  /** QName. */
  static final QNm Q_COLUMN = new QNm(SQL_PREFIX, "column", SQL_URI);
  /** QName. */
  static final QNm Q_PARAMETERS = new QNm(SQL_PREFIX, "parameters", SQL_URI);
  /** QName. */
  static final QNm Q_PARAMETER = new QNm(SQL_PREFIX, "parameter", SQL_URI);
  /** QName. */
  static final QNm Q_NAME = new QNm("name");
  /** QName. */
  static final QNm Q_NULL = new QNm("null");

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Connection conn = connection(qc);
    final String statement = toString(arg(1), qc);
    final StatementOptions options = toOptions(arg(2), new StatementOptions(), qc);
    final boolean keys = options.get(StatementOptions.GENERATED_KEYS);
    final boolean nulls = options.get(StatementOptions.NULL);

    try {
      final Statement stmt = conn.createStatement();
      stmt.setQueryTimeout(options.get(StatementOptions.TIMEOUT));
      final boolean result = keys ? stmt.execute(statement, Statement.RETURN_GENERATED_KEYS) :
        stmt.execute(statement);
      return iter(stmt, true, result, keys, nulls);
    } catch(final SQLTimeoutException ex) {
      throw SQL_TIMEOUT_X.get(info, ex);
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
  }

  /**
   * Returns a result iterator, the auto-generated keys, or the number of updated rows.
   * @param stmt SQL statement
   * @param close close statement after last result
   * @param result result set flag ({@code false}: statement was updating)
   * @param keys return auto-generated keys instead of the update count
   * @param nulls represent null values as empty columns instead of omitting them
   * @return iterator
   * @throws QueryException query exception
   */
  final Iter iter(final Statement stmt, final boolean close, final boolean result,
      final boolean keys, final boolean nulls) throws QueryException {

    try {
      // updating statement: return auto-generated keys or number of updated rows
      if(!result) {
        if(keys) return rows(stmt.getGeneratedKeys(), stmt, close, nulls);
        return Itr.get(stmt.getUpdateCount()).iter();
      }
      // query statement: return result set
      return rows(stmt.getResultSet(), stmt, close, nulls);
    } catch(final SQLException ex) {
      throw SQL_ERROR_X.get(info, ex);
    }
  }

  /**
   * Returns an iterator over the rows of a result set.
   * @param rs result set
   * @param stmt SQL statement
   * @param close close statement after last result
   * @param nulls represent null values as empty columns instead of omitting them
   * @return iterator
   * @throws SQLException SQL exception
   */
  private Iter rows(final ResultSet rs, final Statement stmt, final boolean close,
      final boolean nulls) throws SQLException {

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

          final FBuilder row = FElem.build(Q_ROW).ns();
          for(int c = 1; c <= cols; c++) {
            // for each row add column values as children
            final String name = md.getColumnLabel(c);
            final Object value = rs.getObject(c);

            // element <sql:column name='...'>...</sql:column>
            final FBuilder column = FElem.build(Q_COLUMN).attr(Q_NAME, name);
            if(value == null) {
              // null values are omitted unless requested as <sql:column name='...' null='true'/>
              if(!nulls) continue;
              column.attr(Q_NULL, "true");
            } else if(value instanceof final SQLXML sxml) {
              // add XML value as child element
              final String xml = sxml.getString();
              try {
                column.node(new DBNode(new IOContent(xml)).childIter().next());
              } catch(final IOException ex) {
                // fallback: add string representation
                Util.debug(ex);
                column.text(xml);
              }
            } else if(value instanceof final Clob clob) {
              // add huge string from clob
              column.text(clob.getSubString(1, (int) clob.length()));
            } else {
              // add string representation of other values
              column.text(value);
            }
            row.node(column);
          }
          return row.finish();
        } catch(final SQLException ex) {
          throw SQL_ERROR_X.get(info, ex);
        }
      }
    };
  }
}
