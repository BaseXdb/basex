package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.users.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the User Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UserModuleTest extends SandboxTest {
  /** Initialize tests. */
  @BeforeAll public static void beforeClass() {
    // create database
    query(_DB_CREATE.args(NAME));
  }

  /** Initialize test. */
  @BeforeEach public void before() {
    // create user and local permission
    query(_USER_CREATE.args(NAME, NAME));
    query(_USER_GRANT.args(NAME, Perm.WRITE, NAME));
  }

  /** Finish test. */
  @AfterEach public void after() {
    // drop user
    query("if(" + _USER_EXISTS.args(NAME) + ") then " + _USER_DROP.args(NAME) + " else ()");
  }

  /** Test method. */
  @Test public void alter() {
    final Function func = _USER_ALTER;
    // rename user
    query(func.args(NAME, NAME + '2'));

    // overwrite user
    query(_USER_CREATE.args(NAME, ""));
    query(func.args(NAME + '2', NAME));

    // admin cannot be modified
    error(func.args(UserText.ADMIN, NAME), USER_ADMIN);
    error(func.args(NAME, UserText.ADMIN), USER_ADMIN);
    // invalid name
    error(func.args("", NAME), USER_NAME_X);
    error(func.args(NAME, ""), USER_NAME_X);
    // redundant operations
    error(func.args(NAME, "X") + ',' + func.args(NAME, "X"), USER_UPDATE1_X_X);
    // redundant operations
    error(func.args(NAME, "X") + ',' + _USER_DROP.args(NAME), USER_CONFLICT_X);
  }

  /** Test method. */
  @Test public void check() {
    final Function func = _USER_CHECK;
    query(func.args(UserText.ADMIN, UserText.ADMIN));
    error(func.args("", "x"), USER_NAME_X);
    error(func.args("x", "x"), USER_UNKNOWN_X);
    error(func.args(UserText.ADMIN, "x"), USER_PASSWORD_X);
  }

  /** Test method. */
  @Test public void create() {
    final Function func = _USER_CREATE;
    // allow empty passwords, overwriting existing users
    query(func.args(NAME, ""));
    // specify permissions
    query(func.args(NAME, NAME, Perm.ADMIN));
    query(func.args(NAME, NAME, " ('admin','none')", " ('','x')"));
    query(func.args(NAME, NAME, " ('admin','none')", " ('','x')", " <info a='x'/>"));

    // invalid permission
    error(func.args(NAME, NAME, ""), USER_PERMISSION_X);
    // admin cannot be modified
    error(func.args(UserText.ADMIN, ""), USER_ADMIN);
    // invalid name
    error(func.args("", ""), USER_NAME_X);
    error(func.args("", "", Perm.ADMIN), USER_NAME_X);

    // redundant operations
    error(func.args(NAME, "") + ',' + func.args(NAME, ""), USER_UPDATE1_X_X);
    error(func.args(NAME, "", " ('admin','admin')", " ('','')"), USER_UPDATE3_X_X);
    error(func.args(NAME, "", " ('admin','admin')", " ('x','x')"), USER_UPDATE2_X);
  }

  /** Test method. */
  @Test public void current() {
    final Function func = _USER_CURRENT;
    query(func.args(), UserText.ADMIN);
  }

  /** Test method. */
  @Test public void drop() {
    final Function func = _USER_DROP;
    // create and drop local permission
    query(func.args(NAME, NAME));
    query(_USER_LIST_DETAILS.args() + "/database/@pattern = '" + NAME + '\'', false);

    // drop list of permissions
    query(func.args(NAME, "('x','y')"));

    // invalid database pattern
    error(func.args(NAME, ";"), USER_PATTERN_X);
    // redundant operations
    error(func.args(NAME) + ',' + func.args(NAME), USER_UPDATE1_X_X);
    error(func.args(NAME, 'x') + ',' + func.args(NAME, 'x'), USER_UPDATE2_X);
    error(func.args(NAME) + ',' + _USER_ALTER.args(NAME, "X"), USER_CONFLICT_X);

    // drop user
    query(func.args(NAME));
    query(_USER_EXISTS.args(NAME), false);

    // admin cannot be modified
    error(func.args(UserText.ADMIN), USER_ADMIN);
    // invalid name
    error(func.args(NAME, ""), USER_UNKNOWN_X);
    error(func.args(""), USER_NAME_X);
    error(func.args("", NAME), USER_NAME_X);
  }

  /** Test method. */
  @Test public void exists() {
    final Function func = _USER_EXISTS;
    query(func.args(UserText.ADMIN), true);
    query(func.args(NAME), true);
    query(func.args("unknown"), false);

    // invalid name
    error(func.args(""), USER_NAME_X);
  }

  /** Test method. */
  @Test public void grant() {
    final Function func = _USER_GRANT;
    // change global and local permission
    query(func.args(NAME, Perm.READ));
    query(func.args(NAME, Perm.READ, ""));
    query(func.args(NAME, Perm.WRITE, NAME));

    // check permissions
    query(_USER_LIST_DETAILS.args() + "[@name = '" + NAME + "']/@permission/string()", "read");
    query(_USER_LIST_DETAILS.args() + "/database[@pattern = '" + NAME + "']/@permission/string()",
        "write");

    // grant list of permissions
    query(func.args(NAME, " ('admin','none')", " ('','x')"));

    // admin permission can only be set globally
    error(func.args(NAME, Perm.ADMIN, NAME), USER_LOCAL);
    error(func.args(NAME, Perm.CREATE, NAME), USER_LOCAL);
    // admin cannot be modified
    error(func.args(UserText.ADMIN, Perm.ADMIN), USER_ADMIN);
    error(func.args(UserText.ADMIN, Perm.ADMIN, NAME), USER_ADMIN);
    // invalid names and permissions
    error(func.args("", Perm.ADMIN), USER_NAME_X);
    error(func.args("", Perm.ADMIN, NAME), USER_NAME_X);
    error(func.args(NAME, Perm.ADMIN, ";"), USER_PATTERN_X);
    error(func.args(NAME, "x"), USER_PERMISSION_X);

    // redundant operations
    error(func.args(NAME, Perm.READ) + ',' + func.args(NAME, Perm.WRITE), USER_UPDATE1_X_X);
    error(func.args(NAME, Perm.READ, 'x') + ',' + func.args(NAME, Perm.WRITE, 'x'),
        USER_UPDATE2_X);
  }

  /** Test method. */
  @Test public void info() {
    final Function func = _USER_INFO;
    query(func.args(), "<info/>");
    error(func.args("dummy"), USER_UNKNOWN_X);
  }

  /** Test method. */
  @Test public void list() {
    final Function func = _USER_LIST;
    query(func.args(), UserText.ADMIN + '\n' + NAME);
  }

  /** Test method. */
  @Test public void listDetails() {
    final Function func = _USER_LIST_DETAILS;
    // check if the admin user exists
    query(func.args() + "/@name = '" + UserText.ADMIN + '\'', true);
    // check if the temporarily created user is found
    query(func.args() + "/@name = '" + NAME + '\'', true);
    // check if local permission is found
    query(func.args() + "/database/@pattern = '" + NAME + '\'', true);
    // check if user has been removed
    query(_USER_DROP.args(NAME));
    query(func.args() + "/database/@pattern = '" + NAME + '\'', false);
    query(func.args() + "/@name = '" + NAME + '\'', false);
    // specify user
    query("exists(" + func.args("admin") + ")", true);
    error(func.args("unknown"), USER_UNKNOWN_X);
  }

  /** Test method. */
  @Test public void password() {
    final Function func = _USER_PASSWORD;
    query(func.args(NAME, ""));
    query(func.args(NAME, "string-join((1 to 1000)!'x')"));

    // invalid name
    error(func.args("", ""), USER_NAME_X);
    // redundant operations
    error(func.args(NAME, "") + ',' + func.args(NAME, ""), USER_UPDATE1_X_X);
  }

  /** Test method. */
  @Test public void updateInfo() {
    final Function func = _USER_UPDATE_INFO;
    query(func.args(" <info>A</info>"));
    query(_USER_INFO.args(), "<info>A</info>");
    query(func.args(" <info/>"));

    query(func.args(" <info>B</info>", "admin"));
    query(_USER_INFO.args("admin"), "<info>B</info>");
    query(func.args(" <info/>", "admin"));

    // invalid input
    error(func.args(" <abc/>"), ELM_X_X);
  }
}
