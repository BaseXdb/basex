package org.basex.core.users;

import static org.basex.util.Token.*;

/**
 * This class assembles texts which are used in the user management.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public interface UserText {
  /** User name. */
  String[] S_USERINFO = { "Username", "Permission" };
  /** Default user and password. */
  String ADMIN = "admin";

  /** Users. */
  byte[] USERS = token("users");
  /** User. */
  byte[] USER = token("user");
  /** Name. */
  byte[] NAME = token("name");
  /** Algorithm. */
  byte[] ALGORITHM = token("algorithm");
  /** Password. */
  byte[] PASSWORD = token("password");
  /** Database. */
  byte[] DATABASE = token("database");
  /** Pattern. */
  byte[] PATTERN = token("pattern");
  /** Permission. */
  byte[] PERMISSION = token("permission");
}
