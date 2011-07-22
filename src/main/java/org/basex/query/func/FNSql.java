package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.AtomType;
import org.basex.query.item.Dec;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
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
    return obj instanceof Connection ? executeQuery((Connection) obj, ctx)
        : executePrepStmt((PreparedStatement) obj, ctx);

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
    final Iter iter = expr[1].iter(ctx);
    Item next = null;
    int index = 1;
    try {
      // Set prepare statement's parameters
      while((next = iter.next()) != null) {
        if(next.toJava() instanceof BigInteger) {
          // Needed because JDBC accepts only BigDecimal
          stmt.setObject(index++, new BigDecimal((BigInteger) next.toJava()));
        } else stmt.setObject(index++, next.toJava());
      }
      final boolean result = stmt.execute();
      return result ? buildResult(stmt.getResultSet()) : new NodeCache();
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Builds a sequence of elements from a query's result set.
   * @param rs result set
   * @return sequence of elements <tuple/> each of which represents a row from
   *         the result set
   */
  private NodeCache buildResult(final ResultSet rs) {
    try {
      // Collect columns' names
      final ResultSetMetaData metadata = rs.getMetaData();
      final int columnCount = metadata.getColumnCount();
      final ArrayList<String> columns = new ArrayList<String>();
      for(int i = 1; i < columnCount + 1; i++)
        columns.add(metadata.getColumnLabel(i));
      final NodeCache tuples = new NodeCache();
      while(rs.next()) {
        final NodeCache a = new NodeCache();
        for(int k = 0; k < columns.size(); k++) {
          String label = columns.get(k);
          a.add(new FAttr(new QNm(token(label), EMPTY),
              token(rs.getString(label)), null));
        }
        tuples.add(new FElem(new QNm(TUPLE, SQLURI), null, a, null,
            new Atts().add(SQL, SQLURI), null));
      }
      return tuples;
    } catch(final SQLException ex) {
      // TODO: error handling
      ex.printStackTrace();
      return null;
    }
  }
}
