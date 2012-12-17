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
   * @param d requires opened database
   * @param a arguments
   */
  AInfo(final boolean d, final String... a) {
    super(Perm.READ, d, a);
  }

  /**
   * Formats the specified input.
   * @param tb token builder
   * @param key key
   * @param val value
   */
  static void info(final TokenBuilder tb, final String key, final String val) {
    tb.add(' ').add(key).add(COLS).add(val).add(NL);
  }
}
