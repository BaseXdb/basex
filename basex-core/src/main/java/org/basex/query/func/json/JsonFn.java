package org.basex.query.func.json;

import static org.basex.query.QueryText.*;

import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Functions for parsing and serializing JSON objects.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class JsonFn extends StandardFunc {
  /** Element: options. */
  static final QNm Q_OPTIONS = QNm.get("json:options", JSONURI);
}
