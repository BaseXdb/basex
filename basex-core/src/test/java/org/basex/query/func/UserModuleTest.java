package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the functions of the User Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class UserModuleTest extends AdvancedQueryTest {
  /**
   * Init method.
   * @throws BaseXException database exception
   */
  @BeforeClass
  public static void beforeClass() throws BaseXException {
    new CreateDB(NAME).execute(context);
  }

  /**
   * Finish test.
   * @throws BaseXException database exception
   */
  @After
  public void after() throws BaseXException {
    new DropUser(NAME).execute(context);
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void list() throws BaseXException {
    query(_USER_LIST.args() + "[. = 'admin']", "admin");
    // check if the temporarily created user is found
    new CreateUser(NAME, NAME).execute(context);
    query(_USER_LIST.args() + "[. = '" + NAME + "']", NAME);
  }

  /**
   * Test method.
   * @throws BaseXException database exception
   */
  @Test
  public void listDetails() throws BaseXException {
    // check if the admin user exists
    query(_USER_LIST_DETAILS.args() + "/@name = 'admin'", "true");
    // check if the temporarily created user is found
    new CreateUser(NAME, NAME).execute(context);
    query(_USER_LIST_DETAILS.args() + "/@name = '" + NAME + '\'', "true");
    // check if local user is found
    new Grant(Perm.READ, NAME, NAME).execute(context);
    query(_USER_LIST_DETAILS.args() + "/database/@name = '" + NAME + '\'', "true");
    // check if user has been removed
    new DropUser(NAME).execute(context);
    query(_USER_LIST_DETAILS.args() + "/database/@name = '" + NAME + '\'', "false");
    query(_USER_LIST_DETAILS.args() + "/@name = '" + NAME + '\'', "false");
  }
}
