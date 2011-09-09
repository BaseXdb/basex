package org.basex.query.func;

import static java.sql.DriverManager.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.sql.Connection;
import java.sql.Date;
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
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Uri;
import org.basex.query.item.map.Map;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.AxisMoreIter;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.util.Atts;
import org.basex.util.InputInfo;
import org.basex.util.Reflect;
import org.basex.util.hash.TokenObjMap;

/**
 * Functions on relational databases.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class FNSql extends FuncCall {
  /** Types. */
  /** Type int. */
  private static final byte[] INT = AtomType.INT.nam();
  /** Type string. */
  private static final byte[] STRING = AtomType.STR.nam();
  /** Type boolean. */
  private static final byte[] BOOL = AtomType.BLN.nam();
  /** Type date. */
  private static final byte[] DATE = AtomType.DAT.nam();
  /** Type double. */
  private static final byte[] DOUBLE = AtomType.DBL.nam();
  /** Type float. */
  private static final byte[] FLOAT = AtomType.FLT.nam();
  /** Type short. */
  private static final byte[] SHORT = AtomType.SHR.nam();
  /** Type time. */
  private static final byte[] TIME = AtomType.TIM.nam();
  /** Type timestamp. */
  private static final byte[] TIMESTAMP = token("timestamp");

  /** Names. */
  /** Row. */
  private static final QNm Q_ROW = new QNm(token("sql:row"), SQLURI);
  /** Column. */
  private static final QNm Q_COLUMN = new QNm(token("sql:column"), SQLURI);
  /** Name. */
  private static final QNm Q_NAME = new QNm(token("name"), EMPTY);

  /** Elements. */
  /** <sql:options/>. */
  private static final QNm E_OPS = new QNm(token("options"), SQLURI);
  /** <sql:parameters/>. */
  private static final QNm E_PARAMS = new QNm(token("parameters"), SQLURI);
  /** <sql:parameter/>. */
  private static final QNm E_PARAM = new QNm(token("parameter"), SQLURI);

  /** Connection options. */
  /** Auto-commit mode. */
  private static final byte[] AUTO_COMM = token("autocommit");
  /** User. */
  private static final String USER = "user";
  /** Password. */
  private static final String PASS = "password";

  /** Other. */
  /** SQL Namespace attribute. */
  private static final Atts NS_SQL = new Atts().add(SQL, SQLURI);
  /** Attribute "type" of <sql:parameter/>. */
  private static final byte[] TYPE = token("type");

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
      case EXECUTE: return execute(ctx);
      default:      return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    checkAdmin(ctx);
    switch(def) {
      case INIT:     return init(ctx);
      case CONNECT:  return connect(ctx);
      case PREPARE:  return prepare(ctx);
      case CLOSE:    return close(ctx);
      case COMMIT:   return commit(ctx);
      case ROLLBACK: return rollback(ctx);
      default:       return super.item(ctx, ii);
    }
  }

  /**
   * Initializes JDBC with the specified driver.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item init(final QueryContext ctx) throws QueryException {
    final String driver = string(checkStr(expr[0], ctx));
    if(Reflect.find(driver) == null) SQLINIT.thrw(input, driver);
    return null;
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
    try {
      if(expr.length > 2) {
        // Credentials
        final String user = string(checkStr(expr[1], ctx));
        final String pass = string(checkStr(expr[2], ctx));
        if(expr.length == 4) {
          // Connection options
          final TokenObjMap<Object> options = options(3, E_OPS, ctx);
          boolean autoCommit = true;
          final Object commit = options.get(AUTO_COMM);
          if(commit != null) {
            // Extract auto-commit mode from options
            autoCommit = Boolean.parseBoolean(commit.toString());
            options.delete(AUTO_COMM);
          }
          // Connection properties
          final Properties props = connProps(options(3, E_OPS, ctx));
          props.setProperty(USER, user);
          props.setProperty(PASS, pass);
          // Open connection
          final Connection conn = getConnection(url, props);
          // Set auto/commit mode
          conn.setAutoCommit(autoCommit);
          return Itr.get(ctx.jdbc.add(conn));
        }
        return Itr.get(ctx.jdbc.add(getConnection(url, user, pass)));
      }
      return Itr.get(ctx.jdbc.add(getConnection(url)));
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Parses connection options.
   * @param options options
   * @return connection properties
   */
  private Properties connProps(final TokenObjMap<Object> options) {
    final Properties props = new Properties();
    for(final byte[] next : options.keys()) {
      if(next != null) props.setProperty(string(next),
          options.get(next).toString());
    }
    return props;
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
      // Keep prepared statement
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
    if(obj == null) throw NOCONN.thrw(input, id);
    // Execute query or prepared statement
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
    if(!params.qname().eq(E_PARAMS)) PARWHICH.thrw(input, params.qname());
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
   * Counts the numbers of <sql:parameter/> elements.
   * @param params element <sql:parameter/>
   * @return number of parameters
   */
  private long countParams(final ANode params) {
    final AxisIter ch = params.children();
    long c = ch.size();
    if(c == -1) do
      ++c;
    while(ch.next() != null);
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
      // Check name
      if(!next.qname().eq(E_PARAM)) PARWHICH.thrw(input, next.qname());
      final AxisIter attrs = next.attributes();
      byte[] paramType = null;
      boolean isNull = false;
      for(ANode attr; (attr = attrs.next()) != null;) {
        // Attribute "type"
        if(eq(attr.nname(), TYPE)) paramType = attr.atom();
        // Attribute "null"
        else if(eq(attr.nname(), NULL)) isNull = attr.atom() != null
            && Bln.parse(attr.atom(), input);
        // Not expected attribute
        else throw NOTEXPATTR.thrw(input, string(attr.nname()));
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
      final NodeCache rows = new NodeCache();
      while(rs.next()) {
        final NodeCache columns = new NodeCache();
        for(int k = 1; k <= columnCount; k++) {
          // For each row add column values as children
          final String label = metadata.getColumnLabel(k);
          final Object value = rs.getObject(label);
          // Null values are ignored
          if(value != null) {
            // Column name
            final FAttr columnName = new FAttr(Q_NAME, token(label));
            final NodeCache attr = new NodeCache();
            attr.add(columnName);
            // Column value
            final FTxt columnValue = new FTxt(token(value.toString()));
            final NodeCache ch = new NodeCache();
            ch.add(columnValue);
            // Element <sql:column name='...'>...</sql:column>
            columns.add(new FElem(Q_COLUMN, ch, attr, NS_SQL, null));
          }
        }
        rows.add(new FElem(Q_ROW, columns, null, NS_SQL, null));
      }
      return rows;
    } catch(final SQLException ex) {
      throw SQLEXC.thrw(input, ex.getMessage());
    }
  }

  /**
   * Closes a connection to a relational database.
   * @param ctx query context
   * @return {@code null}
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
   * Commits all changes made during last transaction.
   * @param ctx query context
   * @return {@code null}
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
   * @return {@code null}
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

  /**
   * Extracts connection options.
   * @param arg argument with options
   * @param root expected root element
   * @param ctx query context
   * @return options
   * @throws QueryException query exception
   */
  private TokenObjMap<Object> options(final int arg, final QNm root,
      final QueryContext ctx) throws QueryException {
    // initialize token map
    final TokenObjMap<Object> tm = new TokenObjMap<Object>();
    // argument does not exist...
    if(arg >= expr.length) return tm;

    // empty sequence...
    final Item it = expr[arg].item(ctx, input);
    if(it == null) return tm;

    // XQuery map: convert to internal map
    if(it instanceof Map) return ((Map) it).tokenJavaMap(input);
    // no element: convert XQuery map to internal map
    if(!it.type().eq(SeqType.ELM)) throw NODFUNTYPE.thrw(input, this, it.type);

    // parse nodes
    ANode node = (ANode) it;
    if(!node.qname().eq(root)) PARWHICH.thrw(input, node.qname());

    // interpret query parameters
    final AxisIter ai = node.children();
    while((node = ai.next()) != null) {
      final QNm qn = node.qname();
      if(!qn.uri().eq(Uri.uri(SQLURI))) PARWHICH.thrw(input, qn);
      tm.add(qn.ln(), node.children().next());
    }
    return tm;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX || super.uses(u);
  }
}
