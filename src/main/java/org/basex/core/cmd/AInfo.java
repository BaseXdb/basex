package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * Abstract class for database info.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class AInfo extends Command {
  /**
   * Protected constructor.
   * @param p properties
   * @param a arguments
   */
  AInfo(final int p, final String... a) {
    super(p, a);
  }

  /**
   * Formats the specified input.
   * @param tb token builder
   * @param key key
   * @param val value
   */
  static void format(final TokenBuilder tb, final String key, final String val) {
    tb.add(' ').add(key).add(COLS).add(val).add(NL);
  }
}
