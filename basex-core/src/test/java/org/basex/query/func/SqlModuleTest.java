package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.*;
import java.sql.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the SQL Module against an in-process {@link MockDriver},
 * so that no external JDBC driver dependency is required.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SqlModuleTest extends SandboxTest {
  /** Mock database URL. */
  private static final String URL = "jdbc:mock:test";

  /** Resets the mock driver. */
  @BeforeEach public void setup() {
    MockDriver.reset();
  }

  /** Test method. */
  @Test public void init() {
    final Function func = _SQL_INIT;
    query(func.args(MockDriver.class.getName()), "");
    error(func.args("no.such.Driver"), SQL_INIT_X);
  }

  /** Test method. */
  @Test public void connect() {
    final Function func = _SQL_CONNECT;
    assertTrue(query(func.args(URL)).startsWith("jdbc:mock"));
    // auto-commit option is forwarded to the connection
    query(func.args(URL, " ()", " ()", " { 'autocommit': false() }"));
    assertFalse(MockDriver.autoCommit);
  }

  /** Test method. */
  @Test public void execute() {
    final Function func = _SQL_EXECUTE;
    // query: result set is mapped to <sql:row/> elements
    MockDriver.result(new String[] { "id", "name" }, new Object[] { 1, "x" });
    final String row = query(conn(func.args(" $c", "select * from t")));
    assertTrue(row.contains("<sql:column name=\"id\">1</sql:column>"), row);
    assertTrue(row.contains("<sql:column name=\"name\">x</sql:column>"), row);

    // update: number of affected rows is returned
    MockDriver.updateCount = 5;
    query(conn(func.args(" $c", "update t set x = 1")), 5);

    // null columns are omitted by default, emitted with the 'null' option
    MockDriver.result(new String[] { "id", "name" }, new Object[] { 1, null });
    assertFalse(query(conn(func.args(" $c", "select * from t"))).contains("name=\"name\""));
    assertTrue(query(conn(func.args(" $c", "select * from t", " { 'null': true() }"))).contains(
        "<sql:column name=\"name\" null=\"true\"/>"));

    // errors: SQL exceptions are mapped to module errors
    MockDriver.failure = new SQLException("boom");
    error(conn(func.args(" $c", "select 1")), SQL_ERROR_X);
    MockDriver.failure = new SQLTimeoutException("slow");
    error(conn(func.args(" $c", "select 1")), SQL_TIMEOUT_X);
  }

  /** Test method. */
  @Test public void generatedKeys() {
    final Function func = _SQL_EXECUTE;
    MockDriver.keys(new String[] { "id" }, new Object[] { 42 });
    final String row = query(conn(func.args(" $c", "insert into t (x) values (1)",
        " { 'generated-keys': true() }")));
    assertTrue(MockDriver.keysRequested);
    assertTrue(row.contains("<sql:column name=\"id\">42</sql:column>"), row);
  }

  /** Test method. */
  @Test public void prepare() {
    final Function func = _SQL_PREPARE;
    assertTrue(query(conn(func.args(" $c", "select 1"))).contains("statement-"));
  }

  /** Test method. */
  @Test public void executePrepared() {
    final Function func = _SQL_EXECUTE_PREPARED;
    // parameters supplied as array: values are bound positionally, types inferred
    MockDriver.result(new String[] { "id" }, new Object[] { 7 });
    final String row = query("let $c := " + _SQL_CONNECT.args(URL) +
        " let $p := " + _SQL_PREPARE.args(" $c", "select * from t where id = ?") +
        " return " + func.args(" $p", " [ 7 ]"));
    assertTrue(row.contains("<sql:column name=\"id\">7</sql:column>"), row);
    assertEquals(1, MockDriver.bindings.size());
    assertEquals(7L, ((Number) MockDriver.bindings.get(0)).longValue());

    // parameters supplied as <sql:parameters/> element, including a null binding
    MockDriver.reset();
    final String params = "<sql:parameters xmlns:sql='http://basex.org/modules/sql'>" +
        "<sql:parameter type='int'>7</sql:parameter>" +
        "<sql:parameter type='string' null='true'/></sql:parameters>";
    query("let $c := " + _SQL_CONNECT.args(URL) +
        " let $p := " + _SQL_PREPARE.args(" $c", "insert into t values (?, ?)") +
        " return " + func.args(" $p", " " + params));
    assertEquals(7, MockDriver.bindings.get(0));
    assertSame(MockDriver.NULL, MockDriver.bindings.get(1));

    // errors
    error(conn(func.args(" $c")), SQL_ID2_X);
    error(singleParam("<sql:parameter type='int'>x</sql:parameter>"), SQL_TYPE_X_X);
    error(singleParam("<sql:parameter>x</sql:parameter>"), SQL_PARAMETERS);
    error(singleParam("<sql:parameter type='int' x='y'>1</sql:parameter>"), SQL_ATTRIBUTE_X);
  }

  /** Test method: maps the XDM type of each array member to the corresponding JDBC binding. */
  @Test public void parameterTypes() {
    assertEquals("x", binding("'x'"));
    assertEquals(Boolean.TRUE, binding("true()"));
    assertEquals(7L, binding("7"));
    assertEquals(7, binding("xs:int(7)"));
    assertEquals(2.5, binding("xs:double('2.5')"));
    assertEquals(1.5f, binding("xs:float('1.5')"));
    assertEquals(new BigDecimal("3.14"), binding("xs:decimal('3.14')"));
    assertArrayEquals(new byte[] { 1, 2 }, (byte[]) binding("xs:hexBinary('0102')"));
    // temporal types are mapped to the matching java.sql types
    assertInstanceOf(Date.class, binding("xs:date('2026-01-15')"));
    assertInstanceOf(Time.class, binding("xs:time('10:30:00')"));
    assertInstanceOf(Timestamp.class, binding("xs:dateTime('2026-01-15T10:30:00')"));
    // empty sequence is bound to NULL
    assertSame(MockDriver.NULL, binding("()"));
  }

  /** Test method. */
  @Test public void executeBatch() {
    final Function func = _SQL_EXECUTE_BATCH;
    // two parameter sets via the array form: one update count per set
    final String batch = "let $c := " + _SQL_CONNECT.args(URL) +
        " let $p := " + _SQL_PREPARE.args(" $c", "insert into t values (?, ?)") +
        " return " + func.args(" $p", " ([ 1, 'a' ], [ 2, 'b' ])");
    query("count(" + batch + ")", 2);
    // each set was bound positionally and added to the batch, in order
    assertEquals(2, MockDriver.batches.size());
    assertEquals(1L, MockDriver.batches.get(0).get(0));
    assertEquals("a", MockDriver.batches.get(0).get(1));
    assertEquals(2L, MockDriver.batches.get(1).get(0));
    assertEquals("b", MockDriver.batches.get(1).get(1));
  }

  /** Test method. */
  @Test public void commit() {
    final Function func = _SQL_COMMIT;
    query(conn(func.args(" $c")), "");
    assertTrue(MockDriver.committed);
  }

  /** Test method. */
  @Test public void rollback() {
    final Function func = _SQL_ROLLBACK;
    query(conn(func.args(" $c")), "");
    assertTrue(MockDriver.rolledBack);
  }

  /** Test method. */
  @Test public void close() {
    final Function func = _SQL_CLOSE;
    // a closed connection can no longer be used
    error("let $c := " + _SQL_CONNECT.args(URL) + " let $_ := " + func.args(" $c") +
        " return " + _SQL_EXECUTE.args(" $c", "select 1"), SQL_ID1_X);
  }

  /**
   * Wraps an expression with a bound mock connection {@code $c}.
   * @param expr expression referencing {@code $c}
   * @return query string
   */
  private static String conn(final String expr) {
    return "let $c := " + _SQL_CONNECT.args(URL) + " return " + expr;
  }

  /**
   * Binds a single array parameter and returns the value recorded by the mock driver.
   * @param value parameter value (raw XQuery)
   * @return recorded binding
   */
  private static Object binding(final String value) {
    MockDriver.reset();
    query("let $c := " + _SQL_CONNECT.args(URL) +
        " let $p := " + _SQL_PREPARE.args(" $c", "insert into t values (?)") +
        " return " + _SQL_EXECUTE_PREPARED.args(" $p", " [ " + value + " ]"));
    return MockDriver.bindings.get(0);
  }

  /**
   * Builds a query that executes a prepared statement with a single {@code <sql:parameter/>}.
   * @param parameter parameter element
   * @return query string
   */
  private static String singleParam(final String parameter) {
    final String params = "<sql:parameters xmlns:sql='http://basex.org/modules/sql'>" +
        parameter + "</sql:parameters>";
    return "let $c := " + _SQL_CONNECT.args(URL) +
        " let $p := " + _SQL_PREPARE.args(" $c", "insert into t values (?)") +
        " return " + _SQL_EXECUTE_PREPARED.args(" $p", " " + params);
  }
}
