package org.basex.query.func.admin;

import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Admin function.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
abstract class AdminFn extends StandardFunc {
  /** QName. */
  static final QNm Q_SESSION = new QNm("session");
  /** QName. */
  static final QNm Q_ENTRY = new QNm("entry");
  /** QName. */
  static final QNm Q_SIZE = new QNm("size");
  /** QName. */
  static final QNm Q_TIME = new QNm("time");
  /** QName. */
  static final QNm Q_ADDRESS = new QNm("address");
  /** QName. */
  static final QNm Q_FILE = new QNm("file");
  /** QName. */
  static final QNm Q_TYPE = new QNm("type");
  /** QName. */
  static final QNm Q_MS = new QNm("ms");
}
