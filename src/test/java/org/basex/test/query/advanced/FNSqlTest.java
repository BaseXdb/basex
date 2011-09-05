//package org.basex.test.query.advanced;
//
//import static org.basex.query.QueryText.*;
//import static org.basex.util.Token.*;
//import java.io.InputStream;
//import java.io.Reader;
//import java.math.BigDecimal;
//import java.net.URL;
//import java.sql.Array;
//import java.sql.Blob;
//import java.sql.CallableStatement;
//import java.sql.Clob;
//import java.sql.Connection;
//import java.sql.DatabaseMetaData;
//import java.sql.Date;
//import java.sql.Driver;
//import java.sql.DriverManager;
//import java.sql.DriverPropertyInfo;
//import java.sql.NClob;
//import java.sql.ParameterMetaData;
//import java.sql.PreparedStatement;
//import java.sql.Ref;
//import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
//import java.sql.RowId;
//import java.sql.SQLClientInfoException;
//import java.sql.SQLException;
//import java.sql.SQLFeatureNotSupportedException;
//import java.sql.SQLWarning;
//import java.sql.SQLXML;
//import java.sql.Savepoint;
//import java.sql.Statement;
//import java.sql.Struct;
//import java.sql.Time;
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Properties;
//import java.util.concurrent.Executor;
//import java.util.logging.Logger;
//
//import org.basex.query.QueryException;
//import org.basex.query.item.FAttr;
//import org.basex.query.item.FElem;
//import org.basex.query.item.Item;
//import org.basex.query.item.Jav;
//import org.basex.query.item.QNm;
//import org.basex.query.iter.ItemCache;
//import org.basex.query.iter.NodeCache;
//import org.basex.util.Atts;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
///**
// * Tests functions on relational databases.
// *
// * @author BaseX Team 2005-11, BSD License
// * @author Rositsa Shadura
// */
//public class FNSqlTest extends AdvancedQueryTest {
//
//  /** Prepares test. */
//  @BeforeClass
//  public static void init() {
//    try {
//      DriverManager.registerDriver(new FakeDriver());
//    } catch(SQLException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//  }
//
//  /**
//   * Tests method for sql:connect() function.
//   * @throws QueryException query exception
//   */
//  @Test
//  public void testConnect() throws QueryException {
//    query("sql:connect('jdbc:test')", 0);
//  }
//
//  /**
//   * Tests method for sql:execute() function.
//   * @throws QueryException query exception
//   */
//  @Test
//  public void testExecute() throws QueryException {
//    // Test with an update statement
//    query("let $conn := sql:connect('jdbc:test') "
//        + "return sql:execute($conn, 'UPDATE')", "");
//
//    // Test with a select statement
//    // Build expected result
//    final Item[] tuples = new Item[2];
//    final NodeCache attrs1 = new NodeCache();
//    attrs1.add(new FAttr(new QNm(token("bigdec"), EMPTY), new Jav(
//        new BigDecimal(1)).atom(null), null));
//    attrs1.add(new FAttr(new QNm(token("bool"), EMPTY), new Jav(new Boolean(
//        true)).atom(null), null));
//    final FElem tuple1 = new FElem(new QNm(token("sql:tuple"), SQLURI), null,
//        attrs1, null, new Atts().add(SQL, SQLURI), null);
//    tuples[0] = tuple1;
//
//    final NodeCache attrs2 = new NodeCache();
//    attrs2.add(new FAttr(new QNm(token("bigdec"), EMPTY), new Jav(
//        new BigDecimal(2)).atom(null), null));
//    attrs2.add(new FAttr(new QNm(token("bool"), EMPTY), new Jav(new Boolean(
//        false)).atom(null), null));
//    final FElem tuple2 = new FElem(new QNm(token("sql:tuple"), SQLURI), null,
//        attrs2, null, new Atts().add(SQL, SQLURI), null);
//    tuples[1] = tuple2;
//    final ItemCache result = new ItemCache(tuples, 2);
//    query("let $conn := sql:connect('jdbc:test')"
//        + "return sql:execute($conn, 'SELECT')",
//        result.toString().replaceAll("(\\r|\\n) *", ""));
//  }
//}
//
///**
// * Fake driver.
// *
// * @author BaseX Team 2005-11, BSD License
// * @author Rositsa Shadura
// */
//final class FakeDriver implements Driver {
//
//  @Override
//  public Connection connect(final String url, final Properties info)
//      throws SQLException {
//    System.out.println("Connected.");
//    return new FakeConnection();
//  }
//
//  @Override
//  public boolean acceptsURL(final String url) throws SQLException {
//    return true;
//  }
//
//  @Override
//  public DriverPropertyInfo[] getPropertyInfo(final String url,
//      final Properties info) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public int getMajorVersion() {
//    return 0;
//  }
//
//  @Override
//  public int getMinorVersion() {
//    return 0;
//  }
//
//  @Override
//  public boolean jdbcCompliant() {
//    return true;
//  }
//
//  @Override
//  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//}
//
///**
// * Fake connection to relation database.
// *
// * @author BaseX Team 2005-11, BSD License
// * @author Rositsa Shadura
// */
//final class FakeConnection implements Connection {
//
//  /** Constructor. */
//  public FakeConnection() {}
//
//  @Override
//  public <T> T unwrap(final Class<T> iface)
//    throws SQLException { return null; }
//
//  @Override
//  public boolean isWrapperFor(final Class<?> iface) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public Statement createStatement() throws SQLException {
//    return new FakeStatement();
//  }
//
//  @Override
//  public PreparedStatement prepareStatement(final String sql)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public CallableStatement prepareCall(final String sql) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public String nativeSQL(final String sql) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void setAutoCommit(final boolean autoCommit) throws SQLException {}
//
//  @Override
//  public boolean getAutoCommit() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void commit() throws SQLException {}
//
//  @Override
//  public void rollback() throws SQLException {}
//
//  @Override
//  public void close() throws SQLException {}
//
//  @Override
//  public boolean isClosed() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public DatabaseMetaData getMetaData() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void setReadOnly(final boolean readOnly) throws SQLException {}
//
//  @Override
//  public boolean isReadOnly() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void setCatalog(final String catalog) throws SQLException {}
//
//  @Override
//  public String getCatalog() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void setTransactionIsolation(final int level) throws SQLException {}
//
//  @Override
//  public int getTransactionIsolation() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public SQLWarning getWarnings() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void clearWarnings() throws SQLException {}
//
//  @Override
//  public Statement createStatement(final int resultSetType,
//      final int resultSetConcurrency) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public PreparedStatement prepareStatement(final String sql,
//      final int resultSetType, final int resultSetConcurrency)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public CallableStatement prepareCall(final String sql,
//      final int resultSetType, final int resultSetConcurrency)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Map<String, Class<?>> getTypeMap() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void setTypeMap(final Map<String, Class<?>> map)
//    throws SQLException {}
//
//  @Override
//  public void setHoldability(final int holdability) throws SQLException {}
//
//  @Override
//  public int getHoldability() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public Savepoint setSavepoint() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Savepoint setSavepoint(final String name) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void rollback(final Savepoint savepoint) throws SQLException {}
//
//  @Override
//  public void releaseSavepoint(final Savepoint savepoint)
//    throws SQLException {}
//
//  @Override
//  public Statement createStatement(final int resultSetType,
//      final int resultSetConcurrency, final int resultSetHoldability)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public PreparedStatement prepareStatement(final String sql,
//      final int resultSetType, final int resultSetConcurrency,
//      final int resultSetHoldability) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public CallableStatement prepareCall(final String sql,
//      final int resultSetType, final int resultSetConcurrency,
//      final int resultSetHoldability) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public PreparedStatement prepareStatement(final String sql,
//      final int autoGeneratedKeys) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public PreparedStatement prepareStatement(final String sql,
//      final int[] columnIndexes) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public PreparedStatement prepareStatement(final String sql,
//      final String[] columnNames) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Clob createClob() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Blob createBlob() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public NClob createNClob() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public SQLXML createSQLXML() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public boolean isValid(final int timeout) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void setClientInfo(final String name, final String value)
//      throws SQLClientInfoException {}
//
//  @Override
//  public void setClientInfo(final Properties properties)
//      throws SQLClientInfoException {}
//
//  @Override
//  public String getClientInfo(final String name) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Properties getClientInfo() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Array createArrayOf(final String typeName, final Object[] elements)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Struct createStruct(final String typeName, final Object[] attributes)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void abort(Executor arg0) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public int getNetworkTimeout() throws SQLException {
//    // TODO Auto-generated method stub
//    return 0;
//  }
//
//  @Override
//  public String getSchema() throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void setSchema(String arg0) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//}
//
///**
// * Fake statement.
// *
// * @author BaseX Team 2005-11, BSD License
// * @author Rositsa Shadura
// */
//final class FakeStatement implements Statement {
//
//  @Override
//  public <T> T unwrap(final Class<T> iface) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public boolean isWrapperFor(final Class<?> iface) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public ResultSet executeQuery(final String sql) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public int executeUpdate(final String sql) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void close() throws SQLException {
//
//  }
//
//  @Override
//  public int getMaxFieldSize() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void setMaxFieldSize(final int max) throws SQLException {}
//
//  @Override
//  public int getMaxRows() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void setMaxRows(final int max) throws SQLException {}
//
//  @Override
//  public void setEscapeProcessing(final boolean enable) throws SQLException {}
//
//  @Override
//  public int getQueryTimeout() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void setQueryTimeout(final int seconds) throws SQLException {}
//
//  @Override
//  public void cancel() throws SQLException {}
//
//  @Override
//  public SQLWarning getWarnings() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void clearWarnings() throws SQLException {
//
//  }
//
//  @Override
//  public void setCursorName(final String name) throws SQLException {}
//
//  @Override
//  public boolean execute(final String sql) throws SQLException {
//    return sql.startsWith("SELECT") ? true : false;
//  }
//
//  @Override
//  public ResultSet getResultSet() throws SQLException {
//    return new FakeResultSet();
//  }
//
//  @Override
//  public int getUpdateCount() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public boolean getMoreResults() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void setFetchDirection(final int direction) throws SQLException {}
//
//  @Override
//  public int getFetchDirection() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void setFetchSize(final int rows) throws SQLException {
//
//  }
//
//  @Override
//  public int getFetchSize() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int getResultSetConcurrency() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int getResultSetType() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void addBatch(final String sql) throws SQLException {}
//
//  @Override
//  public void clearBatch() throws SQLException {}
//
//  @Override
//  public int[] executeBatch() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Connection getConnection() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public boolean getMoreResults(final int current) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public ResultSet getGeneratedKeys() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public int executeUpdate(final String sql, final int autoGeneratedKeys)
//      throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int executeUpdate(final String sql, final int[] columnIndexes)
//      throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int executeUpdate(final String sql, final String[] columnNames)
//      throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public boolean execute(final String sql, final int autoGeneratedKeys)
//      throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean execute(final String sql, final int[] columnIndexes)
//      throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean execute(final String sql, final String[] columnNames)
//      throws SQLException {
//    return false;
//  }
//
//  @Override
//  public int getResultSetHoldability() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public boolean isClosed() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void setPoolable(final boolean poolable) throws SQLException {}
//
//  @Override
//  public boolean isPoolable() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void closeOnCompletion() throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public boolean isCloseOnCompletion() throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//}
//
///**
// * Fake prepared statement.
// *
// * @author BaseX Team 2005-11, BSD License
// * @author Rositsa Shadura
// */
//final class FakePrepStmt implements PreparedStatement {
//
//  @Override
//  public ResultSet executeQuery(final String sql) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public int executeUpdate(final String sql) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void close() throws SQLException {}
//
//  @Override
//  public int getMaxFieldSize() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void setMaxFieldSize(final int max) throws SQLException {}
//
//  @Override
//  public int getMaxRows() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void setMaxRows(final int max) throws SQLException {}
//
//  @Override
//  public void setEscapeProcessing(final boolean enable) throws SQLException {}
//
//  @Override
//  public int getQueryTimeout() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void setQueryTimeout(final int seconds) throws SQLException {}
//
//  @Override
//  public void cancel() throws SQLException {}
//
//  @Override
//  public SQLWarning getWarnings() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void clearWarnings() throws SQLException {}
//
//  @Override
//  public void setCursorName(final String name) throws SQLException {}
//
//  @Override
//  public boolean execute(final String sql) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public ResultSet getResultSet() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public int getUpdateCount() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public boolean getMoreResults() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void setFetchDirection(final int direction) throws SQLException {}
//
//  @Override
//  public int getFetchDirection() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void setFetchSize(final int rows) throws SQLException {}
//
//  @Override
//  public int getFetchSize() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int getResultSetConcurrency() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int getResultSetType() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void addBatch(final String sql) throws SQLException {}
//
//  @Override
//  public void clearBatch() throws SQLException {}
//
//  @Override
//  public int[] executeBatch() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Connection getConnection() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public boolean getMoreResults(final int current) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public ResultSet getGeneratedKeys() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public int executeUpdate(final String sql, final int autoGeneratedKeys)
//      throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int executeUpdate(final String sql, final int[] columnIndexes)
//      throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int executeUpdate(final String sql, final String[] columnNames)
//      throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public boolean execute(final String sql, final int autoGeneratedKeys)
//      throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean execute(final String sql, final int[] columnIndexes)
//      throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean execute(final String sql, final String[] columnNames)
//      throws SQLException {
//    return false;
//  }
//
//  @Override
//  public int getResultSetHoldability() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public boolean isClosed() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void setPoolable(final boolean poolable) throws SQLException {}
//
//  @Override
//  public boolean isPoolable() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public <T> T unwrap(final Class<T> iface) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public boolean isWrapperFor(final Class<?> iface) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public ResultSet executeQuery() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public int executeUpdate() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void setNull(final int parameterIndex, final int sqlType)
//      throws SQLException {}
//
//  @Override
//  public void setBoolean(final int parameterIndex, final boolean x)
//      throws SQLException {}
//
//  @Override
//  public void setByte(final int parameterIndex, final byte x)
//      throws SQLException {}
//
//  @Override
//  public void setShort(final int parameterIndex, final short x)
//      throws SQLException {}
//
//  @Override
//  public void setInt(final int parameterIndex, final int x)
//    throws SQLException {}
//
//  @Override
//  public void setLong(final int parameterIndex, final long x)
//      throws SQLException {}
//
//  @Override
//  public void setFloat(final int parameterIndex, final float x)
//      throws SQLException {}
//
//  @Override
//  public void setDouble(final int parameterIndex, final double x)
//      throws SQLException {}
//
//  @Override
//  public void setBigDecimal(final int parameterIndex, final BigDecimal x)
//      throws SQLException {}
//
//  @Override
//  public void setString(final int parameterIndex, final String x)
//      throws SQLException {}
//
//  @Override
//  public void setBytes(final int parameterIndex, final byte[] x)
//      throws SQLException {}
//
//  @Override
//  public void setDate(final int parameterIndex, final Date x)
//      throws SQLException {}
//
//  @Override
//  public void setTime(final int parameterIndex, final Time x)
//      throws SQLException {}
//
//  @Override
//  public void setTimestamp(final int parameterIndex, final Timestamp x)
//      throws SQLException {}
//
//  @Override
//  public void setAsciiStream(final int parameterIndex, final InputStream x,
//      final int length) throws SQLException {}
//
//  @SuppressWarnings("deprecation")
//  @Override
//  public void setUnicodeStream(final int parameterIndex, final InputStream x,
//      final int length) throws SQLException {}
//
//  @Override
//  public void setBinaryStream(final int parameterIndex, final InputStream x,
//      final int length) throws SQLException {}
//
//  @Override
//  public void clearParameters() throws SQLException {}
//
//  @Override
//  public void setObject(final int parameterIndex, final Object x,
//      final int targetSqlType) throws SQLException {}
//
//  @Override
//  public void setObject(final int parameterIndex, final Object x)
//      throws SQLException {}
//
//  @Override
//  public boolean execute() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void addBatch() throws SQLException {}
//
//  @Override
//  public void setCharacterStream(final int parameterIndex,
//      final Reader reader, final int length) throws SQLException {}
//
//  @Override
//  public void setRef(final int parameterIndex, final Ref x)
//    throws SQLException {}
//
//  @Override
//  public void setBlob(final int parameterIndex, final Blob x)
//      throws SQLException {}
//
//  @Override
//  public void setClob(final int parameterIndex, final Clob x)
//      throws SQLException {}
//
//  @Override
//  public void setArray(final int parameterIndex, final Array x)
//      throws SQLException {}
//
//  @Override
//  public ResultSetMetaData getMetaData() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void setDate(final int parameterIndex, final Date x,
//    final Calendar cal) throws SQLException {}
//
//  @Override
//  public void setTime(final int parameterIndex, final Time x,
//    final Calendar cal) throws SQLException {}
//
//  @Override
//  public void setTimestamp(final int parameterIndex, final Timestamp x,
//      final Calendar cal) throws SQLException {}
//
//  @Override
//  public void setNull(final int parameterIndex, final int sqlType,
//      final String typeName) throws SQLException {}
//
//  @Override
//  public void setURL(final int parameterIndex, final URL x)
//    throws SQLException {}
//
//  @Override
//  public ParameterMetaData getParameterMetaData() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void setRowId(final int parameterIndex, final RowId x)
//      throws SQLException {}
//
//  @Override
//  public void setNString(final int parameterIndex, final String value)
//      throws SQLException {}
//
//  @Override
//  public void setNCharacterStream(final int parameterIndex,
//      final Reader value, final long length) throws SQLException {}
//
//  @Override
//  public void setNClob(final int parameterIndex, final NClob value)
//      throws SQLException {}
//
//  @Override
//  public void setClob(final int parameterIndex, final Reader reader,
//      final long length) throws SQLException {}
//
//  @Override
//  public void setBlob(final int parameterIndex, final InputStream inputStream,
//      final long length) throws SQLException {}
//
//  @Override
//  public void setNClob(final int parameterIndex, final Reader reader,
//      final long length) throws SQLException {}
//
//  @Override
//  public void setSQLXML(final int parameterIndex, final SQLXML xmlObject)
//      throws SQLException {}
//
//  @Override
//  public void setObject(final int parameterIndex, final Object x,
//      final int targetSqlType, final int scaleOrLength) throws SQLException {}
//
//  @Override
//  public void setAsciiStream(final int parameterIndex, final InputStream x,
//      final long length) throws SQLException {}
//
//  @Override
//  public void setBinaryStream(final int parameterIndex, final InputStream x,
//      final long length) throws SQLException {}
//
//  @Override
//  public void setCharacterStream(final int parameterIndex,
//      final Reader reader, final long length) throws SQLException {}
//
//  @Override
//  public void setAsciiStream(final int parameterIndex, final InputStream x)
//      throws SQLException {}
//
//  @Override
//  public void setBinaryStream(final int parameterIndex, final InputStream x)
//      throws SQLException {}
//
//  @Override
//  public void setCharacterStream(final int parameterIndex,
//      final Reader reader) throws SQLException {}
//
//  @Override
//  public void setNCharacterStream(final int parameterIndex,
//      final Reader value) throws SQLException {}
//
//  @Override
//  public void setClob(final int parameterIndex, final Reader reader)
//      throws SQLException {}
//
//  @Override
//  public void setBlob(final int parameterIndex, final InputStream inputStream)
//      throws SQLException {}
//
//  @Override
//  public void setNClob(final int parameterIndex, final Reader reader)
//      throws SQLException {}
//
//  @Override
//  public void closeOnCompletion() throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public boolean isCloseOnCompletion() throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//}
//
//final class FakeResultSet implements ResultSet {
//  /** Result set tuples. */
//  private ArrayList<Tuple> tuples;
//  /** Iterator over result set. */
//  private Iterator<Tuple> it;
//  /** Tuple to which cursor points currently. */
//  private Tuple current;
//
//  /** Constructor. */
//  public FakeResultSet() {
//    // TODO: initialise fake tuples
//    initTuples();
//    it = tuples.iterator();
//    current = null;
//  }
//
//  /** Initializes tuples. */
//  private void initTuples() {
//    tuples = new ArrayList<Tuple>();
//    tuples.add(new Tuple(new BigDecimal(1), new Boolean(true)));
//    tuples.add(new Tuple(new BigDecimal(2), new Boolean(false)));
//  }
//
//  @Override
//  public <T> T unwrap(final Class<T> iface) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public boolean isWrapperFor(final Class<?> iface) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean next() throws SQLException {
//    final boolean hasNext = it.hasNext();
//    if(hasNext) current = it.next();
//    return hasNext;
//  }
//
//  @Override
//  public void close() throws SQLException {}
//
//  @Override
//  public boolean wasNull() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public String getString(final int columnIndex) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public boolean getBoolean(final int columnIndex) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public byte getByte(final int columnIndex) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public short getShort(final int columnIndex) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int getInt(final int columnIndex) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public long getLong(final int columnIndex) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public float getFloat(final int columnIndex) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public double getDouble(final int columnIndex) throws SQLException {
//    return 0;
//  }
//
//  @SuppressWarnings("deprecation")
//  @Override
//  public BigDecimal getBigDecimal(final int columnIndex, final int scale)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public byte[] getBytes(final int columnIndex) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Date getDate(final int columnIndex) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Time getTime(final int columnIndex) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Timestamp getTimestamp(final int columnIndex) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public InputStream getAsciiStream(final int columnIndex)
//    throws SQLException {
//    return null;
//  }
//
//  @SuppressWarnings("deprecation")
//  @Override
//  public InputStream getUnicodeStream(int columnIndex) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public InputStream getBinaryStream(final int columnIndex)
//    throws SQLException {
//    return null;
//  }
//
//  @Override
//  public String getString(final String columnLabel) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public boolean getBoolean(final String columnLabel) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public byte getByte(final String columnLabel) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public short getShort(final String columnLabel) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int getInt(final String columnLabel) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public long getLong(final String columnLabel) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public float getFloat(final String columnLabel) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public double getDouble(final String columnLabel) throws SQLException {
//    return 0;
//  }
//
//  @SuppressWarnings("deprecation")
//  @Override
//  public BigDecimal getBigDecimal(final String columnLabel, final int scale)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public byte[] getBytes(final String columnLabel) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Date getDate(final String columnLabel) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Time getTime(final String columnLabel) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Timestamp getTimestamp(final String columnLabel)
//    throws SQLException {
//    return null;
//  }
//
//  @Override
//  public InputStream getAsciiStream(final String columnLabel)
//      throws SQLException {
//    return null;
//  }
//
//  @SuppressWarnings("deprecation")
//  @Override
//  public InputStream getUnicodeStream(final String columnLabel)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public InputStream getBinaryStream(final String columnLabel)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public SQLWarning getWarnings() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void clearWarnings() throws SQLException {}
//
//  @Override
//  public String getCursorName() throws SQLException {
//    return null;
//  }
//
//  @Override
//  public ResultSetMetaData getMetaData() throws SQLException {
//    return new FakeMetaData();
//  }
//
//  @Override
//  public Object getObject(final int columnIndex) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Object getObject(final String columnLabel) throws SQLException {
//    // if(columnLabel.equals("array")) return current.array;
//    if(columnLabel.equals("bigdec")) return current.bigdec;
//    else if(columnLabel.equals("bool")) return current.bool;
//    // else if (columnLabel.equals("b")) return current.b;
//    else return null;
//  }
//
//  @Override
//  public int findColumn(final String columnLabel) throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public Reader getCharacterStream(final int columnIndex)
//    throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Reader getCharacterStream(final String columnLabel)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public BigDecimal getBigDecimal(final String columnLabel)
//    throws SQLException {
//    return null;
//  }
//
//  @Override
//  public boolean isBeforeFirst() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean isAfterLast() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean isFirst() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean isLast() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void beforeFirst() throws SQLException {}
//
//  @Override
//  public void afterLast() throws SQLException {}
//
//  @Override
//  public boolean first() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean last() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public int getRow() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public boolean absolute(final int row) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean relative(final int rows) throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean previous() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void setFetchDirection(final int direction) throws SQLException {}
//
//  @Override
//  public int getFetchDirection() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public void setFetchSize(final int rows) throws SQLException {}
//
//  @Override
//  public int getFetchSize() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int getType() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public int getConcurrency() throws SQLException {
//    return 0;
//  }
//
//  @Override
//  public boolean rowUpdated() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean rowInserted() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public boolean rowDeleted() throws SQLException {
//    return false;
//  }
//
//  @Override
//  public void updateNull(final int columnIndex) throws SQLException {}
//
//  @Override
//  public void updateBoolean(final int columnIndex, final boolean x)
//      throws SQLException {}
//
//  @Override
//  public void updateByte(final int columnIndex, final byte x)
//      throws SQLException {}
//
//  @Override
//  public void updateShort(final int columnIndex, final short x)
//      throws SQLException {}
//
//  @Override
//  public void updateInt(final int columnIndex, final int x)
//    throws SQLException {}
//
//  @Override
//  public void updateLong(final int columnIndex, final long x)
//      throws SQLException {}
//
//  @Override
//  public void updateFloat(int columnIndex, float x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateDouble(int columnIndex, double x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateBigDecimal(int columnIndex, BigDecimal x)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateString(int columnIndex, String x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateBytes(int columnIndex, byte[] x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateDate(int columnIndex, Date x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateTime(int columnIndex, Time x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateTimestamp(int columnIndex, Timestamp x)
//    throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateAsciiStream(int columnIndex, InputStream x, int length)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateBinaryStream(int columnIndex, InputStream x, int length)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateCharacterStream(int columnIndex, Reader x, int length)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateObject(int columnIndex, Object x, int scaleOrLength)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateObject(int columnIndex, Object x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateNull(String columnLabel) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateBoolean(String columnLabel, boolean x)
//    throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateByte(String columnLabel, byte x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateShort(String columnLabel, short x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateInt(String columnLabel, int x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateLong(String columnLabel, long x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateFloat(String columnLabel, float x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateDouble(String columnLabel, double x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateBigDecimal(String columnLabel, BigDecimal x)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateString(String columnLabel, String x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateBytes(String columnLabel, byte[] x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateDate(String columnLabel, Date x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateTime(String columnLabel, Time x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateTimestamp(String columnLabel, Timestamp x)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateAsciiStream(String columnLabel, InputStream x, int length)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateBinaryStream(String columnLabel, InputStream x,
//      int length) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateCharacterStream(String columnLabel, Reader reader,
//      int length) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateObject(String columnLabel, Object x, int scaleOrLength)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateObject(String columnLabel, Object x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void insertRow() throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateRow() throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void deleteRow() throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void refreshRow() throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void cancelRowUpdates() throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void moveToInsertRow() throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void moveToCurrentRow() throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public Statement getStatement() throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Object getObject(int columnIndex, Map<String, Class<?>> map)
//      throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Ref getRef(int columnIndex) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Blob getBlob(int columnIndex) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Clob getClob(int columnIndex) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Array getArray(int columnIndex) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Object getObject(String columnLabel, Map<String, Class<?>> map)
//      throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Ref getRef(String columnLabel) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Blob getBlob(String columnLabel) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Clob getClob(String columnLabel) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Array getArray(String columnLabel) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Date getDate(int columnIndex, Calendar cal) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Date getDate(String columnLabel, Calendar cal) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Time getTime(int columnIndex, Calendar cal) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Time getTime(String columnLabel, Calendar cal) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Timestamp getTimestamp(int columnIndex, Calendar cal)
//      throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public Timestamp getTimestamp(String columnLabel, Calendar cal)
//      throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public URL getURL(int columnIndex) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public URL getURL(String columnLabel) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public void updateRef(int columnIndex, Ref x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateRef(String columnLabel, Ref x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateBlob(int columnIndex, Blob x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateBlob(String columnLabel, Blob x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateClob(int columnIndex, Clob x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateClob(String columnLabel, Clob x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateArray(int columnIndex, Array x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateArray(String columnLabel, Array x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public RowId getRowId(int columnIndex) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public RowId getRowId(String columnLabel) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public void updateRowId(int columnIndex, RowId x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateRowId(String columnLabel, RowId x) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public int getHoldability() throws SQLException {
//    // TODO Auto-generated method stub
//    return 0;
//  }
//
//  @Override
//  public boolean isClosed() throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//
//  @Override
//  public void updateNString(int columnIndex, String nString)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateNString(String columnLabel, String nString)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateNClob(String columnLabel, NClob nClob)
//    throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public NClob getNClob(int columnIndex) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public NClob getNClob(String columnLabel) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public SQLXML getSQLXML(int columnIndex) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public SQLXML getSQLXML(String columnLabel) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public void updateSQLXML(int columnIndex, SQLXML xmlObject)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void updateSQLXML(String columnLabel, SQLXML xmlObject)
//      throws SQLException {
//    // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public String getNString(final int columnIndex) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public String getNString(final String columnLabel) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Reader getNCharacterStream(final int columnIndex)
//    throws SQLException {
//    return null;
//  }
//
//  @Override
//  public Reader getNCharacterStream(final String columnLabel)
//      throws SQLException {
//    return null;
//  }
//
//  @Override
//  public void updateNCharacterStream(final int columnIndex, final Reader x,
//      final long length) throws SQLException {}
//
//  @Override
//  public void updateNCharacterStream(final String columnLabel,
//      final Reader reader, final long length) throws SQLException {}
//
//  @Override
//  public void updateAsciiStream(final int columnIndex, final InputStream x,
//      final long length) throws SQLException {}
//
//  @Override
//  public void updateBinaryStream(final int columnIndex, final InputStream x,
//      final long length) throws SQLException {}
//
//  @Override
//  public void updateCharacterStream(final int columnIndex, final Reader x,
//      final long length) throws SQLException {}
//
//  @Override
//  public void updateAsciiStream(final String columnLabel, final InputStream x,
//      final long length) throws SQLException {}
//
//  @Override
//  public void updateBinaryStream(final String columnLabel,
//      final InputStream x, final long length) throws SQLException {}
//
//  @Override
//  public void updateCharacterStream(final String columnLabel,
//      final Reader reader, final long length) throws SQLException {}
//
//  @Override
//  public void updateBlob(final int columnIndex, final InputStream inputStream,
//      final long length) throws SQLException {}
//
//  @Override
//  public void updateBlob(final String columnLabel,
//      final InputStream inputStream, final long length) throws SQLException {}
//
//  @Override
//  public void updateClob(final int columnIndex, final Reader reader,
//      final long length) throws SQLException {}
//
//  @Override
//  public void updateClob(final String columnLabel, final Reader reader,
//      final long length) throws SQLException {}
//
//  @Override
//  public void updateNClob(final int columnIndex, final Reader reader,
//      final long length) throws SQLException {}
//
//  @Override
//  public void updateNClob(final String columnLabel, final Reader reader,
//      final long length) throws SQLException {}
//
//  @Override
//  public void updateNCharacterStream(final int columnIndex, final Reader x)
//      throws SQLException {}
//
//  @Override
//  public void updateNCharacterStream(final String columnLabel,
//      final Reader reader) throws SQLException {}
//
//  @Override
//  public void updateAsciiStream(final int columnIndex, final InputStream x)
//      throws SQLException {}
//
//  @Override
//  public void updateBinaryStream(final int columnIndex, final InputStream x)
//      throws SQLException {}
//
//  @Override
//  public void updateCharacterStream(final int columnIndex, final Reader x)
//      throws SQLException {}
//
//  @Override
//  public void updateAsciiStream(final String columnLabel, final InputStream x)
//      throws SQLException {}
//
//  @Override
//  public void updateBinaryStream(final String columnLabel,
//      final InputStream x) throws SQLException {}
//
//  @Override
//  public void updateCharacterStream(final String columnLabel,
//      final Reader reader) throws SQLException {}
//
//  @Override
//  public void updateBlob(final int columnIndex, final InputStream inputStream)
//      throws SQLException {}
//
//  @Override
//  public void updateBlob(final String columnLabel,
//      final InputStream inputStream) throws SQLException {}
//
//  @Override
//  public void updateClob(final int columnIndex, final Reader reader)
//      throws SQLException {}
//
//  @Override
//  public void updateClob(final String columnLabel, final Reader reader)
//      throws SQLException {}
//
//  @Override
//  public void updateNClob(final int columnIndex, final Reader reader)
//      throws SQLException {}
//
//  @Override
//  public void updateNClob(final String columnLabel, final Reader reader)
//      throws SQLException {}
//
//  @Override
//  public <T> T getObject(int arg0, Class<T> arg1) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public <T> T getObject(String arg0, Class<T> arg1) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//}
//
///**
// * Fake result set metadata.
// *
// * @author BaseX Team 2005-11, BSD License
// * @author Rositsa Shadura
// */
//final class FakeMetaData implements ResultSetMetaData {
//
//  /** Column count. */
//  private static final int COLCOUNT = 2;
//  /** Column labels. */
//  private String[] labels = { "bigdec", "bool"};
//
//  @Override
//  public <T> T unwrap(Class<T> iface) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public boolean isWrapperFor(Class<?> iface) throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//
//  @Override
//  public int getColumnCount() throws SQLException {
//    return COLCOUNT;
//  }
//
//  @Override
//  public boolean isAutoIncrement(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//
//  @Override
//  public boolean isCaseSensitive(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//
//  @Override
//  public boolean isSearchable(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//
//  @Override
//  public boolean isCurrency(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//
//  @Override
//  public int isNullable(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return 0;
//  }
//
//  @Override
//  public boolean isSigned(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//
//  @Override
//  public int getColumnDisplaySize(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return 0;
//  }
//
//  @Override
//  public String getColumnLabel(int column) throws SQLException {
//    return labels[column - 1];
//  }
//
//  @Override
//  public String getColumnName(int column) throws SQLException {
//    return null;
//  }
//
//  @Override
//  public String getSchemaName(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public int getPrecision(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return 0;
//  }
//
//  @Override
//  public int getScale(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return 0;
//  }
//
//  @Override
//  public String getTableName(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public String getCatalogName(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public int getColumnType(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return 0;
//  }
//
//  @Override
//  public String getColumnTypeName(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//
//  @Override
//  public boolean isReadOnly(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//
//  @Override
//  public boolean isWritable(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//
//  @Override
//  public boolean isDefinitelyWritable(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return false;
//  }
//
//  @Override
//  public String getColumnClassName(int column) throws SQLException {
//    // TODO Auto-generated method stub
//    return null;
//  }
//}
//
///**
// * Tuple.
// *
// * @author BaseX Team 2005-11, BSD License
// * @author Rositsa Shadura
// */
//final class Tuple {
//  /** Column with type BigDecimal. */
//  public BigDecimal bigdec;
//  /** Column with type Boolean. */
//  public Boolean bool;
//
//  /**
//   * Constructor.
//   * @param bigdec column with type BigDecimal
//   * @param bool column with type Boolean
//   */
//  @SuppressWarnings("hiding")
//  public Tuple(final BigDecimal bigdec, final Boolean bool) {
//    this.bigdec = bigdec;
//    this.bool = bool;
//  }
//}
