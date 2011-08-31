package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.math.BigDecimal;
import java.math.BigInteger;
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

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.Dec;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.Item;
import org.basex.query.item.Jav;
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
  private static final String INT = "int";
  /** Type string. */
  private static final String STRING = "string";
  /** Type boolean. */
  private static final String BOOL = "boolean";
  /** Type date. */
  private static final String DATE = "date";
  /** Type double. */
  private static final String DOUBLE = "double";
  /** Type float. */
  private static final String FLOAT = "float";
  /** Type short. */
  private static final String SHORT = "short";
  /** Type time. */
  private static final String TIME = "time";
  /** Type timestamp. */
  private static final String TIMESTAMP = "timestamp";

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
  private Dec connect(final QueryContext ctx) throws QueryException {
    // URL to relational database
    final String url = string(checkStr(expr[0], ctx));
    // Auto-commit mode
    final boolean autoComm = expr.length == 2 ? checkBln(expr[1], ctx) : true;
    try {
      final Connection conn = expr.length == 3 ? DriverManager.getConnection(
          url, string(checkStr(expr[2], ctx)), string(checkStr(expr[3], ctx)))
          : DriverManager.getConnection(url);
      conn.setAutoCommit(autoComm);
      final int connId = ctx.depot.add(conn);
      return new Dec(BigDecimal.valueOf(connId), AtomType.INT);
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
  private Dec prepare(final QueryContext ctx) throws QueryException {
    // Connection id
    final Item id = checkType(expr[0].item(ctx, input), AtomType.INT);
    // Get connection with given id
    final Connection conn = (Connection) ctx.depot.get(toInt(id.atom(input)));
    // Prepared statement
    final byte[] prepStmt = checkStr(expr[1], ctx);
    try {
      final PreparedStatement prep = conn.prepareStatement(string(prepStmt));
      final int prepId = ctx.depot.add(prep);
      return new Dec(BigDecimal.valueOf(prepId), AtomType.INT);
    } catch(SQLException ex) {
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
    final Item id = checkType(expr[0].item(ctx, input), AtomType.INT);
    // Look up id in depot
    final Object obj = ctx.depot.get(toInt(id.atom(input)));
    // Execute
    if(obj == null) throw Err.NOCONN.thrw(input, toInt(id.atom(input)));
    return obj instanceof Connection ? executeQuery((Connection) obj, ctx)
        : executePrepStmt2((PreparedStatement) obj, ctx);
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
    } catch(SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Executes a prepared statement on a relational database.
   * @param stmt prepared statement
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private NodeCache executePrepStmt(final PreparedStatement stmt,
      final QueryContext ctx) throws QueryException {
    // final ItemCache iter = (ItemCache)expr[1].iter(ctx);
    // Item next = null;
    int index = 1;
    try {
      for(int e = 1; e < expr.length; e++) {
        final Item item = expr[e].item(ctx, input);
        final Object next = item == null ? null : item.toJava();
        if(next instanceof BigInteger) {
          // Needed because JDBC accepts only BigDecimal
          stmt.setObject(index++, new BigDecimal((BigInteger) next));
        } else stmt.setObject(index++, next);
      }
      // Set prepare statement's parameters
      // while((next = iter.next()) != null) {
      // if(next.toJava() instanceof BigInteger) {
      // // Needed because JDBC accepts only BigDecimal
      // stmt.setObject(index++, new BigDecimal((BigInteger) next.toJava()));
      // } else stmt.setObject(index++, next.toJava());
      // }
      final boolean result = stmt.execute();
      return result ? buildResult(stmt.getResultSet()) : new NodeCache();
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  private NodeCache executePrepStmt2(final PreparedStatement stmt,
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
  private int countParams(final ANode params) {
    final AxisIter ch = params.children();
    int count = 0;
    while(ch.next() != null)
      count++;
    return count;
  }

  /**
   * Sets the parameters of a prepared statement.
   * @param params parameters
   * @param stmt prepared statement
   * @throws QueryException query exception
   */
  private void setParameters(final AxisMoreIter params,
      final PreparedStatement stmt) throws QueryException {
    int index = 1;
    for(ANode next; ((next = params.next()) != null);) {
      final AxisIter attrs = next.attributes();
      byte[] paramType = null;
      byte[] isNull = null;
      for(ANode attr; ((attr = attrs.next()) != null);) {
        if(eq(attr.nname(), TYPE)) paramType = attr.atom();
        if(eq(attr.nname(), NULL)) isNull = attr.atom();
        if(paramType == null) try {
          NOPARAMTYPE.thrw(input);
        } catch(QueryException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        else setParameter(index++, stmt, string(paramType),
            string(next.atom()), Bln.parse(isNull, input));
      }

    }
  }

  /**
   * Sets the parameter with the given index.
   * @param index parameter index
   * @param stmt prepared statement
   * @param paramType parameter type
   * @param value parameter value
   * @param isNull 
   * @throws QueryException query exception
   */
  private void setParameter(final int index, final PreparedStatement stmt,
      final String paramType, final String value, final boolean isNull)
      throws QueryException {
    try {
      if(paramType.equals(BOOL)) {
        if(isNull) stmt.setNull(index, Types.BOOLEAN);
        else stmt.setBoolean(index, Boolean.parseBoolean(value));
      } else if(paramType.equals(DATE)) {
        if(isNull) stmt.setNull(index, Types.DATE);
        else stmt.setDate(index, Date.valueOf(value));
      } else if(paramType.equals(DOUBLE)) {
        if(isNull) stmt.setNull(index, Types.DOUBLE);
        else stmt.setDouble(index, Double.parseDouble(value));
      } else if(paramType.equals(FLOAT)) {
        if(isNull) stmt.setNull(index, Types.FLOAT);
        else stmt.setFloat(index, Float.parseFloat(value));
      } else if(paramType.equals(INT)) {
        if(isNull) stmt.setNull(index, Types.INTEGER);
        else stmt.setInt(index, Integer.parseInt(value));
      } else if(paramType.equals(SHORT)) {
        if(isNull) stmt.setNull(index, Types.SMALLINT);
        else stmt.setShort(index, Short.parseShort(value));
      } else if(paramType.equals(STRING)) {
        if(isNull) stmt.setNull(index, Types.VARCHAR);
        else stmt.setString(index, value);
      } else if(paramType.equals(TIME)) {
        if(isNull) stmt.setNull(index, Types.TIME);
        else stmt.setTime(index, Time.valueOf(value));
      } else if(paramType.equals(TIMESTAMP)) {
        if(isNull) stmt.setNull(index, Types.TIMESTAMP);
        else stmt.setTimestamp(index, Timestamp.valueOf(value));
      } else {
        // TODO: error
      }
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
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
        // For each row in the result set create an element </tuple>
        final NodeCache a = new NodeCache();
        for(int k = 1; k <= columnCount; k++) {
          // Set columns as attributes of element </tuple>
          final String label = metadata.getColumnLabel(k);
          final Object value = rs.getObject(label);
          if(value != null) a.add(new FAttr(new QNm(token(label), EMPTY),
              new Jav(value).atom(input)));
        }
        tuples.add(new FElem(new QNm(TUPLE, SQLURI), null, a,
            new Atts().add(SQL, SQLURI), null));
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
    final Item id = checkType(expr[0].item(ctx, input), AtomType.INT);
    try {
      final int connId = toInt(id.atom(input));
      ((Connection) ctx.depot.get(connId)).close();
      ctx.depot.remove(connId);
    } catch(SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
    return null;
  }

  /**
   * Commits all changes made during last transcation.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item commit(final QueryContext ctx) throws QueryException {
    final Item id = checkType(expr[0].item(ctx, input), AtomType.INT);
    try {
      ((Connection) ctx.depot.get(toInt(id.atom(input)))).commit();
    } catch(SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
    return null;
  }

  /**
   * Rollbacks all changes made during last transaction.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item rollback(final QueryContext ctx) throws QueryException {
    final Item id = checkType(expr[0].item(ctx, input), AtomType.INT);
    try {
      ((Connection) ctx.depot.get(toInt(id.atom(input)))).rollback();
    } catch(SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
    return null;
  }
}
