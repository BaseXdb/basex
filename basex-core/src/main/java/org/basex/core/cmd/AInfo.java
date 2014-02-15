package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * Abstract class for database info.
 *
 * @author BaseX Team 2005-14, BSD License
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
  static void info(final TokenBuilder tb, final Object key, final Object val) {
    tb.add(' ').add(key.toString()).add(COLS).add(val.toString()).add(NL);
  }
}
