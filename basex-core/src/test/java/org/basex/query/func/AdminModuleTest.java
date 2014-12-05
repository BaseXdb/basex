package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the functions of the Admin Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class AdminModuleTest extends AdvancedQueryTest {
  /**
   * Init method.
   * @throws BaseXException database exception
   */
  @BeforeClass
  public static void init() throws BaseXException {
    new CreateDB(NAME).execute(context);
    new DropUser(NAME).execute(context);
  }

  /**
   * Init method.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void users() throws BaseXException {
    // check if the admin user exists
    query(_ADMIN_USERS.args() + "/@name = 'admin'", "true");
    // check if the temporarily created user is found
    new CreateUser(NAME, NAME).execute(context);
    query(_ADMIN_USERS.args() + "/@name = '" + NAME + '\'', "true");
    // check if local user is found
    new Grant(Perm.READ, NAME, NAME).execute(context);
    query(_ADMIN_USERS.args(NAME) + "/@name = '" + NAME + '\'', "true");
    // check if user has been removed
    new DropUser(NAME).execute(context);
    query(_ADMIN_USERS.args(NAME) + "/@name = '" + NAME + '\'', "false");
    query(_ADMIN_USERS.args() + "/@name = '" + NAME + '\'', "false");
  }

  /**
   * Test method.
   */
  @Test
  public void sessions() {
    // can only be tested in client/server mode
    query(_ADMIN_SESSIONS.args(), "");
  }

  /**
   * Test method.
   */
  @Test
  public void logs() {
    // no logging data exists in the sandbox
    query(_ADMIN_LOGS.args(), "");
    error(_ADMIN_LOGS.args("2001-01-01"), WHICHRES_X);
  }
}
