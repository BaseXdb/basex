package org.basex.query.func;

import static java.sql.DriverManager.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.Map.Entry;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class FNSql extends StandardFunc {
  /** Module prefix. */
  private static final String PREFIX = "sql";
  /** QName. */
  private static final QNm Q_ROW = QNm.get(PREFIX, "row", SQLURI);
  /** QName. */
  private static final QNm Q_COLUMN = QNm.get(PREFIX, "column", SQLURI);
  /** QName. */
  private static final QNm Q_OPTIONS = QNm.get(PREFIX, "options", SQLURI);
  /** QName. */
  private static final QNm Q_PARAMETERS = QNm.get(PREFIX, "parameters", SQLURI);
  /** QName. */
  private static final QNm Q_PARAMETER = QNm.get(PREFIX, "parameter", SQLURI);

  /** Type int. */
  private static final byte[] INT = AtomType.INT.string();
  /** Type string. */
  private static final byte[] STRING = AtomType.STR.string();
  /** Type boolean. */
  private static final byte[] BOOL = AtomType.BLN.string();
  /** Type date. */
  private static final byte[] DATE = AtomType.DAT.string();
  /** Type double. */
  private static final byte[] DOUBLE = AtomType.DBL.string();
  /** Type float. */
  private static final byte[] FLOAT = AtomType.FLT.string();
  /** Type short. */
  private static final byte[] SHORT = AtomType.SHR.string();
  /** Type time. */
  private static final byte[] TIME = AtomType.TIM.string();
  /** Type timestamp. */
  private static final byte[] TIMESTAMP = token("timestamp");

  /** Name. */
  private static final String NAME = "name";
  /** Auto-commit mode. */
  private static final String AUTO_COMM = "autocommit";
  /** User. */
  private static final String USER = "user";
  /** Password. */
  private static final String PASS = "password";

  /** Attribute "type" of <sql:parameter/>. */
  private static final byte[] TYPE = token("type");

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    switch(func) {
      case _SQL_EXECUTE:          return execute(qc);
      case _SQL_EXECUTE_PREPARED: return executePrepared(qc);
      default:                    return super.iter(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    switch(func) {
      case _SQL_INIT:     return init(qc);
      case _SQL_CONNECT:  return connect(qc);
      case _SQL_PREPARE:  return prepare(qc);
      case _SQL_CLOSE:    return close(qc);
      case _SQL_COMMIT:   return commit(qc);
      case _SQL_ROLLBACK: return rollback(qc);
      default:            return super.item(qc, ii);
    }
  }

  /**
   * Initializes JDBC with the specified driver.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item init(final QueryContext qc) throws QueryException {
    final String driver = string(toToken(exprs[0], qc));
    if(Reflect.find(driver) == null) throw BXSQ_DRIVER_X.get(info, driver);
    return null;
  }

  /**
   * Establishes a connection to a relational database.
   * @param qc query context
   * @return connection id
   * @throws QueryException query exception
   */
  private Int connect(final QueryContext qc) throws QueryException {
    // URL to relational database
    final String url = string(toToken(exprs[0], qc));
    final JDBCConnections jdbc = jdbc(qc);
    try {
      if(exprs.length > 2) {
        // credentials
        final String user = string(toToken(exprs[1], qc));
        final String pass = string(toToken(exprs[2], qc));
        if(exprs.length == 4) {
          // connection options
          final Options opts = toOptions(3, Q_OPTIONS, new Options(), qc);
          // extract auto-commit mode from options
          boolean ac = true;
          final HashMap<String, String> options = opts.free();
          final String commit = options.get(AUTO_COMM);
          if(commit != null) {
            ac = Util.yes(commit);
            options.remove(AUTO_COMM);
          }
          // connection properties
          final Properties props = connProps(options);
          props.setProperty(USER, user);
          props.setProperty(PASS, pass);

          // open connection
          final Connection conn = getConnection(url, props);
          // set auto/commit mode
          conn.setAutoCommit(ac);
          return Int.get(jdbc.add(conn));
        }
        return Int.get(jdbc.add(getConnection(url, user, pass)));
      }
      return Int.get(jdbc.add(getConnection(url)));
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }

  /**
   * Parses connection options.
   * @param options options
   * @return connection properties
   */
  private static Properties connProps(final HashMap<String, String> options) {
    final Properties props = new Properties();
    for(final Entry<String, String> entry : options.entrySet()) {
      props.setProperty(entry.getKey(), entry.getValue());
    }
    return props;
  }

  /**
   * Prepares a statement and returns its id.
   * @param qc query context
   * @return prepared statement id
   * @throws QueryException query exception
   */
  private Int prepare(final QueryContext qc) throws QueryException {
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

  /**
   * Executes a query, update or prepared statement.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private NodeSeqBuilder execute(final QueryContext qc) throws QueryException {
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
   * Executes a query, update or prepared statement.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private NodeSeqBuilder executePrepared(final QueryContext qc) throws QueryException {
    final int id = (int) toLong(exprs[0], qc);
    final Object obj = jdbc(qc).get(id);
    if(!(obj instanceof PreparedStatement)) throw BXSQ_STATE_X.get(info, id);

    // Get parameters for prepared statement
    long c = 0;
    ANode params = null;
    if(exprs.length > 1) {
      params = toElem(exprs[1], qc);
      if(!params.qname().eq(Q_PARAMETERS)) throw INVALIDOPTION_X.get(info, params.qname().local());
      c = countParams(params);
    }

    try {
      final PreparedStatement stmt = (PreparedStatement) obj;
      // Check if number of parameters equals number of place holders
      if(c != stmt.getParameterMetaData().getParameterCount()) throw BXSQ_PARAMS.get(info);
      if(params != null) setParameters(params.children(), stmt);
      return stmt.execute() ? buildResult(stmt.getResultSet()) : new NodeSeqBuilder();
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }

  /**
   * Counts the numbers of <sql:parameter/> elements.
   * @param params element <sql:parameter/>
   * @return number of parameters
   */
  private static long countParams(final ANode params) {
    final AxisIter ch = params.children();
    long c = ch.size();
    if(c == -1) do ++c;
    while(ch.next() != null);
    return c;
  }

  /**
   * Sets the parameters of a prepared statement.
   * @param params parameters
   * @param stmt prepared statement
   * @throws QueryException query exception
   */
  private void setParameters(final AxisMoreIter params, final PreparedStatement stmt)
      throws QueryException {

    int i = 0;
    for(ANode next; (next = params.next()) != null;) {
      // Check name
      if(!next.qname().eq(Q_PARAMETER)) throw INVALIDOPTION_X.get(info, next.qname().local());
      final AxisIter attrs = next.attributes();
      byte[] paramType = null;
      boolean isNull = false;
      for(ANode attr; (attr = attrs.next()) != null;) {
        // Attribute "type"
        if(eq(attr.name(), TYPE)) paramType = attr.string();
        // Attribute "null"
        else if(eq(attr.name(), NULL))
          isNull = attr.string() != null && Bln.parse(attr.string(), info);
        // Not expected attribute
        else throw BXSQ_ATTR_X.get(info, string(attr.name()));
      }
      if(paramType == null) throw BXSQ_TYPE.get(info);
      final byte[] v = next.string();
      setParam(++i, stmt, paramType, isNull ? null : string(v), isNull);
    }
  }

  /**
   * Sets the parameter with the given index in a prepared statement.
   * @param index parameter index
   * @param stmt prepared statement
   * @param paramType parameter type
   * @param value parameter value
   * @param isNull indicator if the parameter is null or not
   * @throws QueryException query exception
   */
  private void setParam(final int index, final PreparedStatement stmt,
      final byte[] paramType, final String value, final boolean isNull) throws QueryException {
    try {
      if(eq(BOOL, paramType)) {
        if(isNull) stmt.setNull(index, Types.BOOLEAN);
        else stmt.setBoolean(index, Boolean.parseBoolean(value));
      } else if(eq(DATE, paramType)) {
        if(isNull) stmt.setNull(index, Types.DATE);
        else stmt.setDate(index, Date.valueOf(value));
      } else if(eq(DOUBLE, paramType)) {
        if(isNull) stmt.setNull(index, Types.DOUBLE);
        else stmt.setDouble(index, Double.parseDouble(value));
      } else if(eq(FLOAT, paramType)) {
        if(isNull) stmt.setNull(index, Types.FLOAT);
        else stmt.setFloat(index, Float.parseFloat(value));
      } else if(eq(INT, paramType)) {
        if(isNull) stmt.setNull(index, Types.INTEGER);
        else stmt.setInt(index, Integer.parseInt(value));
      } else if(eq(SHORT, paramType)) {
        if(isNull) stmt.setNull(index, Types.SMALLINT);
        else stmt.setShort(index, Short.parseShort(value));
      } else if(eq(STRING, paramType)) {
        if(isNull) stmt.setNull(index, Types.VARCHAR);
        else stmt.setString(index, value);
      } else if(eq(TIME, paramType)) {
        if(isNull) stmt.setNull(index, Types.TIME);
        else stmt.setTime(index, Time.valueOf(value));
      } else if(eq(TIMESTAMP, paramType)) {
        if(isNull) stmt.setNull(index, Types.TIMESTAMP);
        else stmt.setTimestamp(index, Timestamp.valueOf(value));
      } else {
        throw BXSQ_ERROR_X.get(info, "unsupported type: " + string(paramType));
      }
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    } catch(final IllegalArgumentException ex) {
      throw BXSQ_FORMAT_X.get(info, string(paramType));
    }
  }

  /**
   * Builds a sequence of elements from a query's result set.
   * @param rs result set
   * @return sequence of elements <tuple/> each of which represents a row from
   *         the result set
   * @throws QueryException query exception
   */
  private NodeSeqBuilder buildResult(final ResultSet rs) throws QueryException {
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

  /**
   * Closes a connection or a prepared statement.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item close(final QueryContext qc) throws QueryException {
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

  /**
   * Commits all changes made during last transaction.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item commit(final QueryContext qc) throws QueryException {
    try {
      connection(qc).commit();
      return null;
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }

  /**
   * Rollbacks all changes made during last transaction.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item rollback(final QueryContext qc) throws QueryException {
    try {
      connection(qc).rollback();
      return null;
    } catch(final SQLException ex) {
      throw BXSQ_ERROR_X.get(info, ex);
    }
  }

  /**
   * Returns a connection.
   * @param qc query context
   * @return connection
   * @throws QueryException query exception
   */
  private Connection connection(final QueryContext qc) throws QueryException {
    final int id = (int) toLong(exprs[0], qc);
    final Object obj = jdbc(qc).get(id);
    if(obj instanceof Connection) return (Connection) obj;
    throw BXSQ_CONN_X.get(info, id);
  }

  /**
   * Returns the JDBC connection handler.
   * @param qc query context
   * @return connection handler
   */
  private static JDBCConnections jdbc(final QueryContext qc) {
    JDBCConnections res = qc.resources.get(JDBCConnections.class);
    if(res == null) {
      res = new JDBCConnections();
      qc.resources.add(res);
    }
    return res;
  }
}
