package org.basex.query.func.csv;

import static org.basex.query.QueryText.*;

import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Functions for parsing CSV input.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class CsvFn extends StandardFunc {
  /** Element: options. */
  static final QNm Q_OPTIONS = QNm.get(CSV_PREFIX, "options", CSV_URI);

}
