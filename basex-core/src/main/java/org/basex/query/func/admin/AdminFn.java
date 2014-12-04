package org.basex.query.func.admin;

import org.basex.query.func.*;

/**
 * Admin function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class AdminFn extends StandardFunc {
  /** QName: user. */
  static final String DATABASE = "database";
  /** QName: user. */
  static final String SESSION = "session";
  /** QName: user. */
  static final String USER = "user";
  /** QName: permission. */
  static final String PERMISSION = "permission";
  /** QName: salt. */
  static final String SALT = "salt";
  /** QName: password. */
  static final String HASH = "hash";
  /** QName: digest. */
  static final String DIGEST = "digest";
  /** QName: entry. */
  static final String ENTRY = "entry";
  /** Size element name. */
  static final String SIZE = "size";
  /** QName: time. */
  static final String TIME = "time";
  /** QName: address. */
  static final String ADDRESS = "address";
  /** QName: file. */
  static final String FILE = "file";
  /** QName: type. */
  static final String TYPE = "type";
  /** QName: ms. */
  static final String MS = "ms";
}
