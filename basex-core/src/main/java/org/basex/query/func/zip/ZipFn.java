package org.basex.query.func.zip;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class ZipFn extends StandardFunc {
  /** Module prefix. */
  private static final String PREFIX = "zip";
  /** QName. */
  static final QNm Q_FILE = QNm.get(PREFIX, "file", ZIPURI);
  /** QName. */
  static final QNm Q_DIR = QNm.get(PREFIX, "dir", ZIPURI);
  /** QName. */
  static final QNm Q_ENTRY = QNm.get(PREFIX, "entry", ZIPURI);

  /** Attribute: href. */
  static final byte[] HREF = token("href");
  /** Attribute: name. */
  static final byte[] NAME = token("name");
}
