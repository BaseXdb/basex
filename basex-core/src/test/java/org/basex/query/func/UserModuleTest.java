package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

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
  /** Initialize tests. */
  @BeforeClass public static void beforeClass() {
    // create database
    query(_DB_CREATE.args(NAME));
  }

  /** Initialize test. */
  @Before public void before() {
    // create user and local permission
    query(_USER_CREATE.args(NAME, NAME));
    query(_USER_GRANT.args(NAME, Perm.WRITE, NAME));
  }

  /** Finish test. */
  @After public void after() {
    // drop user
    query("if(" + _USER_EXISTS.args(NAME) + ") then " + _USER_DROP.args(NAME) + " else ()");
  }

  /** Test method. */
  @Test public void exists() {
    query(_USER_EXISTS.args(UserText.ADMIN), "true");
    query(_USER_EXISTS.args(NAME), "true");
    query(_USER_EXISTS.args("unknown"), "false");

    // invalid name
    error(_USER_EXISTS.args(""), BXUS_NAME_X);
  }

  /** Test method. */
  @Test public void list() {
    query(_USER_LIST.args(), UserText.ADMIN + ' ' + NAME);
  }

  /** Test method. */
  @Test public void listDetails() {
    // check if the admin user exists
    query(_USER_LIST_DETAILS.args() + "/@name = '" + UserText.ADMIN + "'", "true");
    // check if the temporarily created user is found
    query(_USER_LIST_DETAILS.args() + "/@name = '" + NAME + '\'', "true");
    // check if local permission is found
    query(_USER_LIST_DETAILS.args() + "/database/@name = '" + NAME + '\'', "true");
    // check if user has been removed
    query(_USER_DROP.args(NAME));
    query(_USER_LIST_DETAILS.args() + "/database/@name = '" + NAME + '\'', "false");
    query(_USER_LIST_DETAILS.args() + "/@name = '" + NAME + '\'', "false");
  }

  /** Test method. */
  @Test public void create() {
    // allow empty passwords, overwriting existing users
    query(_USER_CREATE.args(NAME, ""));
    // specify permission
    query(_USER_CREATE.args(NAME, NAME, Perm.ADMIN));

    // invalid permission
    error(_USER_CREATE.args(NAME, NAME, ""), BXUS_PERM_X);
    // admin cannot be modified
    error(_USER_CREATE.args(UserText.ADMIN, ""), BXUS_ADMIN);
    // invalid name
    error(_USER_CREATE.args("", ""), BXUS_NAME_X);
    error(_USER_CREATE.args("", "", Perm.ADMIN), BXUS_NAME_X);
  }

  /** Test method. */
  @Test public void drop() {
    // create and drop local permission
    query(_USER_DROP.args(NAME, NAME));
    query(_USER_LIST_DETAILS.args() + "/database/@name = '" + NAME + '\'', "false");

    // drop user
    query(_USER_DROP.args(NAME));
    query(_USER_EXISTS.args(NAME), "false");

    // admin cannot be modified
    error(_USER_DROP.args(UserText.ADMIN), BXUS_ADMIN);
    // invalid name
    error(_USER_DROP.args(""), BXUS_NAME_X);
    error(_USER_DROP.args("", NAME), BXUS_NAME_X);
  }

  /** Test method. */
  @Test public void grant() {
    // change global and local permission
    query(_USER_GRANT.args(NAME, Perm.READ));
    query(_USER_GRANT.args(NAME, Perm.WRITE, NAME));

    // check permissions
    query(_USER_LIST_DETAILS.args() + "[@name = '" + NAME + "']/@permission/string()", "read");
    query(_USER_LIST_DETAILS.args() + "/database[@name = '" + NAME + "']/@permission/string()",
        "write");

    // admin cannot be modified
    error(_USER_GRANT.args(UserText.ADMIN, Perm.ADMIN), BXUS_ADMIN);
    error(_USER_GRANT.args(UserText.ADMIN, Perm.ADMIN, NAME), BXUS_ADMIN);
    // admin permission can only be set globally
    error(_USER_GRANT.args(NAME, Perm.ADMIN, NAME), BXUS_LOCAL_X);
    error(_USER_GRANT.args(NAME, Perm.CREATE, NAME), BXUS_LOCAL_X);
    // invalid name
    error(_USER_GRANT.args("", Perm.ADMIN), BXUS_NAME_X);
    error(_USER_GRANT.args("", Perm.ADMIN, NAME), BXUS_NAME_X);
  }

  /** Test method. */
  @Test public void alter() {
    // rename user
    query(_USER_ALTER.args(NAME, NAME + '2'));

    // overwrite user
    query(_USER_CREATE.args(NAME, ""));
    query(_USER_ALTER.args(NAME + '2', NAME));

    // admin cannot be modified
    error(_USER_ALTER.args(UserText.ADMIN, NAME), BXUS_ADMIN);
    error(_USER_ALTER.args(NAME, UserText.ADMIN), BXUS_ADMIN);
    // invalid name
    error(_USER_ALTER.args("", NAME), BXUS_NAME_X);
    error(_USER_ALTER.args(NAME, ""), BXUS_NAME_X);
  }

  /** Test method. */
  @Test public void password() {
    query(_USER_PASSWORD.args(NAME, ""));
    query(_USER_PASSWORD.args(NAME, "string-join((1 to 1000)!'x')"));

    // invalid name
    error(_USER_PASSWORD.args("", ""), BXUS_NAME_X);
  }
}
