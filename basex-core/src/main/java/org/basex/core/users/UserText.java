package org.basex.core.users;

import org.basex.query.value.item.*;

/**
 * This class assembles texts which are used in the user management.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public interface UserText {
  /** Username, permission. */
  String[] S_USERINFO = { "Username", "Permission" };
  /** Default user and password. */
  String ADMIN = "admin";

  /** QName. */
  QNm Q_USERS = new QNm("users");
  /** QName. */
  QNm Q_USER = new QNm("user");
  /** QName. */
  QNm Q_NAME = new QNm("name");
  /** QName. */
  QNm Q_ALGORITHM = new QNm("algorithm");
  /** QName. */
  QNm Q_PASSWORD = new QNm("password");
  /** QName. */
  QNm Q_DATABASE = new QNm("database");
  /** QName. */
  QNm Q_PATTERN = new QNm("pattern");
  /** QName. */
  QNm Q_PERMISSION = new QNm("permission");
  /** QName. */
  QNm Q_INFO = new QNm("info");
}
