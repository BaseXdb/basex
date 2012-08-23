package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery admin functions prefixed with "admin".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNAdminTest extends AdvancedQueryTest {
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
    query(_ADMIN_USERS.args() + "= 'admin'", "true");
    // check if the temporarily created user is found
    new CreateUser(NAME, md5(NAME)).execute(context);
    query(_ADMIN_USERS.args() + "= '" + NAME + "'", "true");
    // check if local user is found
    new Grant(Perm.READ, NAME, NAME).execute(context);
    query(_ADMIN_USERS.args(NAME) + "= '" + NAME + "'", "true");
    // check if user has been removed
    new DropUser(NAME).execute(context);
    query(_ADMIN_USERS.args(NAME) + "= '" + NAME + "'", "false");
    query(_ADMIN_USERS.args() + "= '" + NAME + "'", "false");
  }
}
