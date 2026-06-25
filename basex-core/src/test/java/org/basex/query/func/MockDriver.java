package org.basex.query.func;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

/**
 * Mock JDBC driver for testing the SQL Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MockDriver implements Driver {
  /** Marker for a value bound via {@code setNull}. */
  public static final Object NULL = new Object() {
    @Override public String toString() { return "NULL"; }
  };

  static {
    try {
      DriverManager.registerDriver(new MockDriver());
    } catch(final SQLException ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  /** Column labels of the canned result set. */
  public static String[] columns = {};
  /** Rows of the canned result set. */
  public static List<Object[]> rows = new ArrayList<>();
  /** Update count returned for updating statements. */
  public static int updateCount;
  /** Column labels of the canned generated keys. */
  public static String[] keyColumns = {};
  /** Rows of the canned generated keys. */
  public static List<Object[]> keyRows = new ArrayList<>();

  /** Executed SQL statements. */
  public static List<String> executed = new ArrayList<>();
  /** Recorded parameter bindings (1-based index → value; {@link #NULL} for {@code setNull}). */
  public static List<Object> bindings = new ArrayList<>();
  /** Auto-commit flag. */
  public static boolean autoCommit = true;
  /** Indicates if the connection was committed. */
  public static boolean committed;
  /** Indicates if the connection was rolled back. */
  public static boolean rolledBack;
  /** Indicates if auto-generated keys were requested. */
  public static boolean keysRequested;
  /** Last requested query timeout. */
  public static int queryTimeout;
  /** Exception thrown by the next {@code execute} call ({@code null} for none). */
  public static SQLException failure;

  /** Resets configuration and recordings; call before each test. */
  public static void reset() {
    columns = new String[0];
    rows = new ArrayList<>();
    updateCount = 0;
    keyColumns = new String[0];
    keyRows = new ArrayList<>();
    executed = new ArrayList<>();
    bindings = new ArrayList<>();
    autoCommit = true;
    committed = false;
    rolledBack = false;
    keysRequested = false;
    queryTimeout = 0;
    failure = null;
  }

  /**
   * Configures the canned result set.
   * @param cols column labels
   * @param data rows
   */
  public static void result(final String[] cols, final Object[]... data) {
    columns = cols;
    rows = new ArrayList<>(Arrays.asList(data));
  }

  /**
   * Configures the canned generated keys.
   * @param cols column labels
   * @param data rows
   */
  public static void keys(final String[] cols, final Object[]... data) {
    keyColumns = cols;
    keyRows = new ArrayList<>(Arrays.asList(data));
  }

  @Override
  public Connection connect(final String url, final Properties info) {
    return acceptsURL(url) ? proxy(Connection.class, new ConnHandler()) : null;
  }

  @Override
  public boolean acceptsURL(final String url) {
    return url != null && url.startsWith("jdbc:mock");
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
    return new DriverPropertyInfo[0];
  }

  @Override
  public int getMajorVersion() {
    return 1;
  }

  @Override
  public int getMinorVersion() {
    return 0;
  }

  @Override
  public boolean jdbcCompliant() {
    return false;
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException();
  }

  /**
   * Creates a proxy for a JDBC interface and links the handler back to it.
   * @param <T> interface type
   * @param type interface
   * @param handler invocation handler
   * @return proxy instance
   */
  private static <T> T proxy(final Class<T> type, final Handler handler) {
    final T instance = type.cast(Proxy.newProxyInstance(MockDriver.class.getClassLoader(),
        new Class<?>[] { type }, handler));
    handler.proxy = instance;
    return instance;
  }

  /**
   * Records a parameter binding.
   * @param index 1-based parameter index
   * @param value bound value
   */
  private static void bind(final int index, final Object value) {
    while(bindings.size() < index) bindings.add(null);
    bindings.set(index - 1, value);
  }

  /**
   * Indicates if a statement yields a result set.
   * @param sql SQL string
   * @return result of check
   */
  private static boolean isQuery(final String sql) {
    final String s = sql == null ? "" : sql.trim().toLowerCase(Locale.ENGLISH);
    return s.startsWith("select") || s.contains("returning");
  }

  /** Base invocation handler: handles {@link Object} and common JDBC methods. */
  private abstract static class Handler implements InvocationHandler {
    /** Sentinel for methods the subclass does not handle. */
    static final Object UNHANDLED = new Object();
    /** Back-reference to the created proxy. */
    Object proxy;

    @Override
    public final Object invoke(final Object p, final Method m, final Object[] a) throws Throwable {
      switch(m.getName()) {
        case "toString": return getClass().getSimpleName() + '@' + System.identityHashCode(proxy);
        case "hashCode": return System.identityHashCode(proxy);
        case "equals": return proxy == a[0];
        case "isClosed": return false;
        case "isWrapperFor": return false;
        case "unwrap": throw new SQLException("Not a wrapper.");
        case "getWarnings": return null;
        case "clearWarnings": case "close": return null;
        default:
      }
      final Object result = call(m, a);
      return result == UNHANDLED ? def(m.getReturnType()) : result;
    }

    /**
     * Handles a method call, or returns {@link #UNHANDLED}.
     * @param m method
     * @param a arguments (can be {@code null})
     * @return result or sentinel
     * @throws SQLException SQL exception
     */
    abstract Object call(Method m, Object[] a) throws SQLException;

    /**
     * Returns a type-appropriate default for an unhandled method.
     * @param t return type
     * @return default value
     */
    private static Object def(final Class<?> t) {
      if(t == boolean.class) return false;
      if(t == int.class) return 0;
      if(t == long.class) return 0L;
      if(t == short.class) return (short) 0;
      if(t == byte.class) return (byte) 0;
      if(t == double.class) return 0d;
      if(t == float.class) return 0f;
      return null;
    }
  }

  /** Connection handler. */
  private static final class ConnHandler extends Handler {
    @Override
    Object call(final Method m, final Object[] a) {
      switch(m.getName()) {
        case "createStatement":
          return proxy(Statement.class, new StmtHandler(proxy, null));
        case "prepareStatement":
          final boolean keys = a.length > 1 && a[1] instanceof final Integer i &&
              i == Statement.RETURN_GENERATED_KEYS;
          if(keys) keysRequested = true;
          return proxy(PreparedStatement.class, new StmtHandler(proxy, (String) a[0]));
        case "setAutoCommit": autoCommit = (Boolean) a[0]; return null;
        case "getAutoCommit": return autoCommit;
        case "commit": committed = true; return null;
        case "rollback": rolledBack = true; return null;
        default: return UNHANDLED;
      }
    }
  }

  /** (Prepared) statement handler. */
  private static final class StmtHandler extends Handler {
    /** Owning connection proxy. */
    private final Object conn;
    /** Prepared SQL ({@code null} for plain statements). */
    private final String sql;

    /**
     * Constructor.
     * @param conn connection proxy
     * @param sql prepared SQL (can be {@code null})
     */
    private StmtHandler(final Object conn, final String sql) {
      this.conn = conn;
      this.sql = sql;
    }

    @Override
    Object call(final Method m, final Object[] a) throws SQLException {
      final String n = m.getName();
      switch(n) {
        case "getConnection": return conn;
        case "setQueryTimeout": queryTimeout = (Integer) a[0]; return null;
        case "getResultSet": return proxy(ResultSet.class, new RowsHandler(columns, rows));
        case "getUpdateCount": return updateCount;
        case "getGeneratedKeys":
          keysRequested = true;
          return proxy(ResultSet.class, new RowsHandler(keyColumns, keyRows));
        case "execute":
          if(failure != null) throw failure;
          final String s = a != null && a.length > 0 && a[0] instanceof final String str ?
              str : sql;
          if(a != null && a.length > 1 && a[1] instanceof final Integer i &&
              i == Statement.RETURN_GENERATED_KEYS) keysRequested = true;
          executed.add(s);
          return isQuery(s);
        default:
          // record parameter binding: setX(int index, value), setNull(int index, int type)
          if(n.startsWith("set") && a != null && a[0] instanceof final Integer index) {
            bind(index, n.equals("setNull") ? NULL : a.length > 1 ? a[1] : null);
            return null;
          }
          return UNHANDLED;
      }
    }
  }

  /** Result set handler. */
  private static final class RowsHandler extends Handler {
    /** Column labels. */
    private final String[] cols;
    /** Rows. */
    private final List<Object[]> data;
    /** Cursor position. */
    private int cursor = -1;

    /**
     * Constructor.
     * @param cols column labels
     * @param data rows
     */
    private RowsHandler(final String[] cols, final List<Object[]> data) {
      this.cols = cols;
      this.data = data;
    }

    @Override
    Object call(final Method m, final Object[] a) {
      switch(m.getName()) {
        case "next": return ++cursor < data.size();
        case "getObject": return data.get(cursor)[(Integer) a[0] - 1];
        case "getMetaData": return proxy(ResultSetMetaData.class, new MetaHandler(cols));
        default: return UNHANDLED;
      }
    }
  }

  /** Result set metadata handler. */
  private static final class MetaHandler extends Handler {
    /** Column labels. */
    private final String[] cols;

    /**
     * Constructor.
     * @param cols column labels
     */
    private MetaHandler(final String[] cols) {
      this.cols = cols;
    }

    @Override
    Object call(final Method m, final Object[] a) {
      switch(m.getName()) {
        case "getColumnCount": return cols.length;
        case "getColumnLabel": case "getColumnName": return cols[(Integer) a[0] - 1];
        default: return UNHANDLED;
      }
    }
  }
}
