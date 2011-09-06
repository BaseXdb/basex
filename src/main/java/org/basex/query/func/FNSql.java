package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Properties;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.Bln;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.AxisMoreIter;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.util.Err;
import org.basex.util.Atts;
import org.basex.util.InputInfo;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class FNSql extends FuncCall {
  /** sql:tuple element. */
  private static final byte[] TUPLE = token("sql:tuple");
  /** parameter attribute: type. */
  private static final byte[] TYPE = token("type");
  /** parameter attribute: null. */
  private static final byte[] NULL = token("null");
  /** Type int. */
  private static final byte[] INT = token("int");
  /** Type string. */
  private static final byte[] STRING = token("string");
  /** Type boolean. */
  private static final byte[] BOOL = token("boolean");
  /** Type date. */
  private static final byte[] DATE = token("date");
  /** Type double. */
  private static final byte[] DOUBLE = token("double");
  /** Type float. */
  private static final byte[] FLOAT = token("float");
  /** Type short. */
  private static final byte[] SHORT = token("short");
  /** Type time. */
  private static final byte[] TIME = token("time");
  /** Type timestamp. */
  private static final byte[] TIMESTAMP = token("timestamp");
  /** Tuple. */
  private static final QNm Q_TUPLE = new QNm(TUPLE, SQLURI);
  /** SQL Namespace attribute. */
  private static final Atts NS_SQL = new Atts().add(SQL, SQLURI);

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNSql(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);
    switch(def) {
      case EXECUTE:
        return execute(ctx);
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    checkAdmin(ctx);
    switch(def) {
      case CONNECT:
        return connect(ctx);
      case PREPARE:
        return prepare(ctx);
      case CLOSE:
        return close(ctx);
      case COMMIT:
        return commit(ctx);
      case ROLLBACK:
        return rollback(ctx);
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Establishes a connection to a relational database.
   * @param ctx query context
   * @return connection id
   * @throws QueryException query exception
   */
  private Itr connect(final QueryContext ctx) throws QueryException {
    // URL to relational database
    final String url = string(checkStr(expr[0], ctx));
    // Auto-commit mode
    final boolean autoComm = expr.length < 2 || checkBln(expr[1], ctx);
    try {
      final Properties prop = new Properties();
      prop.put("create", "true");

      // Establish a connection to the relational database
      final Connection conn = DriverManager.getConnection(url, prop);

      /*final Connection conn = expr.length == 4 ? DriverManager.getConnection(
          url, string(checkStr(expr[2], ctx)), string(checkStr(expr[3], ctx)))
          : DriverManager.getConnection(url);
          */
      conn.setAutoCommit(autoComm);
      return Itr.get(ctx.jdbc.add(conn));
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Prepares a statement and returns its id.
   * @param ctx query context
   * @return prepared statement id
   * @throws QueryException query exception
   */
  private Itr prepare(final QueryContext ctx) throws QueryException {
    final Connection conn = connection(ctx, false);
    // Prepared statement
    final byte[] prepStmt = checkStr(expr[1], ctx);
    try {
      // Keep prepared statement in depot
      final PreparedStatement prep = conn.prepareStatement(string(prepStmt));
      return Itr.get(ctx.jdbc.add(prep));
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Executes a query, update or prepared statement.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Iter execute(final QueryContext ctx) throws QueryException {
    final int id = (int) checkItr(expr[0].item(ctx, input));
    final Object obj = ctx.jdbc.get(id);
    if(obj == null) throw Err.NOCONN.thrw(input, id);
    // Execute query or prepared statement
    return obj instanceof Connection ?
        executeQuery((Connection) obj, ctx) :
        executePrepStmt((PreparedStatement) obj, ctx);
  }

  /**
   * Executes a query or an update statement on a relational database.
   * @param conn connection
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private NodeCache executeQuery(final Connection conn, final QueryContext ctx)
      throws QueryException {

    final String query = string(checkStr(ctx.iter(expr[1]).next(), ctx));
    try {
      final Statement stmt = conn.createStatement();
      final boolean result = stmt.execute(query);
      return result ? buildResult(stmt.getResultSet()) : new NodeCache();
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Executes a prepared statement.
   * @param stmt prepared statement
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private NodeCache executePrepStmt(final PreparedStatement stmt,
      final QueryContext ctx) throws QueryException {

    // Get parameters for prepared statement
    final ANode params = (ANode) checkType(expr[1].item(ctx, input),
        NodeType.ELM);
    try {
      final int placeCount = stmt.getParameterMetaData().getParameterCount();
      // Check if number of parameters equals number of place holders
      if(placeCount != countParams(params)) PARAMS.thrw(input);
      else setParameters(params.children(), stmt);
      final boolean result = stmt.execute();
      return result ? buildResult(stmt.getResultSet()) : new NodeCache();
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Counts the numbers of <parameter/> elements.
   * @param params element <param/>
   * @return number of parameters
   */
  private long countParams(final ANode params) {
    final AxisIter ch = params.children();
    long c = ch.size();
    if(c == -1) do ++c; while(ch.next() != null);
    return c;
  }

  /**
   * Sets the parameters of a prepared statement.
   * @param params parameters
   * @param stmt prepared statement
   * @throws QueryException query exception
   */
  private void setParameters(final AxisMoreIter params,
      final PreparedStatement stmt) throws QueryException {

    int i = 0;
    for(ANode next; (next = params.next()) != null;) {
      final AxisIter attrs = next.attributes();
      byte[] paramType = null;
      boolean isNull = false;
      ANode attr;
      while((attr = attrs.next()) != null) {
        if(eq(attr.nname(), TYPE))
          paramType = attr.atom();
        else if(eq(attr.nname(), NULL))
          isNull = attr.atom() != null && Bln.parse(attr.atom(), input);
        else
          throw NOTEXPATTR.thrw(input, string(attr.nname()));
      }
      if(paramType == null) NOPARAMTYPE.thrw(input);
      final byte[] v = next.atom();
      isNull |= v.length == 0;
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
      final byte[] paramType, final String value, final boolean isNull)
      throws QueryException {

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
        throw SQLEXC.thrw(input, "unsupported type: " + string(paramType));
      }
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    } catch(final IllegalArgumentException e) {
      throw ILLFORMAT.thrw(input, string(paramType));
    }
  }

  /**
   * Builds a sequence of elements from a query's result set.
   * @param rs result set
   * @return sequence of elements <tuple/> each of which represents a row from
   *         the result set
   * @throws QueryException query exception
   */
  private NodeCache buildResult(final ResultSet rs) throws QueryException {
    try {
      final ResultSetMetaData metadata = rs.getMetaData();
      final int columnCount = metadata.getColumnCount();
      final NodeCache tuples = new NodeCache();
      while(rs.next()) {
        // For each row in the result set create an element <tuple/>
        final NodeCache a = new NodeCache();
        for(int k = 1; k <= columnCount; k++) {
          // Set columns as attributes of element <tuple/>
          final String label = metadata.getColumnLabel(k);
          final Object value = rs.getObject(label);
          // Null values are ignored
          if(value != null) a.add(new FAttr(new QNm(token(label), EMPTY),
              token(value.toString())));
        }
        tuples.add(new FElem(Q_TUPLE, null, a, NS_SQL, null));
      }
      return tuples;
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Closes a connection to a relational database.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item close(final QueryContext ctx) throws QueryException {
    try {
      connection(ctx, true).close();
      return null;
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Commits all changes made during last transcation.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item commit(final QueryContext ctx) throws QueryException {
    try {
      connection(ctx, false).commit();
      return null;
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Rollbacks all changes made during last transaction.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item rollback(final QueryContext ctx) throws QueryException {
    try {
      connection(ctx, false).rollback();
      return null;
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Returns a connection and removes it from list with opened connections if
   * requested.
   * @param ctx query context
   * @param del flag indicating if connection has to be removed
   * @return connection
   * @throws QueryException query exception
   */
  private Connection connection(final QueryContext ctx, final boolean del)
      throws QueryException {

    final int id = (int) checkItr(expr[0].item(ctx, input));
    final Object obj = ctx.jdbc.get(id);
    if(obj == null || !(obj instanceof Connection)) NOCONN.thrw(input, id);
    if(del) ctx.jdbc.remove(id);
    return (Connection) obj;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX || super.uses(u);
  }
}
