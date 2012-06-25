package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery SQL functions prefixed with "sql".
 * Currently, due to the lack of a default JDBC driver, only tests signatures.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNSqlTest extends AdvancedQueryTest {
  /** Test method for the sql:init() function. */
  @Test
  public void sqlInit() {
    check(_SQL_INIT);
  }

  /** Test method for the sql:connect() function. */
  @Test
  public void sqlConnect() {
    check(_SQL_CONNECT);
  }

  /** Test method for the sql:prepare() function. */
  @Test
  public void sqlPrepare() {
    check(_SQL_PREPARE);
  }

  /** Test method for the sql:execute() function. */
  @Test
  public void sqlExecute() {
    check(_SQL_EXECUTE);
  }

  /** Test method for the sql:execute-prepared() function. */
  @Test
  public void sqlExecutePrepared() {
    check(_SQL_EXECUTE_PREPARED);
  }

  /** Test method for the sql:close() function. */
  @Test
  public void sqlClose() {
    check(_SQL_CLOSE);
  }

  /** Test method for the sql:commit() function. */
  @Test
  public void sqlCommit() {
    check(_SQL_COMMIT);
  }

  /** Test method for the sql:rollback() function. */
  @Test
  public void sqlRollback() {
    check(_SQL_ROLLBACK);
  }
}
