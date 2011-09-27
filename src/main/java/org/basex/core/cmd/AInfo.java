package org.basex.core.cmd;

import static org.basex.core.Text.*;
import org.basex.core.Command;
import org.basex.util.TokenBuilder;

/**
 * Abstract class for database info.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class AInfo extends Command {
  /**
   * Protected constructor.
   * @param p properties
   * @param a arguments
   */
  protected AInfo(final int p, final String... a) {
    super(p, a);
  }

  /**
   * Formats the specified input.
   * @param tb token builder
   * @param key key
   * @param val value
   */
  protected static void format(final TokenBuilder tb, final String key,
      final String val) {
    tb.add(' ').add(key).add(COLS).add(val).add(NL);
  }
}
